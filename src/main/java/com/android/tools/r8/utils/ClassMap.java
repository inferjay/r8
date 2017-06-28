// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a collection of classes. Collection can be fully loaded,
 * lazy loaded or have preloaded classes along with lazy loaded content.
 */
public abstract class ClassMap<T extends DexClass> {
  // For each type which has ever been queried stores one class loaded from
  // resources provided by different resource providers.
  //
  // NOTE: all access must be synchronized on `classes`.
  private final Map<DexType, Value<T>> classes;

  // Class provider if available. In case it's `null`, all classes of
  // the collection must be pre-populated in `classes`.
  private final ClassProvider<T> classProvider;

  ClassMap(Map<DexType, Value<T>> classes, ClassProvider<T> classProvider) {
    this.classes = classes == null ? new IdentityHashMap<>() : classes;
    this.classProvider = classProvider;
    assert this.classProvider == null || this.classProvider.getClassKind() == getClassKind();
  }

  /** Resolves a class conflict by selecting a class, may generate compilation error. */
  abstract T resolveClassConflict(T a, T b);

  /** Kind of the classes supported by this collection. */
  abstract ClassKind getClassKind();

  @Override
  public String toString() {
    synchronized (classes) {
      return classes.size() + " loaded, provider: " +
          (classProvider == null ? "none" : classProvider.toString());
    }
  }

  /** Returns a definition for a class or `null` if there is no such class in the collection. */
  public T get(DexType type) {
    final Value<T> value = getOrCreateValue(type);

    if (value == null) {
      return null;
    }

    if (!value.ready) {
      // Load the value in a context synchronized on value instance. This way
      // we avoid locking the whole collection during expensive resource loading
      // and classes construction operations.
      synchronized (value) {
        if (!value.ready) {
          assert classProvider != null : "getOrCreateValue() created "
              + "Value for missing type when there is no classProvider.";
          classProvider.collectClass(type, clazz -> {
            assert clazz != null;
            assert getClassKind().isOfKind(clazz);
            assert !value.ready;

            if (clazz.type != type) {
              throw new CompilationError("Class content provided for type descriptor "
                  + type.toSourceString() + " actually defines class " + clazz.type
                  .toSourceString());
            }

            if (value.clazz == null) {
              value.clazz = clazz;
            } else {
              // The class resolution *may* generate a compilation error as one of
              // possible resolutions. In this case we leave `value` in (false, null)
              // state so in rare case of another thread trying to get the same class
              // before this error is propagated it will get the same conflict.
              T oldClass = value.clazz;
              value.clazz = null;
              value.clazz = resolveClassConflict(oldClass, clazz);
            }
          });
          value.ready = true;
        }
      }
    }

    assert value.ready;
    return value.clazz;
  }

  private Value<T> getOrCreateValue(DexType type) {
    synchronized (classes) {
      Value<T> value = classes.get(type);
      if (value == null && classProvider != null) {
        value = new Value<>();
        classes.put(type, value);
      }
      return value;
    }
  }

  /**
   * Returns currently loaded classes.
   *
   * Method is assumed to be called when the collection is fully loaded,
   * otherwise only a subset of potentially loaded classes may be returned.
   */
  public List<T> collectLoadedClasses() {
    List<T> loadedClasses = new ArrayList<>();
    synchronized (classes) {
      for (Value<T> value : classes.values()) {
        // NOTE: value mutations are NOT synchronized on `classes`, here we actually
        // can see value which is not ready yet. Since everything that exists should
        // be guaranteed by the caller to be loaded at this point, this can only happen
        // if the code references classes that do not exist. Therefore, if the value is
        // not ready here, we know that the loaded value will be 'null' once it is ready.
        if (value.ready && value.clazz != null) {
          loadedClasses.add(value.clazz);
        }
      }
    }
    return loadedClasses;
  }

  /** Forces loading of all the classes satisfying the criteria specified. */
  public void forceLoad(Predicate<DexType> load) {
    if (classProvider != null) {
      Set<DexType> loaded = Sets.newIdentityHashSet();
      synchronized (classes) {
        loaded.addAll(classes.keySet());
      }
      Collection<DexType> types = classProvider.collectTypes();
      for (DexType type : types) {
        if (load.test(type) && !loaded.contains(type)) {
          get(type); // force-load type.
        }
      }
    }
  }

  // Represents a value in the class map.
  final static class Value<T> {
    volatile boolean ready;
    T clazz;

    Value() {
      ready = false;
      clazz = null;
    }

    Value(T clazz) {
      this.clazz = clazz;
      this.ready = true;
    }
  }
}
