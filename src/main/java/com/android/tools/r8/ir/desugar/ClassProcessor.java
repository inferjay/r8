// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.synthetic.ForwardMethodSourceCode;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Default and static method interface desugaring processor for classes.
// Adds default interface methods into the class when needed.
final class ClassProcessor {

  private final InterfaceMethodRewriter rewriter;
  // Set of already processed classes.
  private final Set<DexClass> processedClasses = Sets.newIdentityHashSet();
  // Maps already created methods into default methods they were generated based on.
  private final Map<DexEncodedMethod, DexEncodedMethod> createdMethods = new IdentityHashMap<>();
  // Caches default interface method info for already processed interfaces.
  private final Map<DexType, DefaultMethodsHelper.Collection> cache = new IdentityHashMap<>();

  ClassProcessor(InterfaceMethodRewriter rewriter) {
    this.rewriter = rewriter;
  }

  final Set<DexEncodedMethod> getForwardMethods() {
    return createdMethods.keySet();
  }

  final void process(DexClass clazz) {
    if (clazz.isInterface()) {
      throw new CompilationError("Interface in superclass chain.");
    }
    if (!clazz.isProgramClass()) {
      // We assume that library classes don't need to be processed, since they
      // are provided by a runtime not supporting default interface methods.
      // We also skip classpath classes, which results in sub-optimal behavior
      // in case classpath superclass when processed adds a default method which
      // could have been reused in this class otherwise.
      return;
    }
    if (!processedClasses.add(clazz)) {
      return; // Has already been processed.
    }

    // Ensure superclasses are processed first. We need it since we use information
    // about methods added to superclasses when we decide if we want to add a default
    // method to class `clazz`.
    DexType superType = clazz.superType;
    if (superType != null && superType != rewriter.factory.objectType) {
      process(rewriter.findRequiredClass(superType));
    }

    if (clazz.interfaces.isEmpty()) {
      // Since superclass has already been processed and it has all missing methods
      // added, these methods will be inherited by `clazz`, and only need to be revised
      // in case this class has *additional* interfaces implemented, which may change
      // the entire picture of the default method selection in runtime.
      return;
    }

    // Collect the default interface methods to be added to this class.
    List<DexEncodedMethod> methodsToImplement = collectMethodsToImplement(clazz);
    if (methodsToImplement.isEmpty()) {
      return;
    }

    // Add the methods.
    DexEncodedMethod[] existing = clazz.virtualMethods();
    clazz.setVirtualMethods(new DexEncodedMethod[existing.length + methodsToImplement.size()]);
    System.arraycopy(existing, 0, clazz.virtualMethods(), 0, existing.length);

    for (int i = 0; i < methodsToImplement.size(); i++) {
      DexEncodedMethod method = methodsToImplement.get(i);
      assert method.accessFlags.isPublic() && !method.accessFlags.isAbstract();
      DexEncodedMethod newMethod = addForwardingMethod(method, clazz);
      clazz.virtualMethods()[existing.length + i] = newMethod;
      createdMethods.put(newMethod, method);
    }
  }

  private DexEncodedMethod addForwardingMethod(DexEncodedMethod defaultMethod, DexClass clazz) {
    DexMethod method = defaultMethod.method;
    assert !rewriter.findRequiredClass(method.holder).isLibraryClass();
    // New method will have the same name, proto, and also all the flags of the
    // default method, including bridge flag.
    DexMethod newMethod = rewriter.factory.createMethod(clazz.type, method.proto, method.name);
    DexAccessFlags newFlags = new DexAccessFlags(defaultMethod.accessFlags.get());
    return new DexEncodedMethod(newMethod, newFlags,
        defaultMethod.annotations, defaultMethod.parameterAnnotations,
        new SynthesizedCode(new ForwardMethodSourceCode(
            clazz.type, method.proto, /* static method */ null,
            rewriter.defaultAsMethodOfCompanionClass(method),
            Invoke.Type.STATIC)));
  }

