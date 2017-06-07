package com.android.tools.r8.graph;

import com.android.tools.r8.Resource;

/** Kind of the application class. Can be program, classpath or library. */
public enum ClassKind {
  PROGRAM(DexProgramClass::new),
  CLASSPATH(DexClasspathClass::new),
  LIBRARY(DexLibraryClass::new);

  private interface Factory {
    DexClass create(DexType type, Resource.Kind origin, DexAccessFlags accessFlags,
        DexType superType,
        DexTypeList interfaces, DexString sourceFile, DexAnnotationSet annotations,
        DexEncodedField[] staticFields, DexEncodedField[] instanceFields,
        DexEncodedMethod[] directMethods, DexEncodedMethod[] virtualMethods);
  }

  private final Factory factory;

  ClassKind(Factory factory) {
    this.factory = factory;
  }

  public DexClass create(
      DexType type, Resource.Kind origin, DexAccessFlags accessFlags, DexType superType,
      DexTypeList interfaces, DexString sourceFile, DexAnnotationSet annotations,
      DexEncodedField[] staticFields, DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods, DexEncodedMethod[] virtualMethods) {
    return factory.create(type, origin, accessFlags, superType, interfaces, sourceFile,
        annotations, staticFields, instanceFields, directMethods, virtualMethods);
  }
}
