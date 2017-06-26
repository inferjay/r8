// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import static com.android.tools.r8.ir.desugar.InterfaceMethodRewriter.Flavor.ExcludeDexResources;
import static com.android.tools.r8.ir.desugar.InterfaceMethodRewriter.Flavor.IncludeAllResources;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexApplication.Builder;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.desugar.InterfaceMethodRewriter;
import com.android.tools.r8.ir.desugar.LambdaRewriter;
import com.android.tools.r8.ir.optimize.CodeRewriter;
import com.android.tools.r8.ir.optimize.DeadCodeRemover;
import com.android.tools.r8.ir.optimize.Inliner;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import com.android.tools.r8.ir.optimize.MemberValuePropagation;
import com.android.tools.r8.ir.optimize.Outliner;
import com.android.tools.r8.ir.optimize.PeepholeOptimizer;
import com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

public class IRConverter {

  public static final int PEEPHOLE_OPTIMIZATION_PASSES = 2;

  private final Timing timing;
  public final DexApplication application;
  public final AppInfo appInfo;
  private final Outliner outliner;
  private final LambdaRewriter lambdaRewriter;
  private final InterfaceMethodRewriter interfaceMethodRewriter;
  private final InternalOptions options;
  private final CfgPrinter printer;
  private final GraphLense graphLense;
  private final CodeRewriter codeRewriter;
  private final MemberValuePropagation memberValuePropagation;
  private final LensCodeRewriter lensCodeRewriter;
  private final Inliner inliner;
  private CallGraph callGraph;
  private OptimizationFeedback ignoreOptimizationFeedback = new OptimizationFeedbackIgnore();

  private DexString highestSortingString;

  public IRConverter(
      DexApplication application,
      AppInfo appInfo,
      InternalOptions options,
      CfgPrinter printer,
      boolean enableDesugaring) {
    this.timing = new Timing("Testing");
    this.application = application;
    this.appInfo = appInfo;
    this.options = options;
    this.printer = printer;
    this.graphLense = GraphLense.getIdentityLense();
    this.inliner = null;
    this.outliner = null;
    this.codeRewriter = new CodeRewriter(appInfo);
    this.memberValuePropagation = null;
    this.lambdaRewriter = enableDesugaring ? new LambdaRewriter(this) : null;
    this.interfaceMethodRewriter =
        (enableDesugaring && enableInterfaceMethodDesugaring())
            ? new InterfaceMethodRewriter(this) : null;
    lensCodeRewriter = null;
    markLibraryMethodsReturningReceiver();
  }

  public IRConverter(
      DexApplication application, AppInfo appInfo, InternalOptions options, CfgPrinter printer) {
    this(application, appInfo, options, printer, true);
  }

  public IRConverter(
      Timing timing,
      DexApplication application,
      AppInfoWithSubtyping appInfo,
      InternalOptions options,
      CfgPrinter printer,
      GraphLense graphLense) {
    this.timing = timing;
    this.application = application;
    this.appInfo = appInfo;
    this.options = options;
    this.printer = printer;
    this.graphLense = graphLense;
    this.codeRewriter = new CodeRewriter(appInfo);
    this.outliner = new Outliner(appInfo, options);
    this.memberValuePropagation = new MemberValuePropagation(appInfo);
    this.inliner = new Inliner(appInfo, graphLense, options);
    this.lambdaRewriter = new LambdaRewriter(this);
    this.interfaceMethodRewriter = enableInterfaceMethodDesugaring()
        ? new InterfaceMethodRewriter(this) : null;
    lensCodeRewriter = new LensCodeRewriter(graphLense, appInfo);
    markLibraryMethodsReturningReceiver();
  }

  private boolean enableInterfaceMethodDesugaring() {
    switch (options.interfaceMethodDesugaring) {
      case Off:
        return false;
      case Auto:
        return !options.canUseDefaultAndStaticInterfaceMethods();
    }
    throw new Unreachable();
  }

  private boolean enableTryWithResourcesDesugaring() {
    switch (options.tryWithResourcesDesugaring) {
      case Off:
        return false;
      case Auto:
        return !options.canUseSuppressedExceptions();
    }
    throw new Unreachable();
  }

