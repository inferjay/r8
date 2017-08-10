// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class ObjectToOffsetMapping {

  private final int virtualFileId;

  private final DexProgramClass[] classes;
  private final DexProto[] protos;
  private final DexType[] types;
  private final DexMethod[] methods;
  private final DexField[] fields;
  private final DexString[] strings;
  private final DexCallSite[] callSites;
  private final DexMethodHandle[] methodHandles;
  private DexString firstJumboString;

  public ObjectToOffsetMapping(
      int virtualFileId,
      DexApplication application,
      DexProgramClass[] classes,
      DexProto[] protos,
      DexType[] types,
      DexMethod[] methods,
      DexField[] fields,
      DexString[] strings,
      DexCallSite[] callSites,
      DexMethodHandle[] methodHandles) {
    assert application != null;
    assert classes != null;
    assert protos != null;
    assert types != null;
    assert methods != null;
    assert fields != null;
    assert strings != null;
    assert callSites != null;
    assert methodHandles != null;

    this.virtualFileId = virtualFileId;
    this.classes = sortClasses(application, classes);
    this.protos = protos;
    this.types = types;
    this.methods = methods;
    this.fields = fields;
    this.strings = strings;
    this.callSites = callSites;
    this.methodHandles = methodHandles;

    Arrays.sort(protos);
    setIndexes(protos);

    Arrays.sort(types);
    setIndexes(types);

    Arrays.sort(methods);
    setIndexes(methods);

    Arrays.sort(fields);
    setIndexes(fields);

    Arrays.sort(strings);
    setIndexes(strings);

    // No need to sort CallSite, they will be written in data section in the callSites order,
    // consequently offset of call site used into the call site section will be in ascending order.
    setIndexes(callSites);

    // No need to sort method handle
    setIndexes(methodHandles);
  }

  private static DexProgramClass[] sortClasses(
      DexApplication application, DexProgramClass[] classes) {
    Arrays.sort(classes, (o1, o2) -> o1.type.descriptor.slowCompareTo(o2.type.descriptor));
    SortingProgramClassVisitor classVisitor = new SortingProgramClassVisitor(application, classes);
    classVisitor.run(classes);
    return classVisitor.getSortedClasses();
  }

  private void setIndexes(IndexedDexItem[] items) {
    int index = 0;
    for (IndexedDexItem item : items) {
      item.assignVirtualFileIndex(virtualFileId, index);
      // For strings collect the first jumbo string (if any).
      if ((index > Constants.MAX_NON_JUMBO_INDEX) && (item instanceof DexString)) {
        if (index == Constants.FIRST_JUMBO_INDEX) {
          firstJumboString = (DexString) item;
        }
      }
      index++;
    }
  }

  public DexMethod[] getMethods() {
    return methods;
  }

  public DexProgramClass[] getClasses() {
    return classes;
  }

  public DexType[] getTypes() {
    return types;
  }

  public DexProto[] getProtos() {
    return protos;
  }

  public DexField[] getFields() {
    return fields;
  }

  public DexString[] getStrings() {
    return strings;
  }

  public DexCallSite[] getCallSites() {
    return callSites;
  }

  public DexMethodHandle[] getMethodHandles() {
    return methodHandles;
  }

  public boolean hasJumboStrings() {
    return firstJumboString != null;
  }

  public DexString getFirstJumboString() {
    return firstJumboString;
  }

  private boolean isContainedInMapping(IndexedDexItem item) {
    return item.getVirtualFileIndex(virtualFileId) != IndexedDexItem.UNASSOCIATED_VALUE;
  }

  public int getOffsetFor(DexProto proto) {
    assert isContainedInMapping(proto) : "Missing dependency: " + proto;
    return proto.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexField field) {
    assert isContainedInMapping(field) : "Missing dependency: " + field;
    return field.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexMethod method) {
    assert isContainedInMapping(method) : "Missing dependency: " + method;
    return method.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexString string) {
    assert isContainedInMapping(string) : "Missing dependency: " + string;
    return string.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexType type) {
    assert isContainedInMapping(type) : "Missing dependency: " + type;
    return type.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexCallSite callSite) {
    assert isContainedInMapping(callSite) : "Missing dependency: " + callSite;
    return callSite.getVirtualFileIndex(virtualFileId);
  }

  public int getOffsetFor(DexMethodHandle methodHandle) {
    assert isContainedInMapping(methodHandle) : "Missing dependency: " + methodHandle;
    return methodHandle.getVirtualFileIndex(virtualFileId);
  }

  private static class SortingProgramClassVisitor extends ProgramClassVisitor {
    private final Set<DexClass> classSet = Sets.newIdentityHashSet();
    private final DexProgramClass[] sortedClasses;

    private int index = 0;

    public SortingProgramClassVisitor(DexApplication application, DexProgramClass[] classes) {
      super(application);
      this.sortedClasses = new DexProgramClass[classes.length];
      Collections.addAll(classSet, classes);
    }

    @Override
    public void visit(DexType type) {}

    @Override
    public void visit(DexClass clazz) {
      if (classSet.contains(clazz)) {
        assert index < sortedClasses.length;
        sortedClasses[index++] = (DexProgramClass) clazz;
      }
    }

    public DexProgramClass[] getSortedClasses() {
      assert index == sortedClasses.length;
      return sortedClasses;
    }
  }
}
