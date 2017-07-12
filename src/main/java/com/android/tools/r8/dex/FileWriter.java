// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.utils.LebUtils.sizeAsUleb128;

import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.Descriptor;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationDirectory;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexAnnotationSetRefList;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexCode.Try;
import com.android.tools.r8.graph.DexCode.TryHandler;
import com.android.tools.r8.graph.DexCode.TryHandler.TypeAddrPair;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedArray;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.KeyedDexItem;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.graph.PresortedComparable;
import com.android.tools.r8.graph.ProgramClassVisitor;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LebUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.zip.Adler32;

public class FileWriter {

  private final ObjectToOffsetMapping mapping;
  private final DexApplication application;
  private final AppInfo appInfo;
  private final InternalOptions options;
  private final NamingLens namingLens;
  private final DexOutputBuffer dest = new DexOutputBuffer();
  private final MixedSectionOffsets mixedSectionOffsets;

  public FileWriter(
      ObjectToOffsetMapping mapping,
      DexApplication application,
      AppInfo appinfo,
      InternalOptions options,
      NamingLens namingLens) {
    this.mapping = mapping;
    this.application = application;
    this.appInfo = appinfo;
    this.options = options;
    this.namingLens = namingLens;
    this.mixedSectionOffsets = new MixedSectionOffsets();
  }

  public static void writeEncodedAnnotation(DexEncodedAnnotation annotation, DexOutputBuffer dest,
      ObjectToOffsetMapping mapping) {
    if (Log.ENABLED) {
      Log.verbose(FileWriter.class, "Writing encoded annotation @ %08x", dest.position());
    }
    dest.putUleb128(mapping.getOffsetFor(annotation.type));
    dest.putUleb128(annotation.elements.length);
    assert isSorted(annotation.elements, (element) -> element.name);
    for (DexAnnotationElement element : annotation.elements) {
      dest.putUleb128(mapping.getOffsetFor(element.name));
      element.value.writeTo(dest, mapping);
    }
  }

  private static <T extends PresortedComparable<T>> boolean isSorted(KeyedDexItem<T>[] items) {
    return isSorted(items, KeyedDexItem::getKey);
  }

  private static <S, T extends Comparable<T>> boolean isSorted(S[] items, Function<S, T> getter) {
    T current = null;
    for (S item : items) {
      T next = getter.apply(item);
      if (current != null && current.compareTo(next) >= 0) {
        return false;
      }
      current = next;
    }
    return true;
  }

  public FileWriter collect() {
    // Use the class array from the mapping, as it has a deterministic iteration order.
    new ProgramClassDependencyCollector(application, mapping.getClasses())
        .run(mapping.getClasses());

    // Sort the class members.
    // Needed before adding static-value arrays and writing annotation directories and classes.
    sortClassData(mixedSectionOffsets.getClassesWithData());

    // Add the static values for all fields now that we have committed to their sorting.
    mixedSectionOffsets.getClassesWithData().forEach(this::addStaticFieldValues);

    // String data is not tracked by the MixedSectionCollection.new AppInfo(application, null)
    assert mixedSectionOffsets.stringData.size() == 0;
    for (DexString string : mapping.getStrings()) {
      mixedSectionOffsets.add(string);
    }
    // Neither are the typelists in protos...
    for (DexProto proto : mapping.getProtos()) {
      mixedSectionOffsets.add(proto.parameters);
    }

    DexItem.collectAll(mixedSectionOffsets, mapping.getCallSites());

    DexItem.collectAll(mixedSectionOffsets, mapping.getClasses());

    return this;
  }

  private void rewriteCodeWithJumboStrings(IRConverter converter, DexEncodedMethod[] methods) {
    for (int i = 0; i < methods.length; i++) {
      DexEncodedMethod method = methods[i];
      if (method.getCode() == null) {
        continue;
      }
      DexCode code = method.getCode().asDexCode();
      if (code.highestSortingString != null) {
        if (mapping.getOffsetFor(code.highestSortingString) > Constants.MAX_NON_JUMBO_INDEX) {
          converter.processJumboStrings(method, mapping.getFirstJumboString());
        }
      }
    }
  }

  public FileWriter rewriteCodeWithJumboStrings(List<DexProgramClass> classes) {
    // If there are no strings with jumbo indices at all this is a no-op.
    if (!mapping.hasJumboStrings()) {
      return this;
    }
    // If the globally highest sorting string is not a jumbo string this is also a no-op.
    if (application.highestSortingString != null &&
        application.highestSortingString.slowCompareTo(mapping.getFirstJumboString()) < 0) {
      return this;
    }
    // At least one method needs a jumbo string.
    IRConverter converter = new IRConverter(application, appInfo, options, false);
    for (DexProgramClass clazz : classes) {
      rewriteCodeWithJumboStrings(converter, clazz.directMethods());
      rewriteCodeWithJumboStrings(converter, clazz.virtualMethods());
    }
    return this;
  }