  private void markLibraryMethodsReturningReceiver() {
    DexItemFactory dexItemFactory = appInfo.dexItemFactory;
    dexItemFactory.stringBuilderMethods.forEeachAppendMethod(this::markReturnsReceiver);
    dexItemFactory.stringBufferMethods.forEeachAppendMethod(this::markReturnsReceiver);
  }

  private void markReturnsReceiver(DexMethod method) {
    DexEncodedMethod definition = appInfo.definitionFor(method);
    if (definition != null) {
      definition.markReturnsArgument(0);
    }
  }

  private void removeLambdaDeserializationMethods() {
    if (lambdaRewriter != null) {
      lambdaRewriter.removeLambdaDeserializationMethods(application.classes());
    }
  }

  private void synthesizeLambdaClasses(Builder builder) {
    if (lambdaRewriter != null) {
      lambdaRewriter.adjustAccessibility();
      lambdaRewriter.synthesizeLambdaClasses(builder);
    }
  }

  private void desugarInterfaceMethods(
      Builder builder, InterfaceMethodRewriter.Flavor includeAllResources) {
    if (interfaceMethodRewriter != null) {
      interfaceMethodRewriter.desugarInterfaceMethods(builder, includeAllResources);
    }
  }

  public DexApplication convertToDex() {
    removeLambdaDeserializationMethods();

    convertClassesToDex(application.classes());

    // Build a new application with jumbo string info,
    Builder builder = new Builder(application);
    builder.setHighestSortingString(highestSortingString);

    synthesizeLambdaClasses(builder);
    desugarInterfaceMethods(builder, ExcludeDexResources);

    return builder.build();
  }

  private void convertClassesToDex(Iterable<DexProgramClass> classes) {
    for (DexProgramClass clazz : classes) {
      convertMethodsToDex(clazz.directMethods());
      convertMethodsToDex(clazz.virtualMethods());
    }
  }

  private void convertMethodsToDex(DexEncodedMethod[] methods) {
    for (int i = 0; i < methods.length; i++) {
      DexEncodedMethod method = methods[i];
      if (method.getCode() != null) {
        boolean matchesMethodFilter = options.methodMatchesFilter(method);
        if (matchesMethodFilter) {
          if (method.getCode().isJarCode()) {
            rewriteCode(method, ignoreOptimizationFeedback, Outliner::noProcessing);
          }
          updateHighestSortingStrings(method);
        }
      }
    }
  }

