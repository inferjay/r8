// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.ir.desugar.LambdaRewriter;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LazyClassCollection;
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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DexApplication {

  // Maps type into class, may be used concurrently.
  private final ImmutableMap<DexType, DexClass> classMap;

  // Lazily loaded classes.
  //
  // Note that this collection is autonomous and may be used in several
  // different applications. Particularly, it is the case when one
  // application is being build based on another one. Among others,
  // it will have an important side-effect: class conflict resolution,
  // generated errors in particular, may be different in lazy scenario.
  private final LazyClassCollection lazyClassCollection;

  public final ImmutableSet<DexType> mainDexList;

  private final ClassNameMapper proguardMap;

  public final Timing timing;

  public final DexItemFactory dexItemFactory;

  // Information on the lexicographically largest string referenced from code.
  public final DexString highestSortingString;

  /** Constructor should only be invoked by the DexApplication.Builder. */
  private DexApplication(
      ClassNameMapper proguardMap,
      ImmutableMap<DexType, DexClass> classMap,
      LazyClassCollection lazyClassCollection,
      ImmutableSet<DexType> mainDexList,
      DexItemFactory dexItemFactory,
      DexString highestSortingString,
      Timing timing) {
    this.proguardMap = proguardMap;
    this.lazyClassCollection = lazyClassCollection;
    this.mainDexList = mainDexList;
    this.classMap = classMap;
    this.dexItemFactory = dexItemFactory;
    this.highestSortingString = highestSortingString;
    this.timing = timing;
  }

  ImmutableMap<DexType, DexClass> getClassMap() {
    assert lazyClassCollection == null : "Only allowed in non-lazy scenarios.";
    return classMap;
  }

  public Iterable<DexProgramClass> classes() {
    List<DexProgramClass> result = new ArrayList<>();
    // Note: we ignore lazy class collection because it
    // is never supposed to be used for program classes.
    for (DexClass clazz : classMap.values()) {
      if (clazz.isProgramClass()) {
        result.add(clazz.asProgramClass());
      }
    }
    return result;
  }

  public Iterable<DexLibraryClass> libraryClasses() {
    assert lazyClassCollection == null : "Only allowed in non-lazy scenarios.";
    List<DexLibraryClass> result = new ArrayList<>();
    for (DexClass clazz : classMap.values()) {
      if (clazz.isLibraryClass()) {
        result.add(clazz.asLibraryClass());
      }
    }
    return result;
  }

  public DexClass definitionFor(DexType type) {
    DexClass clazz = classMap.get(type);
    // In presence of lazy class collection we also reach out to it
    // as well unless the class found is already a program class.
    if (lazyClassCollection != null && (clazz == null || !clazz.isProgramClass())) {
      clazz = lazyClassCollection.get(type, clazz);
    }
    return clazz;
  }

  public DexProgramClass programDefinitionFor(DexType type) {
    DexClass clazz = classMap.get(type);
    // Don't bother about lazy class collection, it should never load program classes.
    return (clazz == null || !clazz.isProgramClass()) ? null : clazz.asProgramClass();
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
      builder.append("\n");
      for (DexType iface : clazz.interfaces.values) {
        builder.append(".implements ");
        builder.append(iface.toSmaliString());
        builder.append("\n");
      }
    }
    ps.append(builder.toString());
  }

  private void writeClassFooter(DexClass clazz, PrintStream ps) {
    StringBuilder builder = new StringBuilder();
    builder.append("# End of class ");
    builder.append(clazz.type.toSmaliString());
    builder.append("\n");
    ps.append(builder.toString());
  }

  /**
   * Write smali source for the application code on the provided PrintStream.
   */
  public void smali(InternalOptions options, PrintStream ps) {
    List<DexProgramClass> classes = (List<DexProgramClass>) classes();
    classes.sort(Comparator.comparing(DexProgramClass::toSourceString));
    boolean firstClass = true;
    for (DexClass clazz : classes) {
      boolean classHeaderWritten = false;
      if (!options.hasMethodsFilter()) {
        if (!firstClass) {
          ps.append("\n");
          firstClass = false;
        }
        writeClassHeader(clazz, ps);
        classHeaderWritten = true;
      }
      for (DexEncodedMethod method : clazz.virtualMethods()) {
        if (options.methodMatchesFilter(method)) {
          if (!classHeaderWritten) {
            if (!firstClass) {
              ps.append("\n");
              firstClass = false;
            }
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
            if (!firstClass) {
              ps.append("\n");
              firstClass = false;
            }
            writeClassHeader(clazz, ps);
            classHeaderWritten = true;
          }
          ps.append("\n");
          ps.append(method.toSmaliString(getProguardMap()));
        }
      }
      if (classHeaderWritten) {
        ps.append("\n");
        writeClassFooter(clazz, ps);
      }
    }
  }

  public static class Builder {

    private final Hashtable<DexType, DexClass> classMap = new Hashtable<>();
    private LazyClassCollection lazyClassCollection;

    public final Hashtable<DexCode, DexCode> codeItems = new Hashtable<>();

    public final DexItemFactory dexItemFactory;
    public ClassNameMapper proguardMap;
    private final Timing timing;

    public DexString highestSortingString;
    private final Set<DexType> mainDexList = Sets.newIdentityHashSet();

    public Builder(DexItemFactory dexItemFactory, Timing timing) {
      this.dexItemFactory = dexItemFactory;
      this.timing = timing;
      this.lazyClassCollection = null;
    }

    public Builder(DexApplication application) {
      this(application, application.classMap);
    }

    public Builder(DexApplication application, Map<DexType, DexClass> classMap) {
      this.classMap.putAll(classMap);
      this.lazyClassCollection = application.lazyClassCollection;
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

    public Builder addClass(DexClass clazz) {
      addClass(clazz, false);
      return this;
    }

    public Builder setLazyClassCollection(LazyClassCollection lazyClassMap) {
      this.lazyClassCollection = lazyClassMap;
      return this;
    }

    public Builder addClassIgnoringLibraryDuplicates(DexClass clazz) {
      addClass(clazz, true);
      return this;
    }

    public Builder addSynthesizedClass(DexProgramClass synthesizedClass, boolean addToMainDexList) {
      assert synthesizedClass.isProgramClass() : "All synthesized classes must be program classes";
      addClass(synthesizedClass);
      if (addToMainDexList && !mainDexList.isEmpty()) {
        mainDexList.add(synthesizedClass.type);
      }
      return this;
    }

    public List<DexProgramClass> getProgramClasses() {
      List<DexProgramClass> result = new ArrayList<>();
      // Note: we ignore lazy class collection because it
      // is never supposed to be used for program classes.
      for (DexClass clazz : classMap.values()) {
        if (clazz.isProgramClass()) {
          result.add(clazz.asProgramClass());
        }
      }
      return result;
    }

    // Callback from FileReader when parsing a DexProgramClass (multi-threaded).
    public synchronized void addClass(DexClass newClass, boolean skipLibDups) {
      assert newClass != null;
      DexType type = newClass.type;
      DexClass oldClass = classMap.get(type);
      if (oldClass != null) {
        newClass = chooseClass(newClass, oldClass, skipLibDups);
      }
      if (oldClass != newClass) {
        classMap.put(type, newClass);
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
          lazyClassCollection,
          ImmutableSet.copyOf(mainDexList),
          dexItemFactory,
          highestSortingString,
          timing);
    }
  }

  public static DexClass chooseClass(DexClass a, DexClass b, boolean skipLibDups) {
    // NOTE: We assume that there should not be any conflicting names in user defined
    // classes and/or linked jars. If we ever want to allow 'keep first'-like policy
    // to resolve this kind of conflict between program and/or classpath classes, we'll
    // need to make sure we choose the class we keep deterministically.
    if (a.isProgramClass() && b.isProgramClass()) {
      if (allowProgramClassConflict(a.asProgramClass(), b.asProgramClass())) {
        return a;
      }
      throw new CompilationError("Program type already present: " + a.type.toSourceString());
    }
    if (a.isProgramClass()) {
      return chooseClass(a.asProgramClass(), b);
    }
    if (b.isProgramClass()) {
      return chooseClass(b.asProgramClass(), a);
    }

    if (a.isClasspathClass() && b.isClasspathClass()) {
      throw new CompilationError("Classpath type already present: " + a.type.toSourceString());
    }
    if (a.isClasspathClass()) {
      return chooseClass(a.asClasspathClass(), b.asLibraryClass());
    }
    if (b.isClasspathClass()) {
      return chooseClass(b.asClasspathClass(), a.asLibraryClass());
    }

    return chooseClasses(b.asLibraryClass(), a.asLibraryClass(), skipLibDups);
  }

  private static boolean allowProgramClassConflict(DexProgramClass a, DexProgramClass b) {
    // Currently only allow collapsing synthetic lambda classes.
    return a.getOrigin() == Resource.Kind.DEX
        && b.getOrigin() == Resource.Kind.DEX
        && a.accessFlags.isSynthetic()
        && b.accessFlags.isSynthetic()
        && LambdaRewriter.hasLambdaClassPrefix(a.type)
        && LambdaRewriter.hasLambdaClassPrefix(b.type);
  }

  private static DexClass chooseClass(DexProgramClass selected, DexClass ignored) {
    assert !ignored.isProgramClass();
    if (ignored.isLibraryClass()) {
      logIgnoredClass(ignored, "Class `%s` was specified as library and program type.");
    }
    // We don't log program/classpath class conflict since it is expected case.
    return selected;
  }

  private static DexClass chooseClass(DexClasspathClass selected, DexLibraryClass ignored) {
    logIgnoredClass(ignored, "Class `%s` was specified as library and classpath type.");
    return selected;
  }

  private static DexClass chooseClasses(
      DexLibraryClass selected, DexLibraryClass ignored, boolean skipDups) {
    if (!skipDups) {
      throw new CompilationError(
          "Library type already present: " + selected.type.toSourceString());
    }
    logIgnoredClass(ignored, "Class `%s` was specified twice as a library type.");
    return selected;
  }

  private static void logIgnoredClass(DexClass ignored, String message) {
    if (Log.ENABLED) {
      Log.warn(DexApplication.class, message, ignored.type.toSourceString());
    }
  }
}
