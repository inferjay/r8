// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.DexApplication.Builder;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeSuper;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

//
// Default and static interface method desugaring rewriter (note that lambda
// desugaring should have already processed the code before this rewriter).
//
// In short, during default and static interface method desugaring
// the following actions are performed:
//
//   (1) All static interface methods are moved into companion classes. All calls
//       to these methods are redirected appropriately. All references to these
//       methods from method handles are reported as errors.
//
// Companion class is a synthesized class (<interface-name>-CC) created to host
// static and former default interface methods (see below) from the interface.
//
//   (2) All default interface methods are made static and moved into companion
//       class.
//
//   (3) All calls to default interface methods made via 'super' are changed
//       to directly call appropriate static methods in companion classes.
//
//   (4) All other calls or references to default interface methods are not changed.
//
//   (5) For all program classes explicitly implementing interfaces we analyze the
//       set of default interface methods missing and add them, the created methods
//       forward the call to an appropriate method in interface companion class.
//
public final class InterfaceMethodRewriter {
  private static final String COMPANION_CLASS_NAME_SUFFIX = "-CC";
  private static final String DEFAULT_METHOD_PREFIX = "$default$";

  private final IRConverter converter;
  final DexItemFactory factory;

  // All forwarding methods generated during desugaring. We don't synchronize access
  // to this collection since it is only filled in ClassProcessor running synchronously.
  private final Set<DexEncodedMethod> forwardingMethods = Sets.newIdentityHashSet();

  /** Defines a minor variation in desugaring. */
  public enum Flavor {
    /** Process all application resources. */
    IncludeAllResources,
    /** Process all but DEX application resources. */
    ExcludeDexResources
  }

  public InterfaceMethodRewriter(IRConverter converter) {
    assert converter != null;
    this.converter = converter;
    this.factory = converter.application.dexItemFactory;
  }

  // Rewrites the references to static and default interface methods.
  // NOTE: can be called for different methods concurrently.
  public void rewriteMethodReferences(DexEncodedMethod encodedMethod, IRCode code) {
    if (forwardingMethods.contains(encodedMethod)) {
      return;
    }

    ListIterator<BasicBlock> blocks = code.listIterator();
    while (blocks.hasNext()) {
      BasicBlock block = blocks.next();
      InstructionListIterator instructions = block.listIterator();
      while (instructions.hasNext()) {
        Instruction instruction = instructions.next();

        if (instruction.isInvokeCustom()) {
          // Check that static interface methods are not referenced
          // from invoke-custom instructions via method handles.
          DexCallSite callSite = instruction.asInvokeCustom().getCallSite();
          reportStaticInterfaceMethodHandle(callSite.bootstrapMethod);
          for (DexValue arg : callSite.bootstrapArgs) {
            if (arg instanceof DexValue.DexValueMethodHandle) {
              reportStaticInterfaceMethodHandle(((DexValue.DexValueMethodHandle) arg).value);
            }
          }
          continue;
        }

        if (instruction.isInvokeStatic()) {
          InvokeStatic invokeStatic = instruction.asInvokeStatic();
          DexMethod method = invokeStatic.getInvokedMethod();
          if (isInterfaceClass(method.holder)) {
            // Retarget call to an appropriate method of companion class.
            instructions.replaceCurrentInstruction(
                new InvokeStatic(staticAsMethodOfCompanionClass(method),
                    invokeStatic.outValue(), invokeStatic.arguments()));
          }
          continue;
        }

        if (instruction.isInvokeSuper()) {
          InvokeSuper invokeSuper = instruction.asInvokeSuper();
          DexMethod method = invokeSuper.getInvokedMethod();
          if (isInterfaceClass(method.holder)) {
            // Retarget call to an appropriate method of companion class.
            instructions.replaceCurrentInstruction(
                new InvokeStatic(defaultAsMethodOfCompanionClass(method),
                    invokeSuper.outValue(), invokeSuper.arguments()));
          }
        }
      }
    }
  }

  private void reportStaticInterfaceMethodHandle(DexMethodHandle handle) {
    if (handle.type.isInvokeStatic() && isInterfaceClass(handle.asMethod().holder)) {
      throw new Unimplemented(
          "Desugaring of static interface method handle in is not yet supported.");
    }
  }

  private boolean isInterfaceClass(DexType type) {
    return findRequiredClass(type).isInterface();
  }

