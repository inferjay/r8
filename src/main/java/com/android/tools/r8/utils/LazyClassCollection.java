// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.DEFAULT_DEX_FILENAME;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.JarClassFileReader;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a collection of classes loaded lazily from a set of lazy resource
 * providers. The collection is autonomous, it lazily collects classes but
 * does not choose the classes in cases of conflicts, delaying it until
 * the class is asked for.
 *
 * NOTE: only java class resources are allowed to be lazy loaded.
 */
public final class LazyClassCollection {
  // For each type which has ever been queried stores one or several classes loaded
  // from resources provided by different resource providers. In majority of the
  // cases there will only be one class per type. We store classes for all the
  // resource providers and resolve the classes at the time it is queried.
  //
  // Must be synchronized on `classes`.
  private final Map<DexType, DexClass[]> classes = new IdentityHashMap<>();

  // Available lazy resource providers.
  private final List<ResourceProvider> classpathProviders;
  private final List<ResourceProvider> libraryProviders;

  // Application reader to be used. Note that the reader may be reused in
  // many loaders and may be used concurrently, it is considered to be
  // thread-safe since its only state is internal options which we
  // consider immutable after they are initialized (barring dex factory
  // which is thread-safe).
  private final JarApplicationReader reader;

  public LazyClassCollection(JarApplicationReader reader,
      List<ResourceProvider> classpathProviders, List<ResourceProvider> libraryProviders) {
    this.classpathProviders = ImmutableList.copyOf(classpathProviders);
    this.libraryProviders = ImmutableList.copyOf(libraryProviders);
    this.reader = reader;
  }

  /**
   * Returns a definition for a class or `null` if there is no such class.
   * Parameter `dominator` represents a class that is considered
   * to be already loaded, it may be null but if specified it may affect
   * the conflict resolution. For example non-lazy loaded classpath class
   * provided as `dominator` will conflict with lazy-loaded classpath classes.
   */
  public DexClass get(DexType type, DexClass dominator) {
    DexClass[] candidates;
    synchronized (classes) {
      candidates = classes.get(type);
    }

    if (candidates == null) {
      String descriptor = type.descriptor.toString();

      // Loading resources and constructing classes may be time consuming, we do it
      // outside the global lock so others don't have to wait.
      List<Resource> classpathResources = collectResources(classpathProviders, descriptor);
      List<Resource> libraryResources = collectResources(libraryProviders, descriptor);

      candidates = new DexClass[classpathResources.size() + libraryResources.size()];

      // Check if someone else has already added the array for this type.
      synchronized (classes) {
        DexClass[] existing = classes.get(type);
        if (existing != null) {
          assert candidates.length == existing.length;
          candidates = existing;
        } else {
          classes.put(type, candidates);
        }
      }

      if (candidates.length > 0) {
        // Load classes in synchronized content unique for the type.
        synchronized (candidates) {
          // Either all or none of the array classes will be loaded, so we use this
          // as a criteria for checking if we need to load classes.
          if (candidates[0] == null) {
            new ClassLoader(type, candidates, reader, classpathResources, libraryResources).load();
          }
        }
      }
    }

    // Choose class in case there are conflicts.
    DexClass candidate = dominator;
    for (DexClass clazz : candidates) {
      candidate = (candidate == null) ? clazz
          : DexApplication.chooseClass(candidate, clazz, /* skipLibDups: */ true);
    }
    return candidate;
  }

  private List<Resource> collectResources(List<ResourceProvider> providers, String descriptor) {
    List<Resource> resources = new ArrayList<>();
    for (ResourceProvider provider : providers) {
      Resource resource = provider.getResource(descriptor);
      if (resource != null) {
        resources.add(resource);
      }
    }
    return resources;
  }

  private static final class ClassLoader {
    int index = 0;
    final DexType type;
    final DexClass[] classes;
    final JarApplicationReader reader;
    final List<Resource> classpathResources;
    final List<Resource> libraryResources;

    ClassLoader(DexType type, DexClass[] classes, JarApplicationReader reader,
        List<Resource> classpathResources, List<Resource> libraryResources) {
      this.type = type;
      this.classes = classes;
      this.reader = reader;
      this.classpathResources = classpathResources;
      this.libraryResources = libraryResources;
    }

    void addClass(DexClass clazz) {
      assert index < classes.length;
      assert clazz != null;
      if (clazz.type != type) {
        throw new CompilationError("Class content provided for type descriptor "
            + type.toSourceString() + " actually defines class " + clazz.type
            .toSourceString());
      }
      classes[index++] = clazz;
    }

    void load() {
      try (Closer closer = Closer.create()) {
        for (Resource resource : classpathResources) {
          JarClassFileReader classReader = new JarClassFileReader(reader, this::addClass);
          classReader.read(DEFAULT_DEX_FILENAME, ClassKind.CLASSPATH, resource.getStream(closer));
        }
        for (Resource resource : libraryResources) {
          JarClassFileReader classReader = new JarClassFileReader(reader, this::addClass);
          classReader.read(DEFAULT_DEX_FILENAME, ClassKind.LIBRARY, resource.getStream(closer));
        }
      } catch (IOException e) {
        throw new CompilationError("Failed to load class: " + type.toSourceString(), e);
      }
      assert index == classes.length;
    }
  }
}
