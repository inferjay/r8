// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexClassPromise;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.LazyClassFileLoader;
import com.google.common.collect.ImmutableList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a collection of classes loaded lazily from a set of lazy resource
 * providers. The collection is autonomous, it lazily collects promises but
 * does not choose the classes in cases of conflicts, delaying it until
 * the class is asked for.
 *
 * NOTE: only java class resources are allowed to be lazy loaded.
 */
public final class LazyClassCollection {
  // Stores promises for types which have ever been asked for before. Special value
  // EmptyPromise.INSTANCE indicates there were no resources for this type in any of
  // the resource providers.
  //
  // Promises are potentially coming from different providers and chained, but in majority
  // of the cases there will only be one promise per type. We store promises for all
  // the resource providers and resolve the classes at the time it is queried.
  //
  // Must be synchronized on `classes`.
  private final Map<DexType, DexClassPromise> classes = new IdentityHashMap<>();

  // Available lazy resource providers.
  private final List<ResourceProvider> providers;

  // Application reader to be used. Note that the reader may be reused in
  // many loaders and may be used concurrently, it is considered to be
  // thread-safe since its only state is internal options which we
  // consider immutable after they are initialized (barring dex factory
  // which is thread-safe).
  private final JarApplicationReader reader;

  public LazyClassCollection(JarApplicationReader reader, List<ResourceProvider> providers) {
    this.providers = ImmutableList.copyOf(providers);
    this.reader = reader;
  }

  /**
   * Returns a definition for a class or `null` if there is no such class.
   * Parameter `dominator` represents the class promise that is considered
   * to be already loaded, it may be null but if specified it may affect
   * the conflict resolution. For example non-lazy loaded classpath class
   * provided as `dominator` will conflict with lazy-loaded classpath classes.
   */
  public DexClassPromise get(DexType type, DexClassPromise dominator) {
    DexClassPromise promise;

    // Check if the promise already exists.
    synchronized (classes) {
      promise = classes.get(type);
    }

    if (promise == null) {
      // Building a promise may be time consuming, we do it outside
      // the lock so others don't have to wait.
      promise = buildPromiseChain(type);

      synchronized (classes) {
        DexClassPromise existing = classes.get(type);
        if (existing != null) {
          promise = existing;
        } else {
          classes.put(type, promise);
        }
      }

      assert promise != EmptyPromise.INSTANCE;
    }

    return promise == EmptyPromise.INSTANCE ? dominator : chooseClass(dominator, promise);
  }

  // Build chain of lazy promises or `null` if none of the providers
  // provided resource for this type.
  private DexClassPromise buildPromiseChain(DexType type) {
    String descriptor = type.descriptor.toString();
    DexClassPromise promise = null;
    int size = providers.size();
    for (int i = size - 1; i >= 0; i--) {
      Resource resource = providers.get(i).getResource(descriptor);
      if (resource == null) {
        continue;
      }

      if (resource.getKind() == Resource.Kind.PROGRAM) {
        throw new CompilationError("Attempt to load program class " +
            type.toSourceString() + " via lazy resource provider");
      }

      assert resource instanceof InternalResource;
      LazyClassFileLoader loader =
          new LazyClassFileLoader(type, (InternalResource) resource, reader);
      promise = (promise == null) ? loader : new DexClassPromiseChain(loader, promise);
    }
    return promise == null ? EmptyPromise.INSTANCE : promise;
  }

  // Chooses the proper promise. Recursion is not expected to be deep.
  private DexClassPromise chooseClass(DexClassPromise dominator, DexClassPromise candidate) {
    DexClassPromise best = (dominator == null) ? candidate
        : DexApplication.chooseClass(dominator, candidate, /* skipLibDups: */ true);
    return (candidate instanceof DexClassPromiseChain)
        ? chooseClass(best, ((DexClassPromiseChain) candidate).next) : best;
  }

  private static final class EmptyPromise implements DexClassPromise {
    static final EmptyPromise INSTANCE = new EmptyPromise();

    @Override
    public DexType getType() {
      throw new Unreachable();
    }

    @Override
    public DexClass.Origin getOrigin() {
      throw new Unreachable();
    }

    @Override
    public boolean isProgramClass() {
      throw new Unreachable();
    }

    @Override
    public boolean isClasspathClass() {
      throw new Unreachable();
    }

    @Override
    public boolean isLibraryClass() {
      throw new Unreachable();
    }

    @Override
    public DexClass get() {
      throw new Unreachable();
    }
  }

  private static final class DexClassPromiseChain implements DexClassPromise {
    final DexClassPromise promise;
    final DexClassPromise next;

    private DexClassPromiseChain(DexClassPromise promise, DexClassPromise next) {
      assert promise != null;
      assert next != null;
      this.promise = promise;
      this.next = next;
    }

    @Override
    public DexType getType() {
      return promise.getType();
    }

    @Override
    public DexClass.Origin getOrigin() {
      return promise.getOrigin();
    }

    @Override
    public boolean isProgramClass() {
      return promise.isProgramClass();
    }

    @Override
    public boolean isClasspathClass() {
      return promise.isClasspathClass();
    }

    @Override
    public boolean isLibraryClass() {
      return promise.isLibraryClass();
    }

    @Override
    public DexClass get() {
      return promise.get();
    }
  }
}