  // Returns the class for the type specified, report errors for missing classes.
  final DexClass findRequiredClass(DexType type) {
    DexClass clazz = converter.appInfo.definitionFor(type);
    if (clazz != null) {
      return clazz;
    }
    throw new CompilationError("Type '" + type.toSourceString() +
        "' required for default and static interface methods desugaring not found.");
  }

  // Gets the companion class for the interface `type`.
  final DexType getCompanionClassType(DexType type) {
    assert type.isClassType();
    String descriptor = type.descriptor.toString();
    String ccTypeDescriptor = descriptor.substring(0, descriptor.length() - 1)
        + COMPANION_CLASS_NAME_SUFFIX + ";";
    return factory.createType(ccTypeDescriptor);
  }

  private boolean isInMainDexList(DexType iface) {
    ImmutableSet<DexType> list = converter.application.mainDexList;
    return list.contains(iface);
  }

  // Represent a static interface method as a method of companion class.
  final DexMethod staticAsMethodOfCompanionClass(DexMethod method) {
    // No changes for static methods.
    return factory.createMethod(getCompanionClassType(method.holder), method.proto, method.name);
  }

  // Represent a default interface method as a method of companion class.
  final DexMethod defaultAsMethodOfCompanionClass(DexMethod method) {
    // Add an implicit argument to represent the receiver.
    DexType[] params = method.proto.parameters.values;
    DexType[] newParams = new DexType[params.length + 1];
    newParams[0] = method.holder;
    System.arraycopy(params, 0, newParams, 1, params.length);

    // Add prefix to avoid name conflicts.
    return factory.createMethod(getCompanionClassType(method.holder),
        factory.createProto(method.proto.returnType, newParams),
        factory.createString(DEFAULT_METHOD_PREFIX + method.name.toString()));
  }

  /**
   * Move static and default interface methods to companion classes,
   * add missing methods to forward to moved default methods implementation.
   */
  public void desugarInterfaceMethods(Builder builder, Flavor flavour) {
    // Process all classes first. Add missing forwarding methods to
    // replace desugared default interface methods.
    forwardingMethods.addAll(processClasses(builder, flavour));

    // Process interfaces, create companion class if needed, move static methods
    // to companion class, copy default interface methods to companion classes,
    // make original default methods abstract, remove bridge methods.
    Map<DexProgramClass, DexProgramClass> companionClasses =
        processInterfaces(builder, flavour);

    for (Map.Entry<DexProgramClass, DexProgramClass> entry : companionClasses.entrySet()) {
      // Don't need to optimize synthesized class since all of its methods
      // are just moved from interfaces and don't need to be re-processed.
      builder.addSynthesizedClass(entry.getValue(), isInMainDexList(entry.getKey().type));
    }

    for (DexEncodedMethod method : forwardingMethods) {
      converter.optimizeSynthesizedMethod(method);
    }
  }

  private static boolean shouldProcess(
      DexProgramClass clazz, Flavor flavour, boolean mustBeInterface) {
    return (clazz.getOrigin() != Resource.Kind.DEX || flavour == Flavor.IncludeAllResources)
        && clazz.isInterface() == mustBeInterface;
  }

  private Map<DexProgramClass, DexProgramClass> processInterfaces(
      Builder builder, Flavor flavour) {
    InterfaceProcessor processor = new InterfaceProcessor(this);
    for (DexProgramClass clazz : builder.getProgramClasses()) {
      if (shouldProcess(clazz, flavour, true)) {
        processor.process(clazz.asProgramClass());
      }
    }
    return processor.companionClasses;
  }

  private Set<DexEncodedMethod> processClasses(Builder builder, Flavor flavour) {
    ClassProcessor processor = new ClassProcessor(this);
    for (DexProgramClass clazz : builder.getProgramClasses()) {
      if (shouldProcess(clazz, flavour, false)) {
        processor.process(clazz);
      }
    }
    return processor.getForwardMethods();
  }

  final boolean isDefaultMethod(DexEncodedMethod method) {
    assert !method.accessFlags.isConstructor();
    assert !method.accessFlags.isStatic();

    if (method.accessFlags.isAbstract()) {
      return false;
    }
    if (method.accessFlags.isNative()) {
      throw new Unimplemented("Native default interface methods are not yet supported.");
    }
    if (!method.accessFlags.isPublic()) {
      // NOTE: even though the class is allowed to have non-public interface methods
      // with code, for example private methods, all such methods we are aware of are
      // created by the compiler for stateful lambdas and they must be converted into
      // static methods by lambda desugaring by this time.
      throw new Unimplemented("Non public default interface methods are not yet supported.");
    }
    return true;
  }
}