  public byte[] generate() {
    // Check restrictions on interface methods.
    checkInterfaceMethods();

    Layout layout = Layout.from(mapping);
    layout.setCodesOffset(layout.dataSectionOffset);

    // Sort the codes first, as their order might impact size due to alignment constraints.
    List<DexCode> codes = sortDexCodesByClassName(mixedSectionOffsets.getCodes(), application);

    // Output the debug_info_items first, as they have no dependencies.
    dest.moveTo(layout.getCodesOffset() + sizeOfCodeItems(codes));
    writeItems(mixedSectionOffsets.getDebugInfos(), layout::setDebugInfosOffset,
        this::writeDebugItem);

    // Remember the typelist offset for later.
    layout.setTypeListsOffset(dest.align(4));  // type_list are aligned.

    // Now output the code.
    dest.moveTo(layout.getCodesOffset());
    assert dest.isAligned(4);
    writeItems(codes, layout::alreadySetOffset, this::writeCodeItem, 4);
    assert layout.getDebugInfosOffset() == 0 || dest.position() == layout.getDebugInfosOffset();

    // Now the type lists and rest.
    dest.moveTo(layout.getTypeListsOffset());
    writeItems(mixedSectionOffsets.getTypeLists(), layout::alreadySetOffset, this::writeTypeList);
    writeItems(mixedSectionOffsets.getStringData(), layout::setStringDataOffsets,
        this::writeStringData);
    writeItems(mixedSectionOffsets.getAnnotations(), layout::setAnnotationsOffset,
        this::writeAnnotation);
    writeItems(mixedSectionOffsets.getClassesWithData(), layout::setClassDataOffset,
        this::writeClassData);
    writeItems(mixedSectionOffsets.getEncodedArrays(), layout::setEncodedArrarysOffset,
        this::writeEncodedArray);
    writeItems(mixedSectionOffsets.getAnnotationSets(), layout::setAnnotationSetsOffset,
        this::writeAnnotationSet, 4);
    writeItems(mixedSectionOffsets.getAnnotationSetRefLists(),
        layout::setAnnotationSetRefListsOffset, this::writeAnnotationSetRefList, 4);
    writeItems(mixedSectionOffsets.getAnnotationDirectories(),
        layout::setAnnotationDirectoriesOffset, this::writeAnnotationDirectory, 4);

    // Add the map at the end
    layout.setMapOffset(dest.align(4));
    writeMap(layout);
    layout.setEndOfFile(dest.position());

    // Now that we have all mixedSectionOffsets, lets write the indexed items.
    dest.moveTo(Constants.HEADER_SIZE);
    writeFixedSectionItems(mapping.getStrings(), layout.stringIdsOffset, this::writeStringItem);
    writeFixedSectionItems(mapping.getTypes(), layout.typeIdsOffset, this::writeTypeItem);
    writeFixedSectionItems(mapping.getProtos(), layout.protoIdsOffset, this::writeProtoItem);
    writeFixedSectionItems(mapping.getFields(), layout.fieldIdsOffset, this::writeFieldItem);
    writeFixedSectionItems(mapping.getMethods(), layout.methodIdsOffset, this::writeMethodItem);
    writeFixedSectionItems(mapping.getClasses(), layout.classDefsOffset, this::writeClassDefItem);
    writeFixedSectionItems(mapping.getCallSites(), layout.callSiteIdsOffset, this::writeCallSite);
    writeFixedSectionItems(
        mapping.getMethodHandles(), layout.methodHandleIdsOffset, this::writeMethodHandle);

    // Fill in the header information.
    writeHeader(layout);
    writeSignature(layout);
    writeChecksum(layout);

    // Turn into an array
    return Arrays.copyOf(dest.asArray(), layout.getEndOfFile());
  }

  private void sortClassData(List<DexProgramClass> classesWithData) {
    for (DexProgramClass clazz : classesWithData) {
      sortEncodedFields(clazz.instanceFields);
      sortEncodedFields(clazz.staticFields);
      sortEncodedMethods(clazz.directMethods);
      sortEncodedMethods(clazz.virtualMethods);
    }
  }

  private void sortEncodedFields(DexEncodedField[] fields) {
    Arrays.sort(fields, (DexEncodedField a, DexEncodedField b) -> a.field.compareTo(b.field));
  }

  private void sortEncodedMethods(DexEncodedMethod[] methods) {
    Arrays.sort(methods, (DexEncodedMethod a, DexEncodedMethod b) -> a.method.compareTo(b.method));
  }

  private void checkInterfaceMethods() {
    for (DexProgramClass clazz : mapping.getClasses()) {
      if (clazz.isInterface()) {
        checkInterfaceMethods(clazz.directMethods());
        checkInterfaceMethods(clazz.virtualMethods());
      }
    }
  }

  // Ensures interface methods comply with requirements imposed by Android runtime:
  //  -- in pre-N Android versions interfaces may only have class
  //     initializer and public abstract methods.
  //  -- starting with N interfaces may also have public or private
  //     static methods, as well as public non-abstract (default)
  //     and private instance methods.
  private void checkInterfaceMethods(DexEncodedMethod[] methods) {
    for (DexEncodedMethod method : methods) {
      if (application.dexItemFactory.isClassConstructor(method.method)) {
        continue; // Class constructor is always OK.
      }
      if (method.accessFlags.isStatic()) {
        if (!options.canUseDefaultAndStaticInterfaceMethods()) {
          throw new CompilationError("Static interface methods are only supported "
              + "starting with Android N (--min-api " + Constants.ANDROID_N_API + "): "
              + method.method.toSourceString());
        }

      } else {
        if (method.accessFlags.isConstructor()) {
          throw new CompilationError(
              "Interface must not have constructors: " + method.method.toSourceString());
        }
        if (!method.accessFlags.isAbstract() && !method.accessFlags.isPrivate() &&
            !options.canUseDefaultAndStaticInterfaceMethods()) {
          throw new CompilationError("Default interface methods are only supported "
              + "starting with Android N (--min-api " + Constants.ANDROID_N_API + "): "
              + method.method.toSourceString());
        }
      }

      if (method.accessFlags.isPrivate()) {
        if (options.canUsePrivateInterfaceMethods()) {
          continue;
        }
        throw new CompilationError("Private interface methods are only supported "
            + "starting with Android N (--min-api " + Constants.ANDROID_N_API + "): "
            + method.method.toSourceString());
      }

      if (!method.accessFlags.isPublic()) {
        throw new CompilationError("Interface methods must not be "
            + "protected or package private: " + method.method.toSourceString());
      }
    }
  }

  private List<DexCode> sortDexCodesByClassName(List<DexCode> codes, DexApplication application) {
    Map<DexCode, String> codeToSignatureMap = new IdentityHashMap<>();
    for (DexProgramClass clazz : mapping.getClasses()) {
      addSignaturesFromMethods(clazz.directMethods(), codeToSignatureMap,
          application.getProguardMap());
      addSignaturesFromMethods(clazz.virtualMethods(), codeToSignatureMap,
          application.getProguardMap());
    }
    DexCode[] codesArray = codes.toArray(new DexCode[codes.size()]);
    Arrays.sort(codesArray, Comparator.comparing(codeToSignatureMap::get));
    return Arrays.asList(codesArray);
  }

  private static void addSignaturesFromMethods(DexEncodedMethod[] methods,
      Map<DexCode, String> codeToSignatureMap,
      ClassNameMapper proguardMap) {
    for (DexEncodedMethod method : methods) {
      if (method.getCode() == null) {
        assert method.accessFlags.isAbstract() || method.accessFlags.isNative();
      } else {
        Signature signature;
        String originalClassName;
        if (proguardMap != null) {
          signature = proguardMap.originalSignatureOf(method.method);
          originalClassName = proguardMap.originalNameOf(method.method.holder);
        } else {
          signature = MethodSignature.fromDexMethod(method.method);
          originalClassName = method.method.holder.toSourceString();
        }
        codeToSignatureMap.put(method.getCode().asDexCode(), originalClassName + signature);
      }
    }
  }

