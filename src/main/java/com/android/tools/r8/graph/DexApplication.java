// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.ir.desugar.LambdaRewriter;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DexApplication {

  // Maps type into class promise, may be used concurrently.
  final ImmutableMap<DexType, DexClassPromise> classMap;

  public final ImmutableSet<DexType> mainDexList;

  private final ClassNameMapper proguardMap;

  public final Timing timing;

  public final DexItemFactory dexItemFactory;

  // Information on the lexicographically largest string referenced from code.
  public final DexString highestSortingString;

  /** Constructor should only be invoked by the DexApplication.Builder. */
  private DexApplication(
      ClassNameMapper proguardMap,
      ImmutableMap<DexType, DexClassPromise> classMap,
      ImmutableSet<DexType> mainDexList,
      DexItemFactory dexItemFactory,
      DexString highestSortingString,
      Timing timing) {
    this.proguardMap = proguardMap;
    this.mainDexList = mainDexList;
    this.classMap = classMap;
    this.dexItemFactory = dexItemFactory;
    this.highestSortingString = highestSortingString;
    this.timing = timing;
  }

  public Iterable<DexProgramClass> classes() {
    List<DexProgramClass> result = new ArrayList<>();
    for (DexClassPromise promise : classMap.values()) {
      if (promise.isProgramClass()) {
        result.add(promise.get().asProgramClass());
      }
    }
    return result;
  }

  public Iterable<DexLibraryClass> libraryClasses() {
    List<DexLibraryClass> result = new ArrayList<>();
    for (DexClassPromise promise : classMap.values()) {
      if (promise.isLibraryClass()) {
        result.add(promise.get().asLibraryClass());
      }
    }
    return result;
  }

  public DexClass definitionFor(DexType type) {
    DexClassPromise promise = classMap.get(type);
    return promise == null ? null : promise.get();
  }

  public DexProgramClass programDefinitionFor(DexType type) {
    DexClassPromise promise = classMap.get(type);
    return (promise == null || !promise.isProgramClass()) ? null : promise.get().asProgramClass();
  }

  public String toString() {
    return "Application (classes #" + classMap.size() + ")";
  }

  public ClassNameMapper getProguardMap() {
    return proguardMap;
  }

  private void disassemble(DexEncodedMethod method, ClassNameMapper naming, Path outputDir) {
    if (method.getCode() != null) {
      PrintStream ps = System.out;
      try {
        String clazzName;
        String methodName;
        if (naming != null) {
          clazzName = naming.originalNameOf(method.method.holder);
          methodName = naming.originalSignatureOf(method.method).toString();
        } else {
          clazzName = method.method.holder.toSourceString();
          methodName = method.method.name.toString();
        }
        if (outputDir != null) {
          Path directory = outputDir.resolve(clazzName.replace('.', '/'));
          String name = methodName + ".dump";
          if (name.length() > 200) {
            name = StringUtils.computeMD5Hash(name);
          }
          Files.createDirectories(directory);
          ps = new PrintStream(Files.newOutputStream(directory.resolve(name)));
        }
        ps.println("Bytecode for");
        ps.println("Class: '" + clazzName + "'");
        ps.println("Method: '" + methodName + "':");
        ps.println(method.getCode().toString(naming));
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (outputDir != null) {
          ps.flush();
          ps.close();
        }
      }
    }
  }

  /**
   * Write disassembly for the application code in the provided directory.
   *
   * <p>If no directory is provided everything is written to System.out.
   */
  public void disassemble(Path outputDir, InternalOptions options) {
    for (DexClass clazz : classes()) {
      for (DexEncodedMethod method : clazz.virtualMethods()) {
        if (options.methodMatchesFilter(method)) {
          disassemble(method, getProguardMap(), outputDir);
        }
      }
      for (DexEncodedMethod method : clazz.directMethods()) {
        if (options.methodMatchesFilter(method)) {
          disassemble(method, getProguardMap(), outputDir);
        }
      }
    }
  }

  /** Return smali source for the application code. */
  public String smali(InternalOptions options) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(os);
    smali(options, ps);
    return new String(os.toByteArray(), StandardCharsets.UTF_8);
  }

  private void writeClassHeader(DexClass clazz, PrintStream ps) {
    StringBuilder builder = new StringBuilder();
    builder.append(".class ");
    builder.append(clazz.accessFlags.toSmaliString());
    builder.append(" ");
    builder.append(clazz.type.toSmaliString());
    builder.append("\n\n");
    if (clazz.type != dexItemFactory.objectType) {
      builder.append(".super ");
      builder.append(clazz.superType.toSmaliString());
      // TODO(sgjesse): Add implemented interfaces
      builder.append("\n");
    }
    ps.append(builder.toString());
  }

  /** Write smali source for the application code on the provided PrintStream. */
  public void smali(InternalOptions options, PrintStream ps) {
    for (DexClass clazz : classes()) {
      boolean classHeaderWritten = false;
      for (DexEncodedMethod method : clazz.virtualMethods()) {
        if (options.methodMatchesFilter(method)) {
          if (!classHeaderWritten) {
            writeClassHeader(clazz, ps);
            classHeaderWritten = true;
          }
          ps.append("\n");
          ps.append(method.toSmaliString(getProguardMap()));
        }
      }
      for (DexEncodedMethod method : clazz.directMethods()) {
        if (options.methodMatchesFilter(method)) {
          if (!classHeaderWritten) {
            writeClassHeader(clazz, ps);
            classHeaderWritten = true;
          }
          ps.append("\n");
          ps.append(method.toSmaliString(getProguardMap()));
        }
      }
    }
  }

  public static class Builder {

    public final Hashtable<DexType, DexClassPromise> classMap = new Hashtable<>();
    public final Hashtable<DexCode, DexCode> codeItems = new Hashtable<>();

    public final DexItemFactory dexItemFactory;
    public ClassNameMapper proguardMap;
    private final Timing timing;

    public DexString highestSortingString;
    private final Set<DexType> mainDexList = Sets.newIdentityHashSet();

    public Builder(DexItemFactory dexItemFactory, Timing timing) {
      this.dexItemFactory = dexItemFactory;
      this.timing = timing;
    }

    public Builder(DexApplication application) {
      this(application, application.classMap);
    }

    public Builder(DexApplication application, Map<DexType, DexClassPromise> classMap) {
      this.classMap.putAll(classMap);
      proguardMap = application.proguardMap;
      timing = application.timing;
      highestSortingString = application.highestSortingString;
      dexItemFactory = application.dexItemFactory;
      mainDexList.addAll(application.mainDexList);
    }

    public synchronized Builder setProguardMap(ClassNameMapper proguardMap) {
      assert this.proguardMap == null;
      this.proguardMap = proguardMap;
      return this;
    }

    public synchronized Builder setHighestSortingString(DexString value) {
      highestSortingString = value;
      return this;
    }

    public Builder addClassPromise(DexClassPromise promise) {
      addClassPromise(promise, false);
      return this;
    }

    public Builder addClassIgnoringLibraryDuplicates(DexClass clazz) {
      addClass(clazz, true);
      return this;
    }

    public Builder addSynthesizedClass(DexProgramClass synthesizedClass, boolean addToMainDexList) {
      addClassPromise(synthesizedClass);
      if (addToMainDexList && !mainDexList.isEmpty()) {
        mainDexList.add(synthesizedClass.type);
      }
      return this;
    }

    // Callback from FileReader when parsing a DexProgramClass (multi-threaded).
    private void addClass(DexClass clazz, boolean skipLibDups) {
      addClassPromise(clazz, skipLibDups);
    }

    public synchronized void addClassPromise(DexClassPromise promise, boolean skipLibDups) {
      assert promise != null;
      DexType type = promise.getType();
      DexClassPromise oldPromise = classMap.get(type);
      if (oldPromise != null) {
        promise = chooseClass(promise, oldPromise, skipLibDups);
      }
      if (oldPromise != promise) {
        classMap.put(type, promise);
      }
    }

    private DexClassPromise chooseClass(DexClassPromise a, DexClassPromise b, boolean skipLibDups) {
      // NOTE: We assume that there should not be any conflicting names in user defined
      // classes and/or linked jars. If we ever want to allow 'keep first'-like policy
      // to resolve this kind of conflict between program and/or classpath classes, we'll
      // need to make sure we choose the class we keep deterministically.
      if (a.isProgramClass() && b.isProgramClass()) {
        if (allowProgramClassConflict(a, b)) {
          return a;
        }
        throw new CompilationError(
            "Program type already present: " + a.getType().toSourceString());
      }
      if (a.isProgramClass()) {
        return chooseBetweenProgramAndOtherClass(a, b);
      }
      if (b.isProgramClass()) {
        return chooseBetweenProgramAndOtherClass(b, a);
      }

      if (a.isClasspathClass() && b.isClasspathClass()) {
        throw new CompilationError(
            "Classpath type already present: " + a.getType().toSourceString());
      }
      if (a.isClasspathClass()) {
        return chooseBetweenClasspathAndLibraryClass(a, b);
      }
      if (b.isClasspathClass()) {
        return chooseBetweenClasspathAndLibraryClass(b, a);
      }

      return chooseBetweenLibraryClasses(b, a, skipLibDups);
    }

    private boolean allowProgramClassConflict(DexClassPromise a, DexClassPromise b) {
      // Currently only allow collapsing synthetic lambda classes.
      return a.getOrigin() == DexClass.Origin.Dex
          && b.getOrigin() == DexClass.Origin.Dex
          && a.get().accessFlags.isSynthetic()
          && b.get().accessFlags.isSynthetic()
          && LambdaRewriter.hasLambdaClassPrefix(a.getType())
          && LambdaRewriter.hasLambdaClassPrefix(b.getType());
    }

    private DexClassPromise chooseBetweenProgramAndOtherClass(
        DexClassPromise selected, DexClassPromise ignored) {
      assert selected.isProgramClass() && !ignored.isProgramClass();
      if (ignored.isLibraryClass()) {
        logIgnoredClass(ignored, "Class `%s` was specified as library and program type.");
      }
      // We don't log program/classpath class conflict since it is expected case.
      return selected;
    }

    private DexClassPromise chooseBetweenClasspathAndLibraryClass(
        DexClassPromise selected, DexClassPromise ignored) {
      assert selected.isClasspathClass() && ignored.isLibraryClass();
      logIgnoredClass(ignored, "Class `%s` was specified as library and classpath type.");
      return selected;
    }

    private DexClassPromise chooseBetweenLibraryClasses(
        DexClassPromise selected, DexClassPromise ignored, boolean skipDups) {
      assert selected.isLibraryClass() && ignored.isLibraryClass();
      if (!skipDups) {
        throw new CompilationError(
            "Library type already present: " + selected.getType().toSourceString());
      }
      logIgnoredClass(ignored, "Class `%s` was specified twice as a library type.");
      return selected;
    }

    private void logIgnoredClass(DexClassPromise ignored, String message) {
      if (Log.ENABLED) {
        Log.warn(getClass(), message, ignored.getType().toSourceString());
      }
    }

    public Builder addToMainDexList(Collection<DexType> mainDexList) {
      this.mainDexList.addAll(mainDexList);
      return this;
    }

    public DexApplication build() {
      return new DexApplication(
          proguardMap,
          ImmutableMap.copyOf(classMap),
          ImmutableSet.copyOf(mainDexList),
          dexItemFactory,
          highestSortingString,
          timing);
    }
  }
}
