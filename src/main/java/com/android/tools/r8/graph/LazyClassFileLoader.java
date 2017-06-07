// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import static com.android.tools.r8.utils.FileUtils.DEFAULT_DEX_FILENAME;

import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.google.common.io.Closer;
import java.io.IOException;

// Lazily loads a class file represented by resource.
public final class LazyClassFileLoader implements DexClassPromise {
  // Resource representing file definition.
  private final Resource resource;
  // Class kind to be created.
  private final ClassKind classKind;

  // Application reader to be used. Note that the reader may be reused in
  // many loaders and may be used concurrently, it is considered to be
  // thread-safe since its only state is internal options which we
  // consider immutable after they are initialized (barring dex factory
  // which is thread-safe).
  private final JarApplicationReader reader;

  // Dex type of the class to be created.
  private final DexType type;

  // Cached loaded class if get(...) method has already been called, this
  // field is only accessed in context synchronized on `this`.
  private DexClass loadedClass = null;

  public LazyClassFileLoader(DexType type,
      Resource resource, ClassKind classKind, JarApplicationReader reader) {
    this.resource = resource;
    this.reader = reader;
    this.type = type;
    this.classKind = classKind;
    assert classKind != ClassKind.PROGRAM;
  }

  // Callback method for JarClassFileReader, is always called in synchronized context.
  private void addClass(DexClass clazz) {
    assert clazz != null;
    assert loadedClass == null;
    loadedClass = clazz;
  }

  @Override
  public DexType getType() {
    return type;
  }

  @Override
  public Resource.Kind getOrigin() {
    return Resource.Kind.CLASSFILE;
  }

  @Override
  public boolean isProgramClass() {
    return false;
  }

  @Override
  public boolean isClasspathClass() {
    return classKind == ClassKind.CLASSPATH;
  }

  @Override
  public boolean isLibraryClass() {
    return classKind == ClassKind.LIBRARY;
  }

  // Loads the class from the resource. Synchronized on `this` to avoid
  // unnecessary complications, thus all threads trying to load a class with
  // this loader will wait for the first load to finish.
  @Override
  public synchronized DexClass get() {
    if (loadedClass != null) {
      return loadedClass;
    }

    try (Closer closer = Closer.create()) {
      JarClassFileReader reader = new JarClassFileReader(this.reader, this::addClass);
      reader.read(DEFAULT_DEX_FILENAME, classKind, resource.getStream(closer));
    } catch (IOException e) {
      throw new CompilationError("Failed to load class: " + type.toSourceString(), e);
    }

    if (loadedClass == null) {
      throw new Unreachable("Class is supposed to be loaded: " + type.toSourceString());
    }

    if (loadedClass.type != type) {
      throw new CompilationError("Class content provided for type descriptor "
          + type.toSourceString() + " actually defines class " + loadedClass.type.toSourceString());
    }

    return loadedClass;
  }
}