  private <T extends DexItem> void writeFixedSectionItems(T[] items, int offset,
      Consumer<T> writer) {
    assert dest.position() == offset;
    for (T item : items) {
      writer.accept(item);
    }
  }

  private <T extends DexItem> void writeItems(List<T> items, Consumer<Integer> offsetSetter,
      Consumer<T> writer) {
    writeItems(items, offsetSetter, writer, 1);
  }

  private <T extends DexItem> void writeItems(List<T> items, Consumer<Integer> offsetSetter,
      Consumer<T> writer, int alignment) {
    if (items.isEmpty()) {
      offsetSetter.accept(0);
    } else {
      offsetSetter.accept(dest.align(alignment));
      items.forEach(writer);
    }
  }

  private int sizeOfCodeItems(Iterable<DexCode> codes) {
    int size = 0;
    for (DexCode code : codes) {
      size = alignSize(4, size);
      size += sizeOfCodeItem(code);
    }
    return size;
  }

  private int sizeOfCodeItem(DexCode code) {
    int result = 16;
    int insnSize = 0;
    for (Instruction insn : code.instructions) {
      insnSize += insn.getSize();
    }
    result += insnSize * 2;
    result += code.tries.length * 8;
    if ((code.handlers != null) && (code.handlers.length > 0)) {
      result = alignSize(4, result);
      result += LebUtils.sizeAsUleb128(code.handlers.length);
      for (TryHandler handler : code.handlers) {
        boolean hasCatchAll = handler.catchAllAddr != TryHandler.NO_HANDLER;
        result += LebUtils
            .sizeAsSleb128(hasCatchAll ? -handler.pairs.length : handler.pairs.length);
        for (TypeAddrPair pair : handler.pairs) {

          result += sizeAsUleb128(mapping.getOffsetFor(pair.type));
          result += sizeAsUleb128(pair.addr);
        }
        if (hasCatchAll) {
          result += sizeAsUleb128(handler.catchAllAddr);
        }
      }
    }
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Computed size item %08d.", result);
    }
    return result;
  }

  private void writeStringItem(DexString string) {
    dest.putInt(mixedSectionOffsets.getOffsetFor(string));
  }

  private void writeTypeItem(DexType type) {
    DexString descriptor = namingLens.lookupDescriptor(type);
    dest.putInt(mapping.getOffsetFor(descriptor));
  }

  private void writeProtoItem(DexProto proto) {
    dest.putInt(mapping.getOffsetFor(proto.shorty));
    dest.putInt(mapping.getOffsetFor(proto.returnType));
    dest.putInt(mixedSectionOffsets.getOffsetFor(proto.parameters));
  }

  private void writeFieldItem(DexField field) {
    int classIdx = mapping.getOffsetFor(field.clazz);
    assert (short) classIdx == classIdx;
    dest.putShort((short) classIdx);
    int typeIdx = mapping.getOffsetFor(field.type);
    assert (short) typeIdx == typeIdx;
    dest.putShort((short) typeIdx);
    DexString name = namingLens.lookupName(field);
    dest.putInt(mapping.getOffsetFor(name));
  }

  private void writeMethodItem(DexMethod method) {
    int classIdx = mapping.getOffsetFor(method.holder);
    assert (short) classIdx == classIdx;
    dest.putShort((short) classIdx);
    int protoIdx = mapping.getOffsetFor(method.proto);
    assert (short) protoIdx == protoIdx;
    dest.putShort((short) protoIdx);
    DexString name = namingLens.lookupName(method);
    dest.putInt(mapping.getOffsetFor(name));
  }

  private void writeClassDefItem(DexProgramClass clazz) {
    dest.putInt(mapping.getOffsetFor(clazz.type));
    dest.putInt(clazz.accessFlags.get());
    dest.putInt(
        clazz.superType == null ? Constants.NO_INDEX : mapping.getOffsetFor(clazz.superType));
    dest.putInt(mixedSectionOffsets.getOffsetFor(clazz.interfaces));
    dest.putInt(
        clazz.sourceFile == null ? Constants.NO_INDEX : mapping.getOffsetFor(clazz.sourceFile));
    dest.putInt(mixedSectionOffsets.getOffsetForAnnotationsDirectory(clazz));
    dest.putInt(clazz.hasMethodsOrFields() ? mixedSectionOffsets.getOffsetFor(clazz) : Constants.NO_OFFSET);
    dest.putInt(mixedSectionOffsets.getOffsetFor(clazz.getStaticValues()));
  }

  private void writeDebugItem(DexDebugInfo debugInfo) {
    mixedSectionOffsets.setOffsetFor(debugInfo, dest.position());
    dest.putBytes(new DebugBytecodeWriter(debugInfo, mapping).generate());
  }

  private void writeCodeItem(DexCode code) {
    mixedSectionOffsets.setOffsetFor(code, dest.align(4));
    // Fixed size header information.
    dest.putShort((short) code.registerSize);
    dest.putShort((short) code.incomingRegisterSize);
    dest.putShort((short) code.outgoingRegisterSize);
    dest.putShort((short) code.tries.length);
    dest.putInt(mixedSectionOffsets.getOffsetFor(code.getDebugInfo()));
    // Jump over the size.
    int insnSizeOffset = dest.position();
    dest.forward(4);
    // Write instruction stream.
    dest.putInstructions(code.instructions, mapping);
    // Compute size and do the backward/forward dance to write the size at the beginning.
    int insnSize = dest.position() - insnSizeOffset - 4;
    dest.rewind(insnSize + 4);
    dest.putInt(insnSize / 2);
    dest.forward(insnSize);
    if (code.tries.length > 0) {
      // The tries need to be 4 byte aligned.
      int beginOfTriesOffset = dest.align(4);
      // First write the handlers, so that we know their mixedSectionOffsets.
      dest.forward(code.tries.length * 8);
      int beginOfHandlersOffset = dest.position();
      dest.putUleb128(code.handlers.length);
      short[] offsets = new short[code.handlers.length];
      int i = 0;
      for (TryHandler handler : code.handlers) {
        offsets[i++] = (short) (dest.position() - beginOfHandlersOffset);
        boolean hasCatchAll = handler.catchAllAddr != TryHandler.NO_HANDLER;
        dest.putSleb128(hasCatchAll ? -handler.pairs.length : handler.pairs.length);
        for (TypeAddrPair pair : handler.pairs) {
          dest.putUleb128(mapping.getOffsetFor(pair.type));
          dest.putUleb128(pair.addr);
        }
        if (hasCatchAll) {
          dest.putUleb128(handler.catchAllAddr);
        }
      }
      int endOfCodeOffset = dest.position();
      // Now write the tries.
      dest.moveTo(beginOfTriesOffset);
      for (Try aTry : code.tries) {
        dest.putInt(aTry.startAddress);
        dest.putShort((short) aTry.instructionCount);
        dest.putShort(offsets[aTry.handlerIndex]);
      }
      // And move to the end.
      dest.moveTo(endOfCodeOffset);
    }
  }

  private void writeTypeList(DexTypeList list) {
    assert !list.isEmpty();
    mixedSectionOffsets.setOffsetFor(list, dest.align(4));
    DexType[] values = list.values;
    dest.putInt(values.length);
    for (DexType type : values) {
      dest.putShort((short) mapping.getOffsetFor(type));
    }
  }

  private void writeStringData(DexString string) {
    mixedSectionOffsets.setOffsetFor(string, dest.position());
    dest.putUleb128(string.size);
    dest.putBytes(string.content);
  }

  private void writeAnnotation(DexAnnotation annotation) {
    mixedSectionOffsets.setOffsetFor(annotation, dest.position());
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Writing Annotation @ 0x%08x.", dest.position());
    }
    dest.putByte((byte) annotation.visibility);
    writeEncodedAnnotation(annotation.annotation, dest, mapping);
  }

  private void writeAnnotationSet(DexAnnotationSet set) {
    assert !set.isEmpty();
    assert isSorted(set.annotations, (item) -> item.annotation.type);
    mixedSectionOffsets.setOffsetFor(set, dest.align(4));
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Writing AnnotationSet @ 0x%08x.", dest.position());
    }
    dest.putInt(set.annotations.length);
    for (DexAnnotation annotation : set.annotations) {
      dest.putInt(mixedSectionOffsets.getOffsetFor(annotation));
    }
  }

  private void writeAnnotationSetRefList(DexAnnotationSetRefList setRefList) {
    assert !setRefList.isEmpty();
    mixedSectionOffsets.setOffsetFor(setRefList, dest.align(4));
    dest.putInt(setRefList.values.length);
    for (DexAnnotationSet set : setRefList.values) {
      dest.putInt(mixedSectionOffsets.getOffsetFor(set));
    }
  }

  private <S extends Descriptor<T, S>, T extends KeyedDexItem<S>> void writeMemberAnnotations(
      List<T> items, ToIntFunction<T> getter) {
    for (T item : items) {
      dest.putInt(item.getKey().getOffset(mapping));
      dest.putInt(getter.applyAsInt(item));
    }
  }

  private void writeAnnotationDirectory(DexAnnotationDirectory annotationDirectory) {
    mixedSectionOffsets.setOffsetForAnnotationsDirectory(annotationDirectory, dest.align(4));
    dest.putInt(mixedSectionOffsets.getOffsetFor(annotationDirectory.getClazzAnnotations()));
    List<DexEncodedMethod> methodAnnotations = annotationDirectory.getMethodAnnotations();
    List<DexEncodedMethod> parameterAnnotations = annotationDirectory.getParameterAnnotations();
    List<DexEncodedField> fieldAnnotations = annotationDirectory.getFieldAnnotations();
    dest.putInt(fieldAnnotations.size());
    dest.putInt(methodAnnotations.size());
    dest.putInt(parameterAnnotations.size());
    writeMemberAnnotations(fieldAnnotations,
        item -> mixedSectionOffsets.getOffsetFor(item.annotations));
    writeMemberAnnotations(methodAnnotations,
        item -> mixedSectionOffsets.getOffsetFor(item.annotations));
    writeMemberAnnotations(parameterAnnotations,
        item -> mixedSectionOffsets.getOffsetFor(item.parameterAnnotations));
  }

  private void writeEncodedFields(DexEncodedField[] fields) {
    assert isSorted(fields);
    int currentOffset = 0;
    for (DexEncodedField field : fields) {
      int nextOffset = mapping.getOffsetFor(field.field);
      assert nextOffset - currentOffset >= 0;
      dest.putUleb128(nextOffset - currentOffset);
      currentOffset = nextOffset;
      dest.putUleb128(field.accessFlags.get());
    }
  }

  private void writeEncodedMethods(DexEncodedMethod[] methods) {
    assert isSorted(methods);
    int currentOffset = 0;
    for (DexEncodedMethod method : methods) {
      int nextOffset = mapping.getOffsetFor(method.method);
      assert nextOffset - currentOffset >= 0;
      dest.putUleb128(nextOffset - currentOffset);
      currentOffset = nextOffset;
      dest.putUleb128(method.accessFlags.get());
      if (method.getCode() == null) {
        assert method.accessFlags.isAbstract() || method.accessFlags.isNative();
        dest.putUleb128(0);
      } else {
        dest.putUleb128(mixedSectionOffsets.getOffsetFor(method.getCode().asDexCode()));
        // Writing the methods starts to take up memory so we are going to flush the
        // code objects since they are no longer necessary after this.
        method.removeCode();
      }
    }
  }

  private void writeClassData(DexProgramClass clazz) {
    assert clazz.hasMethodsOrFields();
    mixedSectionOffsets.setOffsetFor(clazz, dest.position());
    dest.putUleb128(clazz.staticFields().length);
    dest.putUleb128(clazz.instanceFields().length);
    dest.putUleb128(clazz.directMethods().length);
    dest.putUleb128(clazz.virtualMethods().length);
    writeEncodedFields(clazz.staticFields());
    writeEncodedFields(clazz.instanceFields());
    writeEncodedMethods(clazz.directMethods());
    writeEncodedMethods(clazz.virtualMethods());
  }

  private void addStaticFieldValues(DexProgramClass clazz) {
    DexEncodedField[] fields = clazz.staticFields();
    int length = 0;
    List<DexValue> values = new ArrayList<>(fields.length);
    for (int i = 0; i < fields.length; i++) {
      DexEncodedField field = fields[i];
      assert field.staticValue != null;
      values.add(field.staticValue);
      if (!field.staticValue.isDefault(field.field.type, application.dexItemFactory)) {
        length = i + 1;
      }
    }
    if (length > 0) {
      DexEncodedArray staticValues = new DexEncodedArray(
          values.subList(0, length).toArray(new DexValue[length]));
      clazz.setStaticValues(staticValues);
      mixedSectionOffsets.add(staticValues);
    }
  }

  private void writeMethodHandle(DexMethodHandle methodHandle) {
    checkThatInvokeCustomIsAllowed();
    MethodHandleType methodHandleDexType;
    switch (methodHandle.type) {
      case INVOKE_CONSTRUCTOR:
        throw new CompilationError("Constructor method handle type is not yet supported.");
      case INVOKE_INTERFACE:
      case INVOKE_SUPER:
        methodHandleDexType = MethodHandleType.INVOKE_INSTANCE;
        break;
      default:
        methodHandleDexType = methodHandle.type;
        break;
    }
    assert dest.isAligned(4);
    dest.putShort(methodHandleDexType.getValue());
    dest.putShort((short) 0); // unused
    int fieldOrMethodIdx;
    if (methodHandle.isMethodHandle()) {
      fieldOrMethodIdx = mapping.getOffsetFor(methodHandle.asMethod());
    } else {
      assert methodHandle.isFieldHandle();
      fieldOrMethodIdx = mapping.getOffsetFor(methodHandle.asField());
    }
    assert (short) fieldOrMethodIdx == fieldOrMethodIdx;
    dest.putShort((short) fieldOrMethodIdx);
    dest.putShort((short) 0); // unused
  }

  private void writeCallSite(DexCallSite callSite) {
    checkThatInvokeCustomIsAllowed();
    assert dest.isAligned(4);
    dest.putInt(mixedSectionOffsets.getOffsetFor(callSite.getEncodedArray()));
  }

  private void writeEncodedArray(DexEncodedArray array) {
    mixedSectionOffsets.setOffsetFor(array, dest.position());
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Writing EncodedArray @ 0x%08x [%s].", dest.position(), array);
    }
    dest.putUleb128(array.values.length);
    for (DexValue value : array.values) {
      value.writeTo(dest, mapping);
    }
  }

  private int writeMapItem(int type, int offset, int length) {
    if (length == 0) {
      return 0;
    }
    if (Log.ENABLED) {
      Log.debug(getClass(), "Map entry 0x%04x @ 0x%08x # %08d.", type, offset, length);
    }
    dest.putShort((short) type);
    dest.putShort((short) 0);
    dest.putInt(length);
    dest.putInt(offset);
    return 1;
  }

  private void writeMap(Layout layout) {
    int startOfMap = dest.align(4);
    dest.forward(4); // Leave space for size;
    int size = 0;
    size += writeMapItem(Constants.TYPE_HEADER_ITEM, 0, 1);
    size += writeMapItem(Constants.TYPE_STRING_ID_ITEM, layout.stringIdsOffset,
        mapping.getStrings().length);
    size += writeMapItem(Constants.TYPE_TYPE_ID_ITEM, layout.typeIdsOffset,
        mapping.getTypes().length);
    size += writeMapItem(Constants.TYPE_PROTO_ID_ITEM, layout.protoIdsOffset,
        mapping.getProtos().length);
    size += writeMapItem(Constants.TYPE_FIELD_ID_ITEM, layout.fieldIdsOffset,
        mapping.getFields().length);
    size += writeMapItem(Constants.TYPE_METHOD_ID_ITEM, layout.methodIdsOffset,
        mapping.getMethods().length);
    size += writeMapItem(Constants.TYPE_CLASS_DEF_ITEM, layout.classDefsOffset,
        mapping.getClasses().length);
    size += writeMapItem(Constants.TYPE_CALL_SITE_ID_ITEM, layout.callSiteIdsOffset,
        mapping.getCallSites().length);
    size += writeMapItem(Constants.TYPE_METHOD_HANDLE_ITEM, layout.methodHandleIdsOffset,
        mapping.getMethodHandles().length);
    size += writeMapItem(Constants.TYPE_CODE_ITEM, layout.getCodesOffset(),
        mixedSectionOffsets.getCodes().size());
    size += writeMapItem(Constants.TYPE_DEBUG_INFO_ITEM, layout.getDebugInfosOffset(),
        mixedSectionOffsets.getDebugInfos().size());
    size += writeMapItem(Constants.TYPE_TYPE_LIST, layout.getTypeListsOffset(),
        mixedSectionOffsets.getTypeLists().size());
    size += writeMapItem(Constants.TYPE_STRING_DATA_ITEM, layout.getStringDataOffsets(),
        mixedSectionOffsets.getStringData().size());
    size += writeMapItem(Constants.TYPE_ANNOTATION_ITEM, layout.getAnnotationsOffset(),
        mixedSectionOffsets.getAnnotations().size());
    size += writeMapItem(Constants.TYPE_CLASS_DATA_ITEM, layout.getClassDataOffset(),
        mixedSectionOffsets.getClassesWithData().size());
    size += writeMapItem(Constants.TYPE_ENCODED_ARRAY_ITEM, layout.getEncodedArrarysOffset(),
        mixedSectionOffsets.getEncodedArrays().size());
    size += writeMapItem(Constants.TYPE_ANNOTATION_SET_ITEM, layout.getAnnotationSetsOffset(),
        mixedSectionOffsets.getAnnotationSets().size());
    size += writeMapItem(Constants.TYPE_ANNOTATION_SET_REF_LIST,
        layout.getAnnotationSetRefListsOffset(),
        mixedSectionOffsets.getAnnotationSetRefLists().size());
    size += writeMapItem(Constants.TYPE_ANNOTATIONS_DIRECTORY_ITEM,
        layout.getAnnotationDirectoriesOffset(),
        mixedSectionOffsets.getAnnotationDirectories().size());
    size += writeMapItem(Constants.TYPE_MAP_LIST, layout.getMapOffset(), 1);
    dest.moveTo(startOfMap);
    dest.putInt(size);
    dest.forward(size * Constants.TYPE_MAP_LIST_ITEM_SIZE);
  }

  private static byte[] convertApiLevelToDexVersion(int apiLevel) {
    if (apiLevel >= Constants.ANDROID_O_API) {
      return Constants.ANDROID_O_DEX_VERSION_BYTES;
    }
    if (apiLevel >= Constants.ANDROID_N_API) {
      return Constants.ANDROID_N_DEX_VERSION_BYTES;
    }
    return Constants.ANDROID_PRE_N_DEX_VERSION_BYTES;
  }

  private void writeHeader(Layout layout) {
    dest.moveTo(0);
    dest.putBytes(Constants.DEX_FILE_MAGIC_PREFIX);
    dest.putBytes(convertApiLevelToDexVersion(options.minApiLevel));
    dest.putByte(Constants.DEX_FILE_MAGIC_SUFFIX);
    // Leave out checksum and signature for now.
    dest.moveTo(Constants.FILE_SIZE_OFFSET);
    dest.putInt(layout.getEndOfFile());
    dest.putInt(Constants.HEADER_SIZE);
    dest.putInt(Constants.ENDIAN_CONSTANT);
    dest.putInt(0);
    dest.putInt(0);
    dest.putInt(layout.getMapOffset());
    int numberOfStrings = mapping.getStrings().length;
    dest.putInt(numberOfStrings);
    dest.putInt(numberOfStrings == 0 ? 0 : layout.stringIdsOffset);
    int numberOfTypes = mapping.getTypes().length;
    dest.putInt(numberOfTypes);
    dest.putInt(numberOfTypes == 0 ? 0 : layout.typeIdsOffset);
    int numberOfProtos = mapping.getProtos().length;
    dest.putInt(numberOfProtos);
    dest.putInt(numberOfProtos == 0 ? 0 : layout.protoIdsOffset);
    int numberOfFields = mapping.getFields().length;
    dest.putInt(numberOfFields);
    dest.putInt(numberOfFields == 0 ? 0 : layout.fieldIdsOffset);
    int numberOfMethods = mapping.getMethods().length;
    dest.putInt(numberOfMethods);
    dest.putInt(numberOfMethods == 0 ? 0 : layout.methodIdsOffset);
    int numberOfClasses = mapping.getClasses().length;
    dest.putInt(numberOfClasses);
    dest.putInt(numberOfClasses == 0 ? 0 : layout.classDefsOffset);
    dest.putInt(layout.getDataSectionSize());
    dest.putInt(layout.dataSectionOffset);
    assert dest.position() == layout.stringIdsOffset;
  }

  private void writeSignature(Layout layout) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(dest.asArray(), Constants.FILE_SIZE_OFFSET,
          layout.getEndOfFile() - Constants.FIELD_IDS_OFF_OFFSET);
      md.digest(dest.asArray(), Constants.SIGNATURE_OFFSET, 20);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void writeChecksum(Layout layout) {
    Adler32 adler = new Adler32();
    adler.update(dest.asArray(), Constants.SIGNATURE_OFFSET,
        layout.getEndOfFile() - Constants.SIGNATURE_OFFSET);
    dest.moveTo(Constants.CHECKSUM_OFFSET);
    dest.putInt((int) adler.getValue());
  }

  private int alignSize(int bytes, int value) {
    int mask = bytes - 1;
    return (value + mask) & ~mask;
  }

  private static class Layout {

    private static final int NOT_SET = -1;

    // Fixed size constant pool sections
    final int stringIdsOffset;
    final int typeIdsOffset;
    final int protoIdsOffset;
    final int fieldIdsOffset;
    final int methodIdsOffset;
    final int classDefsOffset;
    final int callSiteIdsOffset;
    final int methodHandleIdsOffset;
    final int dataSectionOffset;

    // Mixed size sections
    private int codesOffset = NOT_SET; // aligned
    private int debugInfosOffset = NOT_SET;

    private int typeListsOffset = NOT_SET; // aligned
    private int stringDataOffsets = NOT_SET;
    private int annotationsOffset = NOT_SET;
    private int annotationSetsOffset = NOT_SET; // aligned
    private int annotationSetRefListsOffset = NOT_SET; // aligned
    private int annotationDirectoriesOffset = NOT_SET; // aligned
    private int classDataOffset = NOT_SET;
    private int encodedArrarysOffset = NOT_SET;
    private int mapOffset = NOT_SET;
    private int endOfFile = NOT_SET;

    private Layout(int stringIdsOffset, int typeIdsOffset, int protoIdsOffset, int fieldIdsOffset,
        int methodIdsOffset, int classDefsOffset, int callSiteIdsOffset, int methodHandleIdsOffset,
        int dataSectionOffset) {
      this.stringIdsOffset = stringIdsOffset;
      this.typeIdsOffset = typeIdsOffset;
      this.protoIdsOffset = protoIdsOffset;
      this.fieldIdsOffset = fieldIdsOffset;
      this.methodIdsOffset = methodIdsOffset;
      this.classDefsOffset = classDefsOffset;
      this.callSiteIdsOffset = callSiteIdsOffset;
      this.methodHandleIdsOffset = methodHandleIdsOffset;
      this.dataSectionOffset = dataSectionOffset;
      assert stringIdsOffset <= typeIdsOffset;
      assert typeIdsOffset <= protoIdsOffset;
      assert protoIdsOffset <= fieldIdsOffset;
      assert fieldIdsOffset <= methodIdsOffset;
      assert methodIdsOffset <= classDefsOffset;
      assert classDefsOffset <= dataSectionOffset;
      assert callSiteIdsOffset <= dataSectionOffset;
      assert methodHandleIdsOffset <= dataSectionOffset;
    }

    static Layout from(ObjectToOffsetMapping mapping) {
      int offset = 0;
      return new Layout(
          offset = Constants.HEADER_SIZE,
          offset += mapping.getStrings().length * Constants.TYPE_STRING_ID_ITEM_SIZE,
          offset += mapping.getTypes().length * Constants.TYPE_TYPE_ID_ITEM_SIZE,
          offset += mapping.getProtos().length * Constants.TYPE_PROTO_ID_ITEM_SIZE,
          offset += mapping.getFields().length * Constants.TYPE_FIELD_ID_ITEM_SIZE,
          offset += mapping.getMethods().length * Constants.TYPE_METHOD_ID_ITEM_SIZE,
          offset += mapping.getClasses().length * Constants.TYPE_CLASS_DEF_ITEM_SIZE,
          offset += mapping.getCallSites().length * Constants.TYPE_CALL_SITE_ID_ITEM_SIZE,
          offset += mapping.getMethodHandles().length * Constants.TYPE_METHOD_HANDLE_ITEM_SIZE);
    }

    int getDataSectionSize() {
      int size = getEndOfFile() - dataSectionOffset;
      assert size % 4 == 0;
      return size;
    }

    private boolean isValidOffset(int value, boolean isAligned) {
      return value != NOT_SET && (!isAligned || value % 4 == 0);
    }

    public int getCodesOffset() {
      assert isValidOffset(codesOffset, true);
      return codesOffset;
    }

    public void setCodesOffset(int codesOffset) {
      assert this.codesOffset == NOT_SET;
      this.codesOffset = codesOffset;
    }

    public int getDebugInfosOffset() {
      assert isValidOffset(debugInfosOffset, false);
      return debugInfosOffset;
    }

    public void setDebugInfosOffset(int debugInfosOffset) {
      assert this.debugInfosOffset == NOT_SET;
      this.debugInfosOffset = debugInfosOffset;
    }

    public int getTypeListsOffset() {
      assert isValidOffset(typeListsOffset, true);
      return typeListsOffset;
    }

    public void setTypeListsOffset(int typeListsOffset) {
      assert this.typeListsOffset == NOT_SET;
      this.typeListsOffset = typeListsOffset;
    }

    public int getStringDataOffsets() {
      assert isValidOffset(stringDataOffsets, false);
      return stringDataOffsets;
    }

    public void setStringDataOffsets(int stringDataOffsets) {
      assert this.stringDataOffsets == NOT_SET;
      this.stringDataOffsets = stringDataOffsets;
    }

    public int getAnnotationsOffset() {
      assert isValidOffset(annotationsOffset, false);
      return annotationsOffset;
    }

    public void setAnnotationsOffset(int annotationsOffset) {
      assert this.annotationsOffset == NOT_SET;
      this.annotationsOffset = annotationsOffset;
    }

    public int getAnnotationSetsOffset() {
      assert isValidOffset(annotationSetsOffset, true);
      return annotationSetsOffset;
    }

    public void alreadySetOffset(int ignored) {
      // Intentionally empty.
    }

    public void setAnnotationSetsOffset(int annotationSetsOffset) {
      assert this.annotationSetsOffset == NOT_SET;
      this.annotationSetsOffset = annotationSetsOffset;
    }

    public int getAnnotationSetRefListsOffset() {
      assert isValidOffset(annotationSetRefListsOffset, true);
      return annotationSetRefListsOffset;
    }

    public void setAnnotationSetRefListsOffset(int annotationSetRefListsOffset) {
      assert this.annotationSetRefListsOffset == NOT_SET;
      this.annotationSetRefListsOffset = annotationSetRefListsOffset;
    }

    public int getAnnotationDirectoriesOffset() {
      assert isValidOffset(annotationDirectoriesOffset, true);
      return annotationDirectoriesOffset;
    }

    public void setAnnotationDirectoriesOffset(int annotationDirectoriesOffset) {
      assert this.annotationDirectoriesOffset == NOT_SET;
      this.annotationDirectoriesOffset = annotationDirectoriesOffset;
    }

    public int getClassDataOffset() {
      assert isValidOffset(classDataOffset, false);
      return classDataOffset;
    }

    public void setClassDataOffset(int classDataOffset) {
      assert this.classDataOffset == NOT_SET;
      this.classDataOffset = classDataOffset;
    }

    public int getEncodedArrarysOffset() {
      assert isValidOffset(encodedArrarysOffset, false);
      return encodedArrarysOffset;
    }

    public void setEncodedArrarysOffset(int encodedArrarysOffset) {
      assert this.encodedArrarysOffset == NOT_SET;
      this.encodedArrarysOffset = encodedArrarysOffset;
    }

    public int getMapOffset() {
      return mapOffset;
    }

    public void setMapOffset(int mapOffset) {
      this.mapOffset = mapOffset;
    }

    public int getEndOfFile() {
      return endOfFile;
    }

    public void setEndOfFile(int endOfFile) {
      this.endOfFile = endOfFile;
    }
  }

  /**
   * Encapsulates information on the offsets of items in the sections of the mixed data part of the
   * DEX file.
   * Initially, items are collected using the {@link MixedSectionCollection} traversal and all
   * offsets are unset. When writing a section, the offsets of the written items are stored.
   * These offsets are then used to resolve cross-references between items from different sections
   * into a file offset.
   */
  private static class MixedSectionOffsets extends MixedSectionCollection {

    private static final int NOT_SET = -1;

    private final Map<DexCode, Integer> codes = Maps.newIdentityHashMap();
    private final List<DexCode> codesList = new LinkedList<>();
    private final Hashtable<DexDebugInfo, Integer> debugInfos = new Hashtable<>();
    private final List<DexDebugInfo> debugInfosList = new LinkedList<>();
    private final Hashtable<DexTypeList, Integer> typeLists = new Hashtable<>();
    private final List<DexTypeList> typeListsList = new LinkedList<>();
    private final Hashtable<DexString, Integer> stringData = new Hashtable<>();
    private final List<DexString> stringDataList = new LinkedList<>();
    private final Hashtable<DexAnnotation, Integer> annotations = new Hashtable<>();
    private final List<DexAnnotation> annotationsList = new LinkedList<>();
    private final Hashtable<DexAnnotationSet, Integer> annotationSets = new Hashtable<>();
    private final List<DexAnnotationSet> annotationSetsList = new LinkedList<>();
    private final Hashtable<DexAnnotationSetRefList, Integer> annotationSetRefLists
        = new Hashtable<>();
    private final List<DexAnnotationSetRefList> annotationSetRefListsList = new LinkedList<>();
    private final Hashtable<DexProgramClass, DexAnnotationDirectory> clazzToAnnotationDirectory
        = new Hashtable<>();
    private final Hashtable<DexAnnotationDirectory, Integer> annotationDirectories
        = new Hashtable<>();
    private final List<DexAnnotationDirectory> annotationDirectoriesList = new LinkedList<>();
    private final Hashtable<DexProgramClass, Integer> classesWithData = new Hashtable<>();
    private final List<DexProgramClass> classesWithDataList = new LinkedList<>();
    private final Hashtable<DexEncodedArray, Integer> encodedArrays = new Hashtable<>();
    private final List<DexEncodedArray> encodedArraysList = new LinkedList<>();

    private <T> boolean add(Map<T, Integer> map, List<T> list, T item) {
      boolean notSeen = map.put(item, NOT_SET) == null;
      if (notSeen) {
        list.add(item);
      }
      return notSeen;
    }

    @Override
    public boolean add(DexProgramClass aClassWithData) {
      return add(classesWithData, classesWithDataList, aClassWithData);
    }

    @Override
    public boolean add(DexEncodedArray encodedArray) {
      return add(encodedArrays, encodedArraysList, encodedArray);
    }

    @Override
    public boolean add(DexAnnotationSet annotationSet) {
      if (annotationSet.isEmpty()) {
        return false;
      }
      return add(annotationSets, annotationSetsList, annotationSet);
    }

    @Override
    public boolean add(DexCode code) {
      return add(codes, codesList, code);
    }

    @Override
    public boolean add(DexDebugInfo debugInfo) {
      return add(debugInfos, debugInfosList, debugInfo);
    }

    @Override
    public boolean add(DexTypeList typeList) {
      if (typeList.isEmpty()) {
        return false;
      }
      return add(typeLists, typeListsList, typeList);
    }

    @Override
    public boolean add(DexAnnotationSetRefList annotationSetRefList) {
      if (annotationSetRefList.isEmpty()) {
        return false;
      }
      return add(annotationSetRefLists, annotationSetRefListsList, annotationSetRefList);
    }

    @Override
    public boolean add(DexAnnotation annotation) {
      return add(annotations, annotationsList, annotation);
    }

    @Override
    public boolean setAnnotationsDirectoryForClass(DexProgramClass clazz,
        DexAnnotationDirectory annotationDirectory) {
      DexAnnotationDirectory previous = clazzToAnnotationDirectory.put(clazz, annotationDirectory);
      assert previous == null;
      return add(annotationDirectories, annotationDirectoriesList, annotationDirectory);
    }

    public boolean add(DexString string) {
      return add(stringData, stringDataList, string);
    }

    public List<DexCode> getCodes() {
      return Collections.unmodifiableList(codesList);
    }

    public List<DexDebugInfo> getDebugInfos() {
      return Collections.unmodifiableList(debugInfosList);
    }

    public List<DexTypeList> getTypeLists() {
      return Collections.unmodifiableList(typeListsList);
    }

    public List<DexString> getStringData() {
      return Collections.unmodifiableList(stringDataList);
    }

    public List<DexAnnotation> getAnnotations() {
      return Collections.unmodifiableList(annotationsList);
    }

    public List<DexAnnotationSet> getAnnotationSets() {
      return Collections.unmodifiableList(annotationSetsList);
    }

    public List<DexAnnotationSetRefList> getAnnotationSetRefLists() {
      return Collections.unmodifiableList(annotationSetRefListsList);
    }

    public List<DexProgramClass> getClassesWithData() {
      return Collections.unmodifiableList(classesWithDataList);
    }

    public List<DexAnnotationDirectory> getAnnotationDirectories() {
      return Collections.unmodifiableList(annotationDirectoriesList);
    }

    public List<DexEncodedArray> getEncodedArrays() {
      return Collections.unmodifiableList(encodedArraysList);
    }

    private <T> int lookup(T item, Map<T, Integer> table) {
      if (item == null) {
        return Constants.NO_OFFSET;
      }
      Integer offset = table.get(item);
      assert offset != null;
      assert offset != NOT_SET;
      return offset;
    }

    public int getOffsetFor(DexString item) {
      return lookup(item, stringData);
    }

    public int getOffsetFor(DexTypeList parameters) {
      if (parameters.isEmpty()) {
        return 0;
      }
      return lookup(parameters, typeLists);
    }

    public int getOffsetFor(DexProgramClass aClassWithData) {
      return lookup(aClassWithData, classesWithData);
    }

    public int getOffsetFor(DexEncodedArray encodedArray) {
      return lookup(encodedArray, encodedArrays);
    }

    public int getOffsetFor(DexDebugInfo debugInfo) {
      return lookup(debugInfo, debugInfos);
    }


    public int getOffsetForAnnotationsDirectory(DexProgramClass clazz) {
      if (!clazz.hasAnnotations()) {
        return Constants.NO_OFFSET;
      }
      Integer offset = annotationDirectories.get(clazzToAnnotationDirectory.get(clazz));
      assert offset != null;
      return offset;
    }

    public int getOffsetFor(DexAnnotation annotation) {
      return lookup(annotation, annotations);
    }

    public int getOffsetFor(DexAnnotationSet annotationSet) {
      if (annotationSet.isEmpty()) {
        return 0;
      }
      return lookup(annotationSet, annotationSets);
    }

    public int getOffsetFor(DexAnnotationSetRefList annotationSetRefList) {
      if (annotationSetRefList.isEmpty()) {
        return 0;
      }
      return lookup(annotationSetRefList, annotationSetRefLists);
    }

    public int getOffsetFor(DexCode code) {
      return lookup(code, codes);
    }

    private <T> void setOffsetFor(T item, int offset, Map<T, Integer> table) {
      Integer old = table.put(item, offset);
      assert old != null;
      assert old <= NOT_SET;
    }

    void setOffsetFor(DexDebugInfo debugInfo, int offset) {
      setOffsetFor(debugInfo, offset, debugInfos);
    }

    void setOffsetFor(DexCode code, int offset) {
      setOffsetFor(code, offset, codes);
    }

    void setOffsetFor(DexTypeList typeList, int offset) {
      assert offset != 0 && !typeLists.isEmpty();
      setOffsetFor(typeList, offset, typeLists);
    }

    void setOffsetFor(DexString string, int offset) {
      setOffsetFor(string, offset, stringData);
    }

    void setOffsetFor(DexAnnotation annotation, int offset) {
      setOffsetFor(annotation, offset, annotations);
    }

    void setOffsetFor(DexAnnotationSet annotationSet, int offset) {
      setOffsetFor(annotationSet, offset, annotationSets);
    }

    void setOffsetForAnnotationsDirectory(DexAnnotationDirectory annotationDirectory, int offset) {
      setOffsetFor(annotationDirectory, offset, annotationDirectories);
    }

    void setOffsetFor(DexProgramClass aClassWithData, int offset) {
      setOffsetFor(aClassWithData, offset, classesWithData);
    }

    void setOffsetFor(DexEncodedArray encodedArray, int offset) {
      setOffsetFor(encodedArray, offset, encodedArrays);
    }

    void setOffsetFor(DexAnnotationSetRefList annotationSetRefList, int offset) {
      assert offset != 0 && !annotationSetRefList.isEmpty();
      setOffsetFor(annotationSetRefList, offset, annotationSetRefLists);
    }
  }

  private class ProgramClassDependencyCollector extends ProgramClassVisitor {

    private final Set<DexClass> includedClasses = Sets.newIdentityHashSet();

    ProgramClassDependencyCollector(DexApplication application, DexProgramClass[] includedClasses) {
      super(application);
      Collections.addAll(this.includedClasses, includedClasses);
    }

    @Override
    public void visit(DexType type) {
      // Intentionally left empty.
    }

    @Override
    public void visit(DexClass clazz) {
      // Only visit classes that are part of the current file.
      if (!includedClasses.contains(clazz)) {
        return;
      }
      clazz.addDependencies(mixedSectionOffsets);
    }
  }

  private void checkThatInvokeCustomIsAllowed() {
    if (!options.canUseInvokeCustom()) {
      throw new CompilationError("Invoke-custom is unsupported before Android O (--min-api "
          + Constants.ANDROID_O_API + ")");
    }
  }
}
