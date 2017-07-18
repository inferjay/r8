// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexDebugEvent.AdvanceLine;
import com.android.tools.r8.graph.DexDebugEvent.AdvancePC;
import com.android.tools.r8.graph.DexDebugEvent.Default;
import com.android.tools.r8.graph.DexDebugEvent.EndLocal;
import com.android.tools.r8.graph.DexDebugEvent.RestartLocal;
import com.android.tools.r8.graph.DexDebugEvent.SetEpilogueBegin;
import com.android.tools.r8.graph.DexDebugEvent.SetFile;
import com.android.tools.r8.graph.DexDebugEvent.SetPrologueEnd;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.naming.NamingLens;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DexItemFactory {

  private final Map<String, DexString> strings = new HashMap<>();
  private final Map<DexType, DexType> types = new HashMap<>();
  private final Map<DexField, DexField> fields = new HashMap<>();
  private final Map<DexProto, DexProto> protos = new HashMap<>();
  private final Map<DexMethod, DexMethod> methods = new HashMap<>();
  private final Map<DexCallSite, DexCallSite> callSites = new HashMap<>();
  private final Map<DexMethodHandle, DexMethodHandle> methodHandles = new HashMap<>();

  // DexDebugEvent Canonicalization.
  private final Int2ObjectMap<AdvanceLine> advanceLines = new Int2ObjectOpenHashMap<>();
  private final Int2ObjectMap<AdvancePC> advancePCs = new Int2ObjectOpenHashMap<>();
  private final Int2ObjectMap<Default> defaults = new Int2ObjectOpenHashMap<>();
  private final Int2ObjectMap<EndLocal> endLocals = new Int2ObjectOpenHashMap<>();
  private final Int2ObjectMap<RestartLocal> restartLocals = new Int2ObjectOpenHashMap<>();
  private final SetEpilogueBegin setEpilogueBegin = new SetEpilogueBegin();
  private final SetPrologueEnd setPrologueEnd = new SetPrologueEnd();
  private final Map<DexString, SetFile> setFiles = new HashMap<>();

  boolean sorted = false;

  public static final DexType catchAllType = new DexType(new DexString("CATCH_ALL"));
  private static final Set<DexItem> internalSentinels = ImmutableSet.of(catchAllType);

  public DexString booleanDescriptor = createString("Z");
  public DexString byteDescriptor = createString("B");
  public DexString charDescriptor = createString("C");
  public DexString doubleDescriptor = createString("D");
  public DexString floatDescriptor = createString("F");
  public DexString intDescriptor = createString("I");
  public DexString longDescriptor = createString("J");
  public DexString shortDescriptor = createString("S");
  public DexString voidDescriptor = createString("V");

  public DexString boxedBooleanDescriptor = createString("Ljava/lang/Boolean;");
  public DexString boxedByteDescriptor = createString("Ljava/lang/Byte;");
  public DexString boxedCharDescriptor = createString("Ljava/lang/Character;");
  public DexString boxedDoubleDescriptor = createString("Ljava/lang/Double;");
  public DexString boxedFloatDescriptor = createString("Ljava/lang/Float;");
  public DexString boxedIntDescriptor = createString("Ljava/lang/Integer;");
  public DexString boxedLongDescriptor = createString("Ljava/lang/Long;");
  public DexString boxedShortDescriptor = createString("Ljava/lang/Short;");
  public DexString boxedNumberDescriptor = createString("Ljava/lang/Number;");

  public DexString unboxBooleanMethodName = createString("booleanValue");
  public DexString unboxByteMethodName = createString("byteValue");
  public DexString unboxCharMethodName = createString("charValue");
  public DexString unboxShortMethodName = createString("shortValue");
  public DexString unboxIntMethodName = createString("intValue");
  public DexString unboxLongMethodName = createString("longValue");
  public DexString unboxFloatMethodName = createString("floatValue");
  public DexString unboxDoubleMethodName = createString("doubleValue");

  public DexString valueOfMethodName = createString("valueOf");

  public DexString getClassMethodName = createString("getClass");
  public DexString ordinalMethodName = createString("ordinal");
  public final DexString desiredAssertionStatusMethodName = createString("desiredAssertionStatus");
  public final DexString assertionsDisabled = createString("$assertionsDisabled");

  public DexString stringDescriptor = createString("Ljava/lang/String;");
  public DexString objectDescriptor = createString("Ljava/lang/Object;");
  public DexString classDescriptor = createString("Ljava/lang/Class;");
  public DexString enumDescriptor = createString("Ljava/lang/Enum;");
  public DexString annotationDescriptor = createString("Ljava/lang/annotation/Annotation;");
  public DexString throwableDescriptor = createString("Ljava/lang/Throwable;");
  public DexString objectsDescriptor = createString("Ljava/util/Objects;");

  public DexString constructorMethodName = createString(Constants.INSTANCE_INITIALIZER_NAME);
  public DexString classConstructorMethodName = createString(Constants.CLASS_INITIALIZER_NAME);

  public DexString thisName = createString("this");

  private DexString charArrayDescriptor = createString("[C");
  private DexType charArrayType = createType(charArrayDescriptor);
  public DexString throwableArrayDescriptor = createString("[Ljava/lang/Throwable;");

  public DexType booleanType = createType(booleanDescriptor);
  public DexType byteType = createType(byteDescriptor);
  public DexType charType = createType(charDescriptor);
  public DexType doubleType = createType(doubleDescriptor);
  public DexType floatType = createType(floatDescriptor);
  public DexType intType = createType(intDescriptor);
  public DexType longType = createType(longDescriptor);
  public DexType shortType = createType(shortDescriptor);
  public DexType voidType = createType(voidDescriptor);

  public DexType boxedBooleanType = createType(boxedBooleanDescriptor);
  public DexType boxedByteType = createType(boxedByteDescriptor);
  public DexType boxedCharType = createType(boxedCharDescriptor);
  public DexType boxedDoubleType = createType(boxedDoubleDescriptor);
  public DexType boxedFloatType = createType(boxedFloatDescriptor);
  public DexType boxedIntType = createType(boxedIntDescriptor);
  public DexType boxedLongType = createType(boxedLongDescriptor);
  public DexType boxedShortType = createType(boxedShortDescriptor);
  public DexType boxedNumberType = createType(boxedNumberDescriptor);

  public DexType stringType = createType(stringDescriptor);
  public DexType objectType = createType(objectDescriptor);
  public DexType enumType = createType(enumDescriptor);
  public DexType annotationType = createType(annotationDescriptor);
  public DexType throwableType = createType(throwableDescriptor);

  public DexType stringBuilderType = createType("Ljava/lang/StringBuilder;");
  public DexType stringBufferType = createType("Ljava/lang/StringBuffer;");

  public StringBuildingMethods stringBuilderMethods = new StringBuildingMethods(stringBuilderType);
  public StringBuildingMethods stringBufferMethods = new StringBuildingMethods(stringBufferType);
  public ObjectsMethods objectsMethods = new ObjectsMethods();
  public ObjectMethods objectMethods = new ObjectMethods();
  public LongMethods longMethods = new LongMethods();
  public ThrowableMethods throwableMethods = new ThrowableMethods();
  public ClassMethods classMethods = new ClassMethods();

  // Dex system annotations.
  // See https://source.android.com/devices/tech/dalvik/dex-format.html#system-annotation
  public final DexType annotationDefault = createType("Ldalvik/annotation/AnnotationDefault;");
  public final DexType annotationEnclosingClass = createType("Ldalvik/annotation/EnclosingClass;");
  public final DexType annotationEnclosingMethod = createType(
      "Ldalvik/annotation/EnclosingMethod;");
  public final DexType annotationInnerClass = createType("Ldalvik/annotation/InnerClass;");
  public final DexType annotationMemberClasses = createType("Ldalvik/annotation/MemberClasses;");
  public final DexType annotationMethodParameters = createType(
      "Ldalvik/annotation/MethodParameters;");
  public final DexType annotationSignature = createType("Ldalvik/annotation/Signature;");
  public final DexType annotationSourceDebugExtension = createType(
      "Ldalvik/annotation/SourceDebugExtension;");
  public final DexType annotationThrows = createType("Ldalvik/annotation/Throws;");

  public void clearSubtypeInformation() {
    types.values().forEach(DexType::clearSubtypeInformation);
  }

  public class LongMethods {

    public DexMethod compare;

    private LongMethods() {
      compare = createMethod(boxedLongDescriptor,
          createString("compare"), intDescriptor, new DexString[]{longDescriptor, longDescriptor});
    }
  }

  public class ThrowableMethods {

    public final DexMethod addSuppressed;
    public final DexMethod getSuppressed;

    private ThrowableMethods() {
      addSuppressed = createMethod(throwableDescriptor,
          createString("addSuppressed"), voidDescriptor, new DexString[]{throwableDescriptor});
      getSuppressed = createMethod(throwableDescriptor,
          createString("getSuppressed"), throwableArrayDescriptor, DexString.EMPTY_ARRAY);
    }
  }

  public class ObjectMethods {

    public DexMethod getClass;

    private ObjectMethods() {
      getClass = createMethod(objectsDescriptor,
          getClassMethodName, classDescriptor, new DexString[]{});
    }
  }

  public class ObjectsMethods {

    public DexMethod requireNonNull;

    private ObjectsMethods() {
      requireNonNull = createMethod(objectsDescriptor,
          createString("requireNonNull"), objectDescriptor, new DexString[]{objectDescriptor});
    }
  }

  public class ClassMethods {

    public DexMethod desiredAssertionStatus;

    private ClassMethods() {
      desiredAssertionStatus = createMethod(classDescriptor,
          desiredAssertionStatusMethodName, booleanDescriptor, new DexString[]{});
    }
  }


  public class StringBuildingMethods {

    public DexMethod appendBoolean;
    public DexMethod appendChar;
    public DexMethod appendCharArray;
    public DexMethod appendSubCharArray;
    public DexMethod appendCharSequence;
    public DexMethod appendSubCharSequence;
    public DexMethod appendInt;
    public DexMethod appendDouble;
    public DexMethod appendFloat;
    public DexMethod appendLong;
    public DexMethod appendObject;
    public DexMethod appendString;
    public DexMethod appendStringBuffer;
    public DexMethod toString;

    private StringBuildingMethods(DexType receiver) {
      DexType sbufType = createType(createString("Ljava/lang/StringBuffer;"));
      DexType charSequenceType = createType(createString("Ljava/lang/CharSequence;"));
      DexString append = createString("append");
      DexString toStringMethodName = createString("toString");


      appendBoolean = createMethod(receiver, createProto(receiver, booleanType), append);
      appendChar = createMethod(receiver, createProto(receiver, charType), append);
      appendCharArray = createMethod(receiver, createProto(receiver, charArrayType), append);
      appendSubCharArray =
          createMethod(receiver, createProto(receiver, charArrayType, intType, intType), append);
      appendCharSequence = createMethod(receiver, createProto(receiver, charSequenceType), append);
      appendSubCharSequence =
          createMethod(receiver, createProto(receiver, charSequenceType, intType, intType), append);
      appendInt = createMethod(receiver, createProto(receiver, intType), append);
      appendDouble = createMethod(receiver, createProto(receiver, doubleType), append);
      appendFloat = createMethod(receiver, createProto(receiver, floatType), append);
      appendLong = createMethod(receiver, createProto(receiver, longType), append);
      appendObject = createMethod(receiver, createProto(receiver, objectType), append);
      appendString = createMethod(receiver, createProto(receiver, stringType), append);
      appendStringBuffer = createMethod(receiver, createProto(receiver, sbufType), append);
      toString = createMethod(receiver, createProto(stringType), toStringMethodName);
    }

    public void forEachAppendMethod(Consumer<DexMethod> consumer) {
      consumer.accept(appendBoolean);
      consumer.accept(appendChar);
      consumer.accept(appendCharArray);
      consumer.accept(appendSubCharArray);
      consumer.accept(appendCharSequence);
      consumer.accept(appendSubCharSequence);
      consumer.accept(appendInt);
      consumer.accept(appendDouble);
      consumer.accept(appendFloat);
      consumer.accept(appendLong);
      consumer.accept(appendObject);
      consumer.accept(appendString);
      consumer.accept(appendStringBuffer);
      consumer.accept(appendBoolean);
    }
  }

  synchronized private static <T extends DexItem> T canonicalize(Map<T, T> map, T item) {
    assert item != null;
    assert !internalSentinels.contains(item);
    T previous = map.putIfAbsent(item, item);
    return previous == null ? item : previous;
  }

  synchronized private DexString canonicalizeString(String key) {
    assert key != null;
    return strings.computeIfAbsent(key, k -> new DexString(k));
  }

  public DexString createString(int size, byte[] content) {
    assert !sorted;
    return canonicalizeString(new DexString(size, content).toString());
  }

  public DexString createString(String source) {
    assert !sorted;
    return canonicalizeString(source);
  }

  public DexType createType(DexString descriptor) {
    assert !sorted;
    DexType type = new DexType(descriptor);
    return canonicalize(types, type);
  }

  public DexType createType(String descriptor) {
    return createType(createString(descriptor));
  }

  public DexField createField(DexType clazz, DexType type, DexString name) {
    assert !sorted;
    DexField field = new DexField(clazz, type, name);
    return canonicalize(fields, field);
  }

  public DexField createField(DexType clazz, DexType type, String name) {
    return createField(clazz, type, createString(name));
  }

  public DexProto createProto(DexString shorty, DexType returnType, DexTypeList parameters) {
    assert !sorted;
    DexProto proto = new DexProto(shorty, returnType, parameters);
    return canonicalize(protos, proto);
  }

  public DexProto createProto(DexString shorty, DexType returnType, DexType[] parameters) {
    assert !sorted;
    return createProto(shorty, returnType,
        parameters.length == 0 ? DexTypeList.empty() : new DexTypeList(parameters));
  }

  public DexProto createProto(DexType returnType, DexType... parameters) {
    return createProto(createShorty(returnType, parameters), returnType, parameters);
  }

  public DexString createShorty(DexType returnType, DexType[] argumentTypes) {
    StringBuilder shortyBuilder = new StringBuilder();
    shortyBuilder.append(returnType.toShorty());
    for (DexType argumentType : argumentTypes) {
      shortyBuilder.append(argumentType.toShorty());
    }
    return createString(shortyBuilder.toString());
  }

  public DexMethod createMethod(DexType holder, DexProto proto, DexString name) {
    assert !sorted;
    DexMethod method = new DexMethod(holder, proto, name);
    return canonicalize(methods, method);
  }

  public DexMethod createMethod(DexType holder, DexProto proto, String name) {
    return createMethod(holder, proto, createString(name));
  }

  public DexMethodHandle createMethodHandle(
      MethodHandleType type, Descriptor<? extends DexItem, ? extends Descriptor> fieldOrMethod) {
    assert !sorted;
    DexMethodHandle methodHandle = new DexMethodHandle(type, fieldOrMethod);
    return canonicalize(methodHandles, methodHandle);
  }

  public DexCallSite createCallSite(
      DexString methodName, DexProto methodProto,
      DexMethodHandle bootstrapMethod, List<DexValue> bootstrapArgs) {
    assert !sorted;
    DexCallSite callSite = new DexCallSite(methodName, methodProto, bootstrapMethod, bootstrapArgs);
    return canonicalize(callSites, callSite);
  }

  public DexMethod createMethod(DexString clazzDescriptor, DexString name,
      DexString returnTypeDescriptor,
      DexString[] parameterDescriptors) {
    assert !sorted;
    DexType clazz = createType(clazzDescriptor);
    DexType returnType = createType(returnTypeDescriptor);
    DexType[] parameterTypes = new DexType[parameterDescriptors.length];
    for (int i = 0; i < parameterDescriptors.length; i++) {
      parameterTypes[i] = createType(parameterDescriptors[i]);
    }
    DexProto proto = createProto(shorty(returnType, parameterTypes), returnType, parameterTypes);

    return createMethod(clazz, proto, name);
  }

  public AdvanceLine createAdvanceLine(int delta) {
    synchronized (advanceLines) {
      AdvanceLine result = advanceLines.get(delta);
      if (result == null) {
        result = new AdvanceLine(delta);
        advanceLines.put(delta, result);
      }
      return result;
    }
  }

  public AdvancePC createAdvancePC(int delta) {
    synchronized (advancePCs) {
      AdvancePC result = advancePCs.get(delta);
      if (result == null) {
        result = new AdvancePC(delta);
        advancePCs.put(delta, result);
      }
      return result;
    }
  }

  public Default createDefault(int value) {
    synchronized (defaults) {
      Default result = defaults.get(value);
      if (result == null) {
        result = new Default(value);
        defaults.put(value, result);
      }
      return result;
    }
  }

  public EndLocal createEndLocal(int registerNum) {
    synchronized (endLocals) {
      EndLocal result = endLocals.get(registerNum);
      if (result == null) {
        result = new EndLocal(registerNum);
        endLocals.put(registerNum, result);
      }
      return result;
    }
  }

  public RestartLocal createRestartLocal(int registerNum) {
    synchronized (restartLocals) {
      RestartLocal result = restartLocals.get(registerNum);
      if (result == null) {
        result = new RestartLocal(registerNum);
        restartLocals.put(registerNum, result);
      }
      return result;
    }
  }

  public SetEpilogueBegin createSetEpilogueBegin() {
    return setEpilogueBegin;
  }

  public SetPrologueEnd createSetPrologueEnd() {
    return setPrologueEnd;
  }

  public SetFile createSetFile(DexString fileName) {
    synchronized (setFiles) {
      SetFile result = setFiles.get(fileName);
      if (result == null) {
        result = new SetFile(fileName);
        setFiles.put(fileName, result);
      }
      return result;
    }
  }

  public boolean isConstructor(DexMethod method) {
    return method.name == constructorMethodName;
  }

  public boolean isClassConstructor(DexMethod method) {
    return method.name == classConstructorMethodName;
  }

  private DexString shorty(DexType returnType, DexType[] parameters) {
    StringBuilder builder = new StringBuilder();
    addToShorty(builder, returnType);
    for (DexType parameter : parameters) {
      addToShorty(builder, parameter);
    }
    return createString(builder.toString());
  }

  private void addToShorty(StringBuilder builder, DexType type) {
    char first = type.toDescriptorString().charAt(0);
    builder.append(first == '[' ? 'L' : first);
  }

  private static <S extends PresortedComparable<S>> void assignSortedIndices(Collection<S> items,
      NamingLens namingLens) {
    List<S> sorted = new ArrayList<>(items);
    sorted.sort((a, b) -> a.layeredCompareTo(b, namingLens));
    int i = 0;
    for (S value : sorted) {
      value.setSortedIndex(i++);
    }
  }

  synchronized public void sort(NamingLens namingLens) {
    assert !sorted;
    assignSortedIndices(strings.values(), namingLens);
    assignSortedIndices(types.values(), namingLens);
    assignSortedIndices(fields.values(), namingLens);
    assignSortedIndices(protos.values(), namingLens);
    assignSortedIndices(methods.values(), namingLens);
    sorted = true;
  }

  synchronized public void resetSortedIndices() {
    if (!sorted) {
      return;
    }
    // Only used for asserting that we don't use the sorted index after we build the graph.
    strings.values().forEach(IndexedDexItem::resetSortedIndex);
    types.values().forEach(IndexedDexItem::resetSortedIndex);
    fields.values().forEach(IndexedDexItem::resetSortedIndex);
    protos.values().forEach(IndexedDexItem::resetSortedIndex);
    methods.values().forEach(IndexedDexItem::resetSortedIndex);
    sorted = false;
  }

  synchronized public void forAllTypes(Consumer<DexType> f) {
    new ArrayList<>(types.values()).forEach(f);
  }
}