  public DexApplication optimize() throws ExecutionException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      return optimize(executor);
    } finally {
      executor.shutdown();
    }
  }

  public DexApplication optimize(ExecutorService executorService) throws ExecutionException {
    removeLambdaDeserializationMethods();

    timing.begin("Build call graph");
    callGraph = CallGraph.build(application, appInfo.withSubtyping(), graphLense);
    timing.end();

    // The process is in two phases.
    // 1) Subject all DexEncodedMethods to optimization (except outlining).
    //    - a side effect is candidates for outlining are identified.
    // 2) Perform outlining for the collected candidates.
    // Ideally, we should outline eagerly when threshold for a template has been reached.

    // Process the application identifying outlining candidates.
    timing.begin("IR conversion phase 1");
    int count = 0;
    OptimizationFeedback directFeedback = new OptimizationFeedbackDirect();
    OptimizationFeedbackDelayed delayedFeedback = new OptimizationFeedbackDelayed();
    while (!callGraph.isEmpty()) {
      count++;
      CallGraph.Leaves leaves = callGraph.pickLeaves();
      List<DexEncodedMethod> methods = leaves.getLeaves();
      assert methods.size() > 0;
      List<Future<?>> futures = new ArrayList<>();

      // For testing we have the option to determine the processing order of the methods.
      if (options.testing.irOrdering != null) {
        methods = options.testing.irOrdering.apply(methods, leaves);
      }

      while (methods.size() > 0) {
        // Process the methods on multiple threads. If cycles where broken collect the
        // optimization feedback to reprocess methods affected by it. This is required to get
        // deterministic behaviour, as the processing order within each set of leaves is
        // non-deterministic.
        for (DexEncodedMethod method : methods) {
          futures.add(executorService.submit(() -> {
            processMethod(method,
                leaves.brokeCycles() ? delayedFeedback : directFeedback,
                outliner == null ? Outliner::noProcessing : outliner::identifyCandidates);
          }));
        }
        ThreadUtils.awaitFutures(futures);
        if (leaves.brokeCycles()) {
          // If cycles in the call graph were broken, then re-process all methods which are
          // affected by the optimization feedback of other methods in this group.
          methods = delayedFeedback.applyAndClear(methods, leaves);
        } else {
          methods = ImmutableList.of();
        }
      }
    }
    timing.end();

    // Build a new application with jumbo string info.
    Builder builder = new Builder(application);
    builder.setHighestSortingString(highestSortingString);

    // Second inlining pass.
    if ((inliner != null) && (inliner.doubleInlineCallers.size() > 0)) {
      inliner.applyDoubleInlining = true;
      for (DexEncodedMethod method : inliner.doubleInlineCallers) {
        processMethod(method, ignoreOptimizationFeedback, Outliner::noProcessing);
        assert method.isProcessed();
      }
    }

    synthesizeLambdaClasses(builder);
    desugarInterfaceMethods(builder, IncludeAllResources);

    if (outliner != null) {
      timing.begin("IR conversion phase 2");
      // Compile all classes flagged for outlining and
      // add the outline support class IF needed.
      DexProgramClass outlineClass = prepareOutlining();
      if (outlineClass != null) {
        // Process the selected methods for outlining.
        for (DexEncodedMethod method : outliner.getMethodsSelectedForOutlining()) {
          // This is the second time we compile this method first mark it not processed.
          assert !method.getCode().isOutlineCode();
          processMethod(method, ignoreOptimizationFeedback, outliner::applyOutliningCandidate);
          assert method.isProcessed();
        }
        builder.addSynthesizedClass(outlineClass, true);
        clearDexMethodCompilationState(outlineClass);
      }
      timing.end();
    }
    clearDexMethodCompilationState();
    return builder.build();
  }

  public void processJumboStrings(DexEncodedMethod method, DexString firstJumboString) {
    convertMethodJumboStringsOnly(method, firstJumboString);
  }

  private void clearDexMethodCompilationState() {
    application.classes().forEach(this::clearDexMethodCompilationState);
  }

  private void clearDexMethodCompilationState(DexProgramClass clazz) {
    Arrays.stream(clazz.directMethods()).forEach(DexEncodedMethod::markNotProcessed);
    Arrays.stream(clazz.virtualMethods()).forEach(DexEncodedMethod::markNotProcessed);
  }

  /**
   * This will replace the Dex code in the method with the Dex code generated from the provided IR.
   *
   * This method is *only* intended for testing, where tests manipulate the IR and need runnable Dex
   * code.
   *
   * @param method the method to replace code for
   * @param code the IR code for the method
   */
  public void replaceCodeForTesting(DexEncodedMethod method, IRCode code) {
    if (Log.ENABLED) {
      Log.debug(getClass(), "Initial (SSA) flow graph for %s:\n%s", method.toSourceString(), code);
    }
    assert code.isConsistentSSA();
    RegisterAllocator registerAllocator = performRegisterAllocation(code, method);
    method.setCode(code, registerAllocator, appInfo.dexItemFactory);
    if (Log.ENABLED) {
      Log.debug(getClass(), "Resulting dex code for %s:\n%s",
          method.toSourceString(), logCode(options, method));
    }
  }

  // Find an unused name for the outlining class. When multiple runs produces additional
  // outlining the default outlining class might already be present.
  private DexType computeOutlineClassType() {
    DexType result;
    int count = 0;
    do {
      String name = options.outline.className + (count == 0 ? "" : Integer.toString(count));
      count++;
      result = application.dexItemFactory.createType(DescriptorUtils.javaTypeToDescriptor(name));
    } while (application.definitionFor(result) != null);
    return result;
  }

  private DexProgramClass prepareOutlining() {
    if (!outliner.selectMethodsForOutlining()) {
      return null;
    }
    DexProgramClass outlineClass = outliner.buildOutlinerClass(computeOutlineClassType());
    optimizeSynthesizedClass(outlineClass);
    return outlineClass;
  }

  public void optimizeSynthesizedClass(DexProgramClass clazz) {
    // Process the generated class, but don't apply any outlining.
    for (DexEncodedMethod method : clazz.directMethods()) {
      optimizeSynthesizedMethod(method);
    }
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      optimizeSynthesizedMethod(method);
    }
  }

  public void optimizeSynthesizedMethod(DexEncodedMethod method) {
    // Process the generated method, but don't apply any outlining.
    processMethod(method, ignoreOptimizationFeedback, Outliner::noProcessing);
  }

  private String logCode(InternalOptions options, DexEncodedMethod method) {
    return options.useSmaliSyntax ? method.toSmaliString(null) : method.codeToString();
  }

  private void processMethod(DexEncodedMethod method,
      OptimizationFeedback feedback,
      BiConsumer<IRCode, DexEncodedMethod> outlineHandler) {
    Code code = method.getCode();
    boolean matchesMethodFilter = options.methodMatchesFilter(method);
    if (code != null && matchesMethodFilter) {
      rewriteCode(method, feedback, outlineHandler);
    } else {
      // Mark abstract methods as processed as well.
      method.markProcessed(Constraint.NEVER);
    }
  }

  private void rewriteCode(DexEncodedMethod method,
      OptimizationFeedback feedback,
      BiConsumer<IRCode, DexEncodedMethod> outlineHandler) {
    if (options.verbose) {
      System.out.println("Processing: " + method.toSourceString());
    }
    if (Log.ENABLED) {
      Log.debug(getClass(), "Original code for %s:\n%s",
          method.toSourceString(), logCode(options, method));
    }
    IRCode code = method.buildIR(options);
    if (code == null) {
      feedback.markProcessed(method, Constraint.NEVER);
      return;
    }
    if (Log.ENABLED) {
      Log.debug(getClass(), "Initial (SSA) flow graph for %s:\n%s", method.toSourceString(), code);
    }
    // Compilation header if printing CFGs for this method.
    printC1VisualizerHeader(method);
    printMethod(code, "Initial IR (SSA)");

    if (lensCodeRewriter != null) {
      lensCodeRewriter.rewrite(code, method);
    } else {
      assert graphLense.isIdentityLense();
    }
    if (memberValuePropagation != null) {
      memberValuePropagation.rewriteWithConstantValues(code);
    }
    if (options.removeSwitchMaps) {
      codeRewriter.removeSwitchMaps(code);
    }
    if (options.inlineAccessors && inliner != null) {
      inliner.performInlining(method, code, callGraph);
    }
    codeRewriter.rewriteLongCompareAndRequireNonNull(code, options.canUseObjectsNonNull());
    codeRewriter.commonSubexpressionElimination(code);
    codeRewriter.simplifyArrayConstruction(code);
    codeRewriter.rewriteMoveResult(code);
    codeRewriter.splitConstants(code);
    codeRewriter.foldConstants(code);
    codeRewriter.simplifyIf(code);
    if (Log.ENABLED) {
      Log.debug(getClass(), "Intermediate (SSA) flow graph for %s:\n%s",
          method.toSourceString(), code);
    }
    // Dead code removal. Performed after simplifications to remove code that becomes dead
    // as a result of those simplifications. The following optimizations could reveal more
    // dead code which is removed right before register allocation in performRegisterAllocation.
    DeadCodeRemover.removeDeadCode(code, codeRewriter, options);
    assert code.isConsistentSSA();

    if (enableTryWithResourcesDesugaring()) {
      codeRewriter.rewriteThrowableAddAndGetSuppressed(code);
    }

    if (lambdaRewriter != null) {
      lambdaRewriter.desugarLambdas(method, code);
      assert code.isConsistentSSA();
    }

    if (interfaceMethodRewriter != null) {
      interfaceMethodRewriter.rewriteMethodReferences(method, code);
      assert code.isConsistentSSA();
    }

    if (options.outline.enabled) {
      outlineHandler.accept(code, method);
      assert code.isConsistentSSA();
    }

    codeRewriter.shortenLiveRanges(code);
    codeRewriter.identifyReturnsArgument(method, code, feedback);

    // Insert code to log arguments if requested.
    if (options.methodMatchesLogArgumentsFilter(method)) {
      codeRewriter.logArgumentTypes(method, code);
    }

    printMethod(code, "Optimized IR (SSA)");
    // Perform register allocation.
    RegisterAllocator registerAllocator = performRegisterAllocation(code, method);
    method.setCode(code, registerAllocator, appInfo.dexItemFactory);
    updateHighestSortingStrings(method);
    if (Log.ENABLED) {
      Log.debug(getClass(), "Resulting dex code for %s:\n%s",
          method.toSourceString(), logCode(options, method));
    }
    printMethod(code, "Final IR (non-SSA)");

    // After all the optimizations have take place, we compute whether method should be inlined.
    Constraint state;
    if (!options.inlineAccessors || inliner == null) {
      state = Constraint.NEVER;
    } else {
      state = inliner.identifySimpleMethods(code, method);
    }
    feedback.markProcessed(method, state);
  }

  private void updateHighestSortingStrings(DexEncodedMethod method) {
    DexString highestSortingReferencedString = method.getCode().asDexCode().highestSortingString;
    if (highestSortingReferencedString != null) {
      if (highestSortingString == null
          || highestSortingReferencedString.slowCompareTo(highestSortingString) > 0) {
        highestSortingString = highestSortingReferencedString;
      }
    }
  }

  // Convert a method ensuring that strings sorting equal or higher than the argument
  // firstJumboString are encoded as jumbo strings.
  // TODO(sgjesse): Consider replacing this with a direct dex2dex converter instead of going
  // through IR.
  private void convertMethodJumboStringsOnly(
      DexEncodedMethod method, DexString firstJumboString) {
    // This is only used for methods already converted to Dex, but missing jumbo strings.
    assert method.getCode() != null && method.getCode().isDexCode();
    if (options.verbose) {
      System.out.println("Processing jumbo strings: " + method.toSourceString());
    }
    if (Log.ENABLED) {
      Log.debug(getClass(), "Original code for %s:\n%s",
          method.toSourceString(), logCode(options, method));
    }
    IRCode code = method.buildIR(options);
    if (Log.ENABLED) {
      Log.debug(getClass(), "Initial (SSA) flow graph for %s:\n%s",
          method.toSourceString(), code);
    }
    // Compilation header if printing CFGs for this method.
    printC1VisualizerHeader(method);
    printMethod(code, "Initial IR (SSA)");

    // Methods passed through here should have been through IR processing already and
    // therefore, we skip most of the IR processing.

    // Perform register allocation.
    RegisterAllocator registerAllocator = performRegisterAllocation(code, method);
    method.setCode(code, registerAllocator, appInfo.dexItemFactory, firstJumboString);

    if (Log.ENABLED) {
      Log.debug(getClass(), "Resulting dex code for %s:\n%s",
          method.toSourceString(), logCode(options, method));
    }
    printMethod(code, "Final IR (non-SSA)");
  }

  private RegisterAllocator performRegisterAllocation(IRCode code, DexEncodedMethod method) {
    // Always perform dead code elimination before register allocation. The register allocator
    // does not allow dead code (to make sure that we do not waste registers for unneeded values).
    DeadCodeRemover.removeDeadCode(code, codeRewriter, options);
    LinearScanRegisterAllocator registerAllocator = new LinearScanRegisterAllocator(code, options);
    registerAllocator.allocateRegisters(options.debug);
    printMethod(code, "After register allocation (non-SSA)");
    printLiveRanges(registerAllocator, "Final live ranges.");
    if (!options.debug) {
      CodeRewriter.removedUnneededDebugPositions(code);
    }
    for (int i = 0; i < PEEPHOLE_OPTIMIZATION_PASSES; i++) {
      CodeRewriter.collapsTrivialGotos(method, code);
      PeepholeOptimizer.optimize(code, registerAllocator);
    }
    CodeRewriter.collapsTrivialGotos(method, code);
    if (Log.ENABLED) {
      Log.debug(getClass(), "Final (non-SSA) flow graph for %s:\n%s",
          method.toSourceString(), code);
    }
    return registerAllocator;
  }

  private void printC1VisualizerHeader(DexEncodedMethod method) {
    if (printer != null) {
      printer.begin("compilation");
      printer.print("name \"").append(method.toSourceString()).append("\"").ln();
      printer.print("method \"").append(method.toSourceString()).append("\"").ln();
      printer.print("date 0").ln();
      printer.end("compilation");
    }
  }

  private void printMethod(IRCode code, String title) {
    if (printer != null) {
      printer.resetUnusedValue();
      printer.begin("cfg");
      printer.print("name \"").append(title).append("\"\n");
      code.print(printer);
      printer.end("cfg");
    }
  }

  private void printLiveRanges(LinearScanRegisterAllocator allocator, String title) {
    if (printer != null) {
      allocator.print(printer, title);
    }
  }
}