  // For a given class `clazz` inspects all interfaces it implements directly or
  // indirectly and collect a set of all default methods to be implemented
  // in this class.
  private List<DexEncodedMethod> collectMethodsToImplement(DexClass clazz) {
    DefaultMethodsHelper helper = new DefaultMethodsHelper();

    // Collect candidate default methods by inspecting interfaces implemented
    // by this class as well as its superclasses.
    DexClass current = clazz;
    while (true) {
      for (DexType type : current.interfaces.values) {
        helper.merge(getOrCreateInterfaceInfo(type));
      }

      DexType superType = current.superType;
      if (superType == null || superType == rewriter.factory.objectType) {
        // We assume here that interfaces implemented by java.lang.Object don't
        // have default methods and don't hide any default interface methods since
        // they must be library interfaces.
        break;
      }
      current = rewriter.findRequiredClass(superType);
    }

    List<DexEncodedMethod> candidates = helper.createCandidatesList();
    if (candidates.isEmpty()) {
      return candidates;
    }

    // Remove from candidates methods defined in class or any of its superclasses.
    List<DexEncodedMethod> toBeImplemented = new ArrayList<>(candidates.size());
    current = clazz;
    while (true) {
      // Hide candidates by virtual method of the class.
      hideCandidates(current.virtualMethods(), candidates, toBeImplemented);
      if (candidates.isEmpty()) {
        return toBeImplemented;
      }

      DexType superType = current.superType;
      if (superType == null || superType == rewriter.factory.objectType) {
        // Note that default interface methods must never have same
        // name/signature as any method in java.lang.Object (JLS §9.4.1.2).

        // Everything still in candidate list is not hidden.
        toBeImplemented.addAll(candidates);
        return toBeImplemented;
      }
      current = rewriter.findRequiredClass(superType);
    }
  }

  private void hideCandidates(DexEncodedMethod[] virtualMethods,
      List<DexEncodedMethod> candidates, List<DexEncodedMethod> toBeImplemented) {
    Iterator<DexEncodedMethod> it = candidates.iterator();
    while (it.hasNext()) {
      DexEncodedMethod candidate = it.next();
      for (DexEncodedMethod encoded : virtualMethods) {
        if (candidate.method.match(encoded)) {
          // Found a methods hiding the candidate.
          DexEncodedMethod basedOnCandidate = createdMethods.get(encoded);
          if (basedOnCandidate != null) {
            // The method we found is a method we have generated for a default interface
            // method in a superclass. If the method is based on the same candidate we don't
            // need to re-generate this method again since it is going to be inherited.
            if (basedOnCandidate != candidate) {
              // Need to re-generate since the inherited version is
              // based on a different candidate.
              toBeImplemented.add(candidate);
            }
          }

          // Done with this candidate.
          it.remove();
          break;
        }
      }
    }
  }

  private DefaultMethodsHelper.Collection getOrCreateInterfaceInfo(DexType iface) {
    DefaultMethodsHelper.Collection collection = cache.get(iface);
    if (collection != null) {
      return collection;
    }
    collection = createInterfaceInfo(iface);
    cache.put(iface, collection);
    return collection;
  }

  private DefaultMethodsHelper.Collection createInterfaceInfo(DexType iface) {
    DefaultMethodsHelper helper = new DefaultMethodsHelper();
    DexClass clazz = rewriter.findRequiredClass(iface);
    if (!clazz.isInterface()) {
      throw new CompilationError(
          "Type " + iface.toSourceString() + " is expected to be an interface.");
    }
    if (clazz.isLibraryClass()) {
      // For library interfaces we always assume there are no default
      // methods, since the interface is part of framework provided by
      // runtime which does not support default interface methods.
      return DefaultMethodsHelper.Collection.EMPTY;
    }

    // Merge information from all superinterfaces.
    for (DexType superinterface : clazz.interfaces.values) {
      helper.merge(getOrCreateInterfaceInfo(superinterface));
    }

    // Hide by virtual methods of this interface.
    for (DexEncodedMethod virtual : clazz.virtualMethods()) {
      helper.hideMatches(virtual.method);
    }

    // Add all default methods of this interface.
    for (DexEncodedMethod encoded : clazz.virtualMethods()) {
      if (rewriter.isDefaultMethod(encoded)) {
        helper.addDefaultMethod(encoded);
      }
    }

    return helper.wrapInCollection();
  }
}
