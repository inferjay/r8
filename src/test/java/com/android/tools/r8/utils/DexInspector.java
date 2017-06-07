// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.code.Iget;
import com.android.tools.r8.code.IgetBoolean;
import com.android.tools.r8.code.IgetByte;
import com.android.tools.r8.code.IgetChar;
import com.android.tools.r8.code.IgetObject;
import com.android.tools.r8.code.IgetShort;
import com.android.tools.r8.code.IgetWide;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.InvokeDirect;
import com.android.tools.r8.code.InvokeDirectRange;
import com.android.tools.r8.code.InvokeInterface;
import com.android.tools.r8.code.InvokeInterfaceRange;
import com.android.tools.r8.code.InvokeStatic;
import com.android.tools.r8.code.InvokeStaticRange;
import com.android.tools.r8.code.InvokeSuper;
import com.android.tools.r8.code.InvokeSuperRange;
import com.android.tools.r8.code.InvokeVirtual;
import com.android.tools.r8.code.InvokeVirtualRange;
import com.android.tools.r8.code.Iput;
import com.android.tools.r8.code.IputBoolean;
import com.android.tools.r8.code.IputByte;
import com.android.tools.r8.code.IputChar;
import com.android.tools.r8.code.IputObject;
import com.android.tools.r8.code.IputShort;
import com.android.tools.r8.code.IputWide;
import com.android.tools.r8.code.Sget;
import com.android.tools.r8.code.SgetBoolean;
import com.android.tools.r8.code.SgetByte;
import com.android.tools.r8.code.SgetChar;
import com.android.tools.r8.code.SgetObject;
import com.android.tools.r8.code.SgetShort;
import com.android.tools.r8.code.SgetWide;
import com.android.tools.r8.code.Sput;
import com.android.tools.r8.code.SputBoolean;
import com.android.tools.r8.code.SputByte;
import com.android.tools.r8.code.SputChar;
import com.android.tools.r8.code.SputObject;
import com.android.tools.r8.code.SputShort;
import com.android.tools.r8.code.SputWide;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.ClassNaming;
import com.android.tools.r8.naming.MemberNaming;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.ProguardMapReader;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DexInspector {

  private final DexApplication application;
  private final DexItemFactory dexItemFactory;
  private final ClassNameMapper mapping;
  private final BiMap<String, String> originalToObfuscatedMapping;

  private final InstructionSubjectFactory factory = new InstructionSubjectFactory();

  public static MethodSignature MAIN =
      new MethodSignature("main", "void", new String[]{"java.lang.String[]"});

  public DexInspector(Path file, String mappingFile) throws IOException, ExecutionException {
    this(Collections.singletonList(file), mappingFile);
  }

  public DexInspector(Path file) throws IOException, ExecutionException {
    this(Collections.singletonList(file), null);
  }

  public DexInspector(List<Path> files) throws IOException, ExecutionException {
    this(files, null);
  }

  public DexInspector(List<Path> files, String mappingFile)
      throws IOException, ExecutionException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    if (mappingFile != null) {
      this.mapping = ProguardMapReader.mapperFromFile(Paths.get(mappingFile));
      originalToObfuscatedMapping = this.mapping.getObfuscatedToOriginalMapping().inverse();
    } else {
      this.mapping = null;
      originalToObfuscatedMapping = null;
    }
    Timing timing = new Timing("DexInspector");
    InternalOptions options = new InternalOptions();
    dexItemFactory = options.itemFactory;
    AndroidApp input = AndroidApp.fromProgramFiles(files);
    application = new ApplicationReader(input, options, timing).read(executor);
    executor.shutdown();
  }

  public DexInspector(AndroidApp app) throws IOException, ExecutionException {
    this(new ApplicationReader(app, new InternalOptions(), new Timing("DexInspector")).read());
  }

  public DexInspector(DexApplication application) {
    dexItemFactory = application.dexItemFactory;
    this.application = application;
    this.mapping = application.getProguardMap();
    originalToObfuscatedMapping =
        mapping == null ? null : mapping.getObfuscatedToOriginalMapping().inverse();
  }

  public DexItemFactory getFactory() {
    return dexItemFactory;
  }

  private DexType toDexType(String string) {
    return dexItemFactory.createType(DescriptorUtils.javaTypeToDescriptor(string));
  }

  private static <S, T extends Subject> void forAll(S[] items,
      BiFunction<S, FoundClassSubject, ? extends T> constructor,
      FoundClassSubject clazz,
      Consumer<T> consumer) {
    for (S item : items) {
      consumer.accept(constructor.apply(item, clazz));
    }
  }

  private static <S, T extends Subject> void forAll(Iterable<S> items, Function<S, T> constructor,
      Consumer<T> consumer) {
    for (S item : items) {
      consumer.accept(constructor.apply(item));
    }
  }

  public ClassSubject clazz(Class clazz) {
    return clazz(clazz.getTypeName());
  }

  public ClassSubject clazz(String name) {
    ClassNaming naming = null;
    if (mapping != null) {
      String obfuscated = originalToObfuscatedMapping.get(name);
      if (obfuscated != null) {
        naming = mapping.getClassNaming(obfuscated);
        name = obfuscated;
      }
    }
    DexClass clazz = application.definitionFor(toDexType(name));
    if (clazz == null) {
      return new AbsentClassSubject();
    }
    return new FoundClassSubject(clazz, naming);
  }

  public void forAllClasses(Consumer<FoundClassSubject> inspection) {
    forAll(application.classes(), clazz -> {
      ClassNaming naming = null;
      if (mapping != null) {
        String obfuscated = originalToObfuscatedMapping.get(clazz.type.toSourceString());
        if (obfuscated != null) {
          naming = mapping.getClassNaming(obfuscated);
        }
      }
      return new FoundClassSubject(clazz, naming);
    }, inspection);
  }

  public MethodSubject method(Method method) {
    ClassSubject clazz = clazz(method.getDeclaringClass());
    if (!clazz.isPresent()) {
      return new AbsentMethodSubject();
    }
    return clazz.method(method);
  }

  private String getObfuscatedTypeName(String originalTypeName) {
    String obfuscatedType = null;
    if (mapping != null) {
      obfuscatedType = originalToObfuscatedMapping.get(originalTypeName);
    }
    obfuscatedType = obfuscatedType == null ? originalTypeName : obfuscatedType;
    return obfuscatedType;
  }

  public abstract class Subject {

    public abstract boolean isPresent();
  }

  public abstract class AnnotationSubject extends Subject {

    public abstract DexEncodedAnnotation getAnnotation();
  }

  public class FoundAnnotationSubject extends AnnotationSubject {

    private final DexAnnotation annotation;

    private FoundAnnotationSubject(DexAnnotation annotation) {
      this.annotation = annotation;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public DexEncodedAnnotation getAnnotation() {
      return annotation.annotation;
    }
  }

  public class AbsentAnnotationSubject extends AnnotationSubject {

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public DexEncodedAnnotation getAnnotation() {
      throw new UnsupportedOperationException();
    }
  }


  public abstract class ClassSubject extends Subject {

    public abstract void forAllMethods(Consumer<FoundMethodSubject> inspection);

    public MethodSubject method(Method method) {
      List<String> parameters = new ArrayList<>();
      for (Class<?> parameterType : method.getParameterTypes()) {
        parameters.add(parameterType.getTypeName());
      }
      return method(method.getReturnType().getTypeName(), method.getName(), parameters);
    }

    public abstract MethodSubject method(String returnType, String name, List<String> parameters);

    public MethodSubject method(MethodSignature signature) {
      return method(signature.type, signature.name, ImmutableList.copyOf(signature.parameters));
    }

    public abstract void forAllFields(Consumer<FoundFieldSubject> inspection);

    public abstract FieldSubject field(String type, String name);

    public abstract boolean isAbstract();

    public String dumpMethods() {
      StringBuilder dump = new StringBuilder();
      forAllMethods((FoundMethodSubject method) ->
          dump.append(method.getMethod().toString())
              .append(method.getMethod().codeToString()));
      return dump.toString();
    }

    public abstract DexClass getDexClass();

    public abstract AnnotationSubject annotation(String name);

    public abstract String getOriginalDescriptor();

    public abstract String getFinalDescriptor();

    public abstract boolean isRenamed();
  }

  private class AbsentClassSubject extends ClassSubject {

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public void forAllMethods(Consumer<FoundMethodSubject> inspection) {
    }

    @Override
    public MethodSubject method(String returnType, String name, List<String> parameters) {
      return new AbsentMethodSubject();
    }

    @Override
    public void forAllFields(Consumer<FoundFieldSubject> inspection) {
    }

    @Override
    public FieldSubject field(String type, String name) {
      return new AbsentFieldSubject();
    }

    @Override
    public boolean isAbstract() {
      return false;
    }

    @Override
    public DexClass getDexClass() {
      return null;
    }

    @Override
    public AnnotationSubject annotation(String name) {
      return new AbsentAnnotationSubject();
    }

    @Override
    public String getOriginalDescriptor() {
      return null;
    }

    @Override
    public String getFinalDescriptor() {
      return null;
    }

    @Override
    public boolean isRenamed() {
      return false;
    }
  }

  public class FoundClassSubject extends ClassSubject {

    private final DexClass dexClass;
    private final ClassNaming naming;

    private FoundClassSubject(DexClass dexClass, ClassNaming naming) {
      this.dexClass = dexClass;
      this.naming = naming;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public void forAllMethods(Consumer<FoundMethodSubject> inspection) {
      forAll(dexClass.directMethods(), FoundMethodSubject::new, this, inspection);
      forAll(dexClass.virtualMethods(), FoundMethodSubject::new, this, inspection);
    }

    @Override
    public MethodSubject method(String returnType, String name, List<String> parameters) {
      DexType[] parameterTypes = new DexType[parameters.size()];
      for (int i = 0; i < parameters.size(); i++) {
        parameterTypes[i] = toDexType(getObfuscatedTypeName(parameters.get(i)));
      }
      DexProto proto = dexItemFactory.createProto(toDexType(getObfuscatedTypeName(returnType)),
          parameterTypes);
      if (naming != null) {
        String[] parameterStrings = new String[parameterTypes.length];
        Signature signature = new MethodSignature(name, returnType,
            parameters.toArray(parameterStrings));
        MemberNaming methodNaming = naming.lookupByOriginalSignature(signature);
        if (methodNaming != null) {
          name = methodNaming.getRenamedName();
        }
      }
      DexMethod dexMethod =
          dexItemFactory.createMethod(dexClass.type, proto, dexItemFactory.createString(name));
      DexEncodedMethod encoded = findMethod(dexClass.directMethods(), dexMethod);
      if (encoded == null) {
        encoded = findMethod(dexClass.virtualMethods(), dexMethod);
      }
      return encoded == null ? new AbsentMethodSubject() : new FoundMethodSubject(encoded, this);
    }

    private DexEncodedMethod findMethod(DexEncodedMethod[] methods, DexMethod dexMethod) {
      for (DexEncodedMethod method : methods) {
        if (method.method.equals(dexMethod)) {
          return method;
        }
      }
      return null;
    }

    @Override
    public void forAllFields(Consumer<FoundFieldSubject> inspection) {
      forAll(dexClass.staticFields(), FoundFieldSubject::new, this, inspection);
      forAll(dexClass.instanceFields(), FoundFieldSubject::new, this, inspection);
    }

    @Override
    public FieldSubject field(String type, String name) {
      String obfuscatedType = getObfuscatedTypeName(type);
      MemberNaming fieldNaming = null;
      if (naming != null) {
        fieldNaming = naming.lookupByOriginalSignature(
            new FieldSignature(name, type));
      }
      String obfuscatedName = fieldNaming == null ? name : fieldNaming.getRenamedName();

      DexField field = dexItemFactory.createField(dexClass.type,
          toDexType(obfuscatedType), dexItemFactory.createString(obfuscatedName));
      DexEncodedField encoded = findField(dexClass.staticFields(), field);
      if (encoded == null) {
        encoded = findField(dexClass.instanceFields(), field);
      }
      return encoded == null ? new AbsentFieldSubject() : new FoundFieldSubject(encoded, this);
    }

    @Override
    public boolean isAbstract() {
      return dexClass.accessFlags.isAbstract();
    }

    private DexEncodedField findField(DexEncodedField[] fields, DexField dexField) {
      for (DexEncodedField field : fields) {
        if (field.field.equals(dexField)) {
          return field;
        }
      }
      return null;
    }

    @Override
    public DexClass getDexClass() {
      return dexClass;
    }

    @Override
    public AnnotationSubject annotation(String name) {
      DexAnnotation annotation = findAnnotation(name);
      return annotation == null
          ? new AbsentAnnotationSubject()
          : new FoundAnnotationSubject(annotation);
    }

    private DexAnnotation findAnnotation(String name) {
      for (DexAnnotation annotation : dexClass.annotations.annotations) {
        DexType type = annotation.annotation.type;
        String original = mapping == null ? type.toSourceString() : mapping.originalNameOf(type);
        if (original.equals(name)) {
          return annotation;
        }
      }
      return null;
    }

    @Override
    public String getOriginalDescriptor() {
      if (naming != null) {
        return DescriptorUtils.javaTypeToDescriptor(naming.originalName);
      } else {
        return getFinalDescriptor();
      }
    }

    @Override
    public String getFinalDescriptor() {
      return dexClass.type.descriptor.toString();
    }

    @Override
    public boolean isRenamed() {
      return naming == null || !getFinalDescriptor().equals(getOriginalDescriptor());
    }

    @Override
    public String toString() {
      return dexClass.toSourceString();
    }
  }

  public abstract class MemberSubject extends Subject {

    public abstract boolean hasAll(DexAccessFlags flags);

    public abstract boolean hasNone(DexAccessFlags flags);

    public abstract boolean isStatic();

    public abstract boolean isFinal();

    public abstract Signature getOriginalSignature();

    public abstract Signature getFinalSignature();
  }

  public abstract class MethodSubject extends MemberSubject {

    public abstract boolean isAbstract();

    public abstract boolean isBridge();

    public abstract DexEncodedMethod getMethod();

    public Iterator<InstructionSubject> iterateInstructions() {
      return null;
    }

    public <T extends InstructionSubject> Iterator<T> iterateInstructions(
        Predicate<InstructionSubject> filter) {
      return null;
    }

    public abstract boolean isRenamed();
  }

  public class AbsentMethodSubject extends MethodSubject {

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public boolean isRenamed() {
      return false;
    }

    @Override
    public boolean hasAll(DexAccessFlags flags) {
      return false;
    }

    @Override
    public boolean hasNone(DexAccessFlags flags) {
      return true;
    }

    @Override
    public boolean isStatic() {
      return false;
    }

    @Override
    public boolean isFinal() {
      return false;
    }

    @Override
    public boolean isAbstract() {
      return false;
    }

    @Override
    public boolean isBridge() {
      return false;
    }

    @Override
    public DexEncodedMethod getMethod() {
      return null;
    }

    @Override
    public Signature getOriginalSignature() {
      return null;
    }

    @Override
    public Signature getFinalSignature() {
      return null;
    }
  }

  public class FoundMethodSubject extends MethodSubject {

    private final FoundClassSubject clazz;
    private final DexEncodedMethod dexMethod;

    public FoundMethodSubject(DexEncodedMethod encoded, FoundClassSubject clazz) {
      this.clazz = clazz;
      this.dexMethod = encoded;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public boolean isRenamed() {
      return clazz.naming == null || !getFinalSignature().name.equals(getOriginalSignature().name);
    }

    @Override
    public boolean hasAll(DexAccessFlags flags) {
      return dexMethod.accessFlags.containsAllOf(flags);
    }

    @Override
    public boolean hasNone(DexAccessFlags flags) {
      return dexMethod.accessFlags.containsNoneOf(flags);
    }

    @Override
    public boolean isStatic() {
      return dexMethod.accessFlags.isStatic();
    }

    @Override
    public boolean isFinal() {
      return dexMethod.accessFlags.isFinal();
    }

    @Override
    public boolean isAbstract() {
      return dexMethod.accessFlags.isAbstract();
    }

    @Override
    public boolean isBridge() {
      return dexMethod.accessFlags.isBridge();
    }

    @Override
    public DexEncodedMethod getMethod() {
      return dexMethod;
    }

    @Override
    public MethodSignature getOriginalSignature() {
      MethodSignature signature = getFinalSignature();
      return clazz.naming != null ?
          (MethodSignature) clazz.naming.lookup(signature).getOriginalSignature() :
          signature;
    }

    @Override
    public MethodSignature getFinalSignature() {
      return MemberNaming.MethodSignature.fromDexMethod(dexMethod.method);
    }

    @Override
    public Iterator<InstructionSubject> iterateInstructions() {
      return new InstructionIterator(this);
    }

    @Override
    public <T extends InstructionSubject> Iterator<T> iterateInstructions(
        Predicate<InstructionSubject> filter) {
      return new FilteredInstructionIterator<>(this, filter);
    }

    @Override
    public String toString() {
      return dexMethod.toSourceString();
    }
  }

  public abstract class FieldSubject extends MemberSubject {

    public abstract DexEncodedField getField();
  }

  public class AbsentFieldSubject extends FieldSubject {

    @Override
    public boolean hasAll(DexAccessFlags flags) {
      return false;
    }

    @Override
    public boolean hasNone(DexAccessFlags flags) {
      return true;
    }

    @Override
    public boolean isStatic() {
      return false;
    }

    @Override
    public boolean isFinal() {
      return false;
    }

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public Signature getOriginalSignature() {
      return null;
    }

    @Override
    public Signature getFinalSignature() {
      return null;
    }

    @Override
    public DexEncodedField getField() {
      return null;
    }
  }

  public class FoundFieldSubject extends FieldSubject {

    private final FoundClassSubject clazz;
    private final DexEncodedField dexField;

    public FoundFieldSubject(DexEncodedField dexField, FoundClassSubject clazz) {
      this.clazz = clazz;
      this.dexField = dexField;
    }

    @Override
    public boolean hasAll(DexAccessFlags flags) {
      return dexField.accessFlags.containsAllOf(flags);
    }

    @Override
    public boolean hasNone(DexAccessFlags flags) {
      return dexField.accessFlags.containsNoneOf(flags);
    }

    @Override
    public boolean isStatic() {
      return dexField.accessFlags.isStatic();
    }

    @Override
    public boolean isFinal() {
      return dexField.accessFlags.isFinal();
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    public TypeSubject type() {
      return new TypeSubject(dexField.field.type);
    }

    @Override
    public FieldSignature getOriginalSignature() {
      FieldSignature signature = getFinalSignature();
      return clazz.naming != null ?
          (FieldSignature) clazz.naming.lookup(signature).getOriginalSignature() :
          signature;
    }

    @Override
    public FieldSignature getFinalSignature() {
      return MemberNaming.FieldSignature.fromDexField(dexField.field);
    }

    @Override
    public DexEncodedField getField() {
      return dexField;
    }
  }

  public class TypeSubject extends Subject {

    private final DexType dexType;

    public TypeSubject(DexType dexType) {
      this.dexType = dexType;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    public boolean is(String type) {
      return dexType.equals(toDexType(type));
    }

    public String toString() {
      return dexType.toSourceString();
    }
  }

  private class InstructionSubjectFactory {

    InstructionSubject create(Instruction instruction) {
      if (isInvoke(instruction)) {
        return new InvokeInstructionSubject(this, instruction);
      } else if (isFieldAccess(instruction)) {
        return new FieldAccessInstructionSubject(this, instruction);
      } else {
        return new InstructionSubject(this, instruction);
      }
    }

    boolean isInvoke(Instruction instruction) {
      return isInvokeVirtual(instruction)
          || isInvokeInterface(instruction)
          || isInvokeDirect(instruction)
          || isInvokeSuper(instruction)
          || isInvokeStatic(instruction);
    }

    boolean isInvokeVirtual(Instruction instruction) {
      return instruction instanceof InvokeVirtual || instruction instanceof InvokeVirtualRange;
    }

    boolean isInvokeInterface(Instruction instruction) {
      return instruction instanceof InvokeInterface || instruction instanceof InvokeInterfaceRange;
    }

    boolean isInvokeDirect(Instruction instruction) {
      return instruction instanceof InvokeDirect || instruction instanceof InvokeDirectRange;
    }

    boolean isInvokeSuper(Instruction instruction) {
      return instruction instanceof InvokeSuper || instruction instanceof InvokeSuperRange;
    }

    boolean isInvokeStatic(Instruction instruction) {
      return instruction instanceof InvokeStatic || instruction instanceof InvokeStaticRange;
    }

    boolean isFieldAccess(Instruction instruction) {
      return isInstanceGet(instruction)
          || isInstancePut(instruction)
          || isStaticGet(instruction)
          || isStaticSet(instruction);
    }

    boolean isInstanceGet(Instruction instruction) {
      return instruction instanceof Iget
          || instruction instanceof IgetBoolean
          || instruction instanceof IgetByte
          || instruction instanceof IgetShort
          || instruction instanceof IgetChar
          || instruction instanceof IgetWide
          || instruction instanceof IgetObject;
    }

    boolean isInstancePut(Instruction instruction) {
      return instruction instanceof Iput
          || instruction instanceof IputBoolean
          || instruction instanceof IputByte
          || instruction instanceof IputShort
          || instruction instanceof IputChar
          || instruction instanceof IputWide
          || instruction instanceof IputObject;
    }

    boolean isStaticGet(Instruction instruction) {
      return instruction instanceof Sget
          || instruction instanceof SgetBoolean
          || instruction instanceof SgetByte
          || instruction instanceof SgetShort
          || instruction instanceof SgetChar
          || instruction instanceof SgetWide
          || instruction instanceof SgetObject;
    }

    boolean isStaticSet(Instruction instruction) {
      return instruction instanceof Sput
          || instruction instanceof SputBoolean
          || instruction instanceof SputByte
          || instruction instanceof SputShort
          || instruction instanceof SputChar
          || instruction instanceof SputWide
          || instruction instanceof SputObject;
    }
  }

  public class InstructionSubject {

    protected final InstructionSubjectFactory factory;
    protected final Instruction instruction;

    protected InstructionSubject(InstructionSubjectFactory factory, Instruction instruction) {
      this.factory = factory;
      this.instruction = instruction;
    }

    public boolean isInvoke() {
      return factory.isInvoke(instruction);
    }

    public boolean isFieldAccess() {
      return factory.isFieldAccess(instruction);
    }

    public boolean isInvokeVirtual() {
      return factory.isInvokeVirtual(instruction);
    }

    public boolean isInvokeInterface() {
      return factory.isInvokeInterface(instruction);
    }

    public boolean isInvokeDirect() {
      return factory.isInvokeDirect(instruction);
    }

    public boolean isInvokeSuper() {
      return factory.isInvokeSuper(instruction);
    }

    public boolean isInvokeStatic() {
      return factory.isInvokeStatic(instruction);
    }

    boolean isFieldAccess(Instruction instruction) {
      return factory.isFieldAccess(instruction);
    }
  }

  public class InvokeInstructionSubject extends InstructionSubject {

    InvokeInstructionSubject(InstructionSubjectFactory factory, Instruction instruction) {
      super(factory, instruction);
      assert isInvoke();
    }

    public TypeSubject holder() {
      return new TypeSubject(invokedMethod().getHolder());
    }

    public DexMethod invokedMethod() {
      if (instruction instanceof InvokeVirtual) {
        return ((InvokeVirtual) instruction).getMethod();
      }
      if (instruction instanceof InvokeVirtualRange) {
        return ((InvokeVirtualRange) instruction).getMethod();
      }
      if (instruction instanceof InvokeInterface) {
        return ((InvokeInterface) instruction).getMethod();
      }
      if (instruction instanceof InvokeInterfaceRange) {
        return ((InvokeInterfaceRange) instruction).getMethod();
      }
      if (instruction instanceof InvokeDirect) {
        return ((InvokeDirect) instruction).getMethod();
      }
      if (instruction instanceof InvokeDirectRange) {
        return ((InvokeDirectRange) instruction).getMethod();
      }
      if (instruction instanceof InvokeSuper) {
        return ((InvokeSuper) instruction).getMethod();
      }
      if (instruction instanceof InvokeSuperRange) {
        return ((InvokeSuperRange) instruction).getMethod();
      }
      if (instruction instanceof InvokeDirect) {
        return ((InvokeDirect) instruction).getMethod();
      }
      if (instruction instanceof InvokeDirectRange) {
        return ((InvokeDirectRange) instruction).getMethod();
      }
      if (instruction instanceof InvokeStatic) {
        return ((InvokeStatic) instruction).getMethod();
      }
      if (instruction instanceof InvokeStaticRange) {
        return ((InvokeStaticRange) instruction).getMethod();
      }
      assert false;
      return null;
    }
  }

  public class FieldAccessInstructionSubject extends InstructionSubject {

    FieldAccessInstructionSubject(InstructionSubjectFactory factory, Instruction instruction) {
      super(factory, instruction);
      assert isFieldAccess();
    }

    public TypeSubject holder() {
      return new TypeSubject(accessedField().getHolder());
    }

    public DexField accessedField() {
      if (instruction instanceof Iget) {
        return ((Iget) instruction).getField();
      }
      if (instruction instanceof IgetBoolean) {
        return ((IgetBoolean) instruction).getField();
      }
      if (instruction instanceof IgetByte) {
        return ((IgetByte) instruction).getField();
      }
      if (instruction instanceof IgetShort) {
        return ((IgetShort) instruction).getField();
      }
      if (instruction instanceof IgetChar) {
        return ((IgetChar) instruction).getField();
      }
      if (instruction instanceof IgetWide) {
        return ((IgetWide) instruction).getField();
      }
      if (instruction instanceof IgetObject) {
        return ((IgetObject) instruction).getField();
      }
      if (instruction instanceof Iput) {
        return ((Iput) instruction).getField();
      }
      if (instruction instanceof IputBoolean) {
        return ((IputBoolean) instruction).getField();
      }
      if (instruction instanceof IputByte) {
        return ((IputByte) instruction).getField();
      }
      if (instruction instanceof IputShort) {
        return ((IputShort) instruction).getField();
      }
      if (instruction instanceof IputChar) {
        return ((IputChar) instruction).getField();
      }
      if (instruction instanceof IputWide) {
        return ((IputWide) instruction).getField();
      }
      if (instruction instanceof IputObject) {
        return ((IputObject) instruction).getField();
      }
      if (instruction instanceof Sget) {
        return ((Sget) instruction).getField();
      }
      if (instruction instanceof SgetBoolean) {
        return ((SgetBoolean) instruction).getField();
      }
      if (instruction instanceof SgetByte) {
        return ((SgetByte) instruction).getField();
      }
      if (instruction instanceof SgetShort) {
        return ((SgetShort) instruction).getField();
      }
      if (instruction instanceof SgetChar) {
        return ((SgetChar) instruction).getField();
      }
      if (instruction instanceof SgetWide) {
        return ((SgetWide) instruction).getField();
      }
      if (instruction instanceof SgetObject) {
        return ((SgetObject) instruction).getField();
      }
      if (instruction instanceof Sput) {
        return ((Sput) instruction).getField();
      }
      if (instruction instanceof SputBoolean) {
        return ((SputBoolean) instruction).getField();
      }
      if (instruction instanceof SputByte) {
        return ((SputByte) instruction).getField();
      }
      if (instruction instanceof SputShort) {
        return ((SputShort) instruction).getField();
      }
      if (instruction instanceof SputChar) {
        return ((SputChar) instruction).getField();
      }
      if (instruction instanceof SputWide) {
        return ((SputWide) instruction).getField();
      }
      if (instruction instanceof SputObject) {
        return ((SputObject) instruction).getField();
      }
      assert false;
      return null;
    }
  }

  private class InstructionIterator implements Iterator<InstructionSubject> {

    private final DexCode code;
    private int index;

    InstructionIterator(MethodSubject method) {
      assert method.isPresent();
      this.code = method.getMethod().getCode().asDexCode();
      this.index = 0;
    }

    @Override
    public boolean hasNext() {
      return index < code.instructions.length;
    }

    @Override
    public InstructionSubject next() {
      if (index == code.instructions.length) {
        throw new NoSuchElementException();
      }
      return factory.create(code.instructions[index++]);
    }
  }

  private class FilteredInstructionIterator<T extends InstructionSubject> implements Iterator<T> {

    private final InstructionIterator iterator;
    private final Predicate<InstructionSubject> predicate;
    private InstructionSubject pendingNext = null;

    FilteredInstructionIterator(MethodSubject method, Predicate<InstructionSubject> predicate) {
      this.iterator = new InstructionIterator(method);
      this.predicate = predicate;
      hasNext();
    }

    @Override
    public boolean hasNext() {
      if (pendingNext == null) {
        while (iterator.hasNext()) {
          pendingNext = iterator.next();
          if (predicate.test(pendingNext)) {
            break;
          }
          pendingNext = null;
        }
      }
      return pendingNext != null;
    }

    @Override
    public T next() {
      hasNext();
      if (pendingNext == null) {
        throw new NoSuchElementException();
      }
      // We cannot tell if the provided predicate will only match instruction subjects of type T.
      @SuppressWarnings("unchecked")
      T result = (T) pendingNext;
      pendingNext = null;
      return result;
    }
  }
}
