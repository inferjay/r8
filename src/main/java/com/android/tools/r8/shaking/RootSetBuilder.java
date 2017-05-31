// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.ProguardTypeMatcher.MatchSpecificType;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.Sets;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RootSetBuilder {

  private DexApplication application;
  private final AppInfo appInfo;
  private final List<ProguardConfigurationRule> rules;
  private final Map<DexItem, ProguardKeepRule> noShrinking = new IdentityHashMap<>();
  private final Set<DexItem> noOptimization = Sets.newIdentityHashSet();
  private final Set<DexItem> noObfuscation = Sets.newIdentityHashSet();
  private final Set<DexItem> reasonAsked = Sets.newIdentityHashSet();
  private final Set<DexItem> keepPackageName = Sets.newIdentityHashSet();
  private final Set<ProguardConfigurationRule> rulesThatUseExtendsOrImplementsWrong =
      Sets.newIdentityHashSet();
  private final Set<DexItem> checkDiscarded = Sets.newIdentityHashSet();
  private final Map<DexItem, Map<DexItem, ProguardKeepRule>> dependentNoShrinking =
      new IdentityHashMap<>();
  private final Map<DexItem, ProguardMemberRule> noSideEffects = new IdentityHashMap<>();
  private final Map<DexItem, ProguardMemberRule> assumedValues = new IdentityHashMap<>();

  public RootSetBuilder(DexApplication application, AppInfo appInfo,
      List<ProguardConfigurationRule> rules) {
    this.application = application;
    this.appInfo = appInfo;
    this.rules = rules;
  }

  private boolean anySuperTypeMatches(DexType type, ProguardTypeMatcher name,
      ProguardTypeMatcher annotation) {
    while (type != null) {
      DexClass clazz = application.definitionFor(type);
      if (clazz == null) {
        // TODO(herhut): Warn about broken supertype chain?
        return false;
      }
      if (name.matches(clazz.type) && containsAnnotation(annotation, clazz.annotations)) {
        return true;
      }
      type = clazz.superType;
    }
    return false;
  }

  private boolean anyImplementedInterfaceMatches(DexClass clazz,
      ProguardTypeMatcher className, ProguardTypeMatcher annotation) {
    if (clazz == null) {
      return false;
    }
    for (DexType iface : clazz.interfaces.values) {
      DexClass ifaceClass = application.definitionFor(iface);
      if (ifaceClass == null) {
        // TODO(herhut): Warn about broken supertype chain?
        return false;
      }
      // TODO(herhut): Maybe it would be better to do this breadth first.
      if (className.matches(iface) && containsAnnotation(annotation, ifaceClass.annotations)
          || anyImplementedInterfaceMatches(ifaceClass, className, annotation)) {
        return true;
      }
    }
    if (clazz.superType == null) {
      return false;
    }
    DexClass superClass = application.definitionFor(clazz.superType);
    if (superClass == null) {
      // TODO(herhut): Warn about broken supertype chain?
      return false;
    }
    return anyImplementedInterfaceMatches(superClass, className, annotation);
  }

  // Returns a list of types iff the keep rule only contains specific type matches.
  // Otherwise, null is returned.
  private DexType[] specificDexTypes(ProguardConfigurationRule rule) {
    for (ProguardTypeMatcher matcher : rule.getClassNames()) {
      if (!(matcher instanceof MatchSpecificType)) {
        return null;
      }
    }
    final int length = rule.getClassNames().size();
    DexType[] result = new DexType[length];
    for (int i = 0; i < length; i++) {
      result[i] = ((MatchSpecificType) rule.getClassNames().get(i)).type;
    }
    return result;
  }

  // Process a class with the keep rule.
  private void process(DexClass clazz, ProguardConfigurationRule rule) {
    if (!clazz.accessFlags.containsAllOf(rule.getClassAccessFlags())) {
      return;
    }
    if (!clazz.accessFlags.containsNoneOf(rule.getNegatedClassAccessFlags())) {
      return;
    }
    if (!containsAnnotation(rule.getClassAnnotation(), clazz.annotations)) {
      return;
    }

    // In principle it should make a difference whether the user specified in a class
    // spec that a class either extends or implements another type. However, proguard
    // seems not to care, so users have started to use this inconsistently. We are thus
    // inconsistent, as well, but tell them.
    // TODO(herhut): One day make this do what it says.
    if (rule.hasInheritanceClassName()) {
      boolean extendsExpected =
          anySuperTypeMatches(clazz.superType, rule.getInheritanceClassName(),
              rule.getInheritanceAnnotation());
      boolean implementsExpected = false;
      if (!extendsExpected) {
        implementsExpected =
            anyImplementedInterfaceMatches(clazz, rule.getInheritanceClassName(),
                rule.getInheritanceAnnotation());
      }
      if (!extendsExpected && !implementsExpected) {
        return;
      }
      // Warn if users got it wrong, but only warn once.
      if (extendsExpected && !rule.getInheritanceIsExtends()) {
        if (rulesThatUseExtendsOrImplementsWrong.add(rule)) {
          System.err.println(
              "The rule `" + rule + "` uses implements but actually matches extends.");
        }
      } else if (implementsExpected && rule.getInheritanceIsExtends()) {
        if (rulesThatUseExtendsOrImplementsWrong.add(rule)) {
          System.err.println(
              "The rule `" + rule + "` uses extends but actually matches implements.");
        }
      }
    }

    for (ProguardTypeMatcher className : rule.getClassNames()) {
      if (className.matches(clazz.type)) {
        Collection<ProguardMemberRule> memberKeepRules = rule.getMemberRules();
        if (rule instanceof ProguardKeepRule) {
          switch (((ProguardKeepRule) rule).getType()) {
            case KEEP_CLASS_MEMBERS: {
              markMatchingVisibleMethods(clazz, memberKeepRules, rule, clazz.type);
              markMatchingFields(clazz, memberKeepRules, rule, clazz.type);
              break;
            }
            case KEEP_CLASSES_WITH_MEMBERS: {
              if (!allRulesSatisfied(memberKeepRules, clazz)) {
                break;
              }
              // fallthrough;
            }
            case KEEP: {
              markClass(clazz, rule);
              markClass(clazz, rule);
              markMatchingVisibleMethods(clazz, memberKeepRules, rule, null);
              markMatchingFields(clazz, memberKeepRules, rule, null);
              break;
            }
          }
        } else if (rule instanceof ProguardAssumeNoSideEffectRule) {
          markMatchingVisibleMethods(clazz, memberKeepRules, rule, null);
          markMatchingFields(clazz, memberKeepRules, rule, null);
        } else {
          assert rule instanceof ProguardAssumeValuesRule;
          markMatchingVisibleMethods(clazz, memberKeepRules, rule, null);
          markMatchingFields(clazz, memberKeepRules, rule, null);
        }
      }
    }
  }

  public RootSet run(ExecutorService executorService) throws ExecutionException {
    application.timing.begin("Build root set...");
    try {
      List<Future<?>> futures = new ArrayList<>();
      // Mark all the things explicitly listed in keep rules.
      if (rules != null) {
        for (ProguardConfigurationRule rule : rules) {
          DexType[] specifics = specificDexTypes(rule);
          if (specifics != null) {
            // This keep rule only lists specific type matches.
            // This means there is no need to iterate over all classes.
            for (DexType type : specifics) {
              DexClass clazz = application.definitionFor(type);
              // Ignore keep rule iff it does not reference a class in the app.
              if (clazz != null) {
                process(clazz, rule);
              }
            }
          } else {
            futures.add(executorService.submit(() -> {
              for (DexProgramClass clazz : application.classes()) {
                process(clazz, rule);
              }
              if (rule.applyToLibraryClasses()) {
                for (DexLibraryClass clazz : application.libraryClasses()) {
                  process(clazz, rule);
                }
              }
            }));
          }
        }
        ThreadUtils.awaitFutures(futures);
      }
    } finally {
      application.timing.end();
    }
    return new RootSet(noShrinking, noOptimization, noObfuscation, reasonAsked, keepPackageName,
        checkDiscarded, noSideEffects, assumedValues, dependentNoShrinking);
  }

  private void markMatchingVisibleMethods(DexClass clazz,
      Collection<ProguardMemberRule> memberKeepRules, ProguardConfigurationRule rule,
      DexType onlyIfClassKept) {
    Set<Wrapper<DexMethod>> methodsMarked = new HashSet<>();
    while (clazz != null) {
      markMethods(clazz.directMethods(), memberKeepRules, rule, methodsMarked, onlyIfClassKept);
      markMethods(clazz.virtualMethods(), memberKeepRules, rule, methodsMarked, onlyIfClassKept);
      clazz = application.definitionFor(clazz.superType);
    }
  }

  private void markMatchingFields(DexClass clazz,
      Collection<ProguardMemberRule> memberKeepRules, ProguardConfigurationRule rule,
      DexType onlyIfClassKept) {
    markFields(clazz.staticFields(), memberKeepRules, rule, onlyIfClassKept);
    markFields(clazz.instanceFields(), memberKeepRules, rule, onlyIfClassKept);
  }

  public static void writeSeeds(Iterable<DexItem> seeds, PrintStream out) {
    for (DexItem seed : seeds) {
      if (seed instanceof DexClass) {
        out.println(((DexClass) seed).type.toSourceString());
      } else if (seed instanceof DexEncodedField) {
        DexField field = ((DexEncodedField) seed).field;
        out.println(
            field.clazz.toSourceString() + ": " + field.type.toSourceString() + " " + field.name
                .toSourceString());
      } else if (seed instanceof DexEncodedMethod) {
        DexEncodedMethod encodedMethod = (DexEncodedMethod) seed;
        DexMethod method = encodedMethod.method;
        out.print(method.holder.toSourceString() + ": ");
        if (encodedMethod.accessFlags.isConstructor()) {
          if (encodedMethod.accessFlags.isStatic()) {
            out.print("<clinit>(");
          } else {
            String holderName = method.holder.toSourceString();
            String constrName = holderName.substring(holderName.lastIndexOf('.') + 1);
            out.print(constrName + "(");
          }
        } else {
          out.print(
              method.proto.returnType.toSourceString() + " " + method.name.toSourceString() + "(");
        }
        boolean first = true;
        for (DexType param : method.proto.parameters.values) {
          if (!first) {
            out.print(",");
          }
          first = false;
          out.print(param.toSourceString());
        }
        out.println(")");
      }
    }
    out.close();
  }

  private boolean allRulesSatisfied(Collection<ProguardMemberRule> memberKeepRules,
      DexClass clazz) {
    for (ProguardMemberRule rule : memberKeepRules) {
      if (!ruleSatisfied(rule, clazz)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given rule is satisfied bu this clazz, not taking superclasses into
   * account.
   */
  private boolean ruleSatisfied(ProguardMemberRule rule, DexClass clazz) {
    if (ruleSatisfiedByMethods(rule, clazz.directMethods()) ||
        ruleSatisfiedByMethods(rule, clazz.virtualMethods()) ||
        ruleSatisfiedByFields(rule, clazz.staticFields()) ||
        ruleSatisfiedByFields(rule, clazz.instanceFields())) {
      return true;
    }
    return false;
  }

  private boolean ruleSatisfiedByMethods(ProguardMemberRule rule, DexEncodedMethod[] methods) {
    if (rule.getRuleType().includesMethods()) {
      for (DexEncodedMethod method : methods) {
        if (rule.matches(method, this)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean ruleSatisfiedByFields(ProguardMemberRule rule, DexEncodedField[] fields) {
    if (rule.getRuleType().includesFields()) {
      for (DexEncodedField field : fields) {
        if (rule.matches(field, this)) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean containsAnnotation(ProguardTypeMatcher classAnnotation,
      DexAnnotationSet annotations) {
    if (classAnnotation == null) {
      return true;
    }
    if (annotations.isEmpty()) {
      return false;
    }
    for (DexAnnotation annotation : annotations.annotations) {
      if (classAnnotation.matches(annotation.annotation.type)) {
        return true;
      }
    }
    return false;
  }

  private final IdentityHashMap<DexString, String> stringCache = new IdentityHashMap<>();
  private final IdentityHashMap<DexType, String> typeCache = new IdentityHashMap<>();

  public String lookupString(DexString name) {
    return stringCache.computeIfAbsent(name, DexString::toString);
  }

  public String lookupType(DexType type) {
    return typeCache.computeIfAbsent(type, DexType::toSourceString);
  }

  private void markMethods(DexEncodedMethod[] methods, Collection<ProguardMemberRule> rules,
      ProguardConfigurationRule context, Set<Wrapper<DexMethod>> methodsMarked,
      DexType onlyIfClassKept) {
    for (DexEncodedMethod method : methods) {
      if (methodsMarked.contains(MethodSignatureEquivalence.get().wrap(method.method))) {
        continue;
      }
      for (ProguardMemberRule rule : rules) {
        if (rule.matches(method, this)) {
          if (Log.ENABLED) {
            Log.verbose(getClass(), "Marking method `%s` due to `%s { %s }`.", method, context,
                rule);
          }
          addItemToSets(method, context, rule, onlyIfClassKept);
        }
      }
    }
  }

  private void markFields(DexEncodedField[] fields, Collection<ProguardMemberRule> rules,
      ProguardConfigurationRule context, DexType onlyIfClassKept) {
    for (DexEncodedField field : fields) {
      for (ProguardMemberRule rule : rules) {
        if (rule.matches(field, this)) {
          if (Log.ENABLED) {
            Log.verbose(getClass(), "Marking field `%s` due to `%s { %s }`.", field, context,
                rule);
          }
          addItemToSets(field, context, rule, onlyIfClassKept);
        }
      }
    }
  }

  private void markClass(DexClass clazz, ProguardConfigurationRule rule) {
    if (Log.ENABLED) {
      Log.verbose(getClass(), "Marking class `%s` due to `%s`.", clazz.type, rule);
    }
    addItemToSets(clazz, rule, null, null);
  }

  private void includeDescriptor(DexItem item, DexType type, ProguardKeepRule context) {
    if (type.isArrayType()) {
      type = type.toBaseType(application.dexItemFactory);
    }
    if (type.isPrimitiveType()) {
      return;
    }
    DexClass definition = appInfo.definitionFor(type);
    if (definition == null || definition.isLibraryClass()) {
      return;
    }
    // Keep the type if the item is also kept.
    dependentNoShrinking.computeIfAbsent(item, x -> new IdentityHashMap<>())
        .put(definition, context);
    // Unconditionally add to no-obfuscation, as that is only checked for surviving items.
    noObfuscation.add(definition);
  }

  private void includeDescriptorClasses(DexItem item, ProguardKeepRule context) {
    if (item instanceof DexEncodedMethod) {
      DexMethod method = ((DexEncodedMethod) item).method;
      includeDescriptor(item, method.proto.returnType, context);
      for (DexType value : method.proto.parameters.values) {
        includeDescriptor(item, value, context);
      }
    } else if (item instanceof DexEncodedField) {
      DexField field = ((DexEncodedField) item).field;
      includeDescriptor(item, field.type, context);
    } else {
      assert item instanceof DexClass;
    }
  }

  private synchronized void addItemToSets(DexItem item, ProguardConfigurationRule context,
      ProguardMemberRule rule, DexType onlyIfClassKept) {
    if (context instanceof ProguardKeepRule) {
      ProguardKeepRule keepRule = (ProguardKeepRule) context;
      ProguardKeepRuleModifiers modifiers = keepRule.getModifiers();
      if (!modifiers.allowsShrinking) {
        if (onlyIfClassKept != null) {
          dependentNoShrinking.computeIfAbsent(onlyIfClassKept, x -> new IdentityHashMap<>())
              .put(item, keepRule);
        } else {
          noShrinking.put(item, keepRule);
        }
      }
      if (!modifiers.allowsOptimization) {
        noOptimization.add(item);
      }
      if (!modifiers.allowsObfuscation) {
        noObfuscation.add(item);
      }
      if (modifiers.whyAreYouKeeping) {
        assert onlyIfClassKept == null;
        reasonAsked.add(item);
      }
      if (modifiers.keepPackageNames) {
        assert onlyIfClassKept == null;
        keepPackageName.add(item);
      }
      if (modifiers.includeDescriptorClasses) {
        includeDescriptorClasses(item, keepRule);
      }
      if (modifiers.checkDiscarded) {
        checkDiscarded.add(item);
      }
    } else if (context instanceof ProguardAssumeNoSideEffectRule) {
      noSideEffects.put(item, rule);
    } else if (context instanceof ProguardAssumeValuesRule) {
      assumedValues.put(item, rule);
    }
  }

  public static class RootSet {

    public final Map<DexItem, ProguardKeepRule> noShrinking;
    public final Set<DexItem> noOptimization;
    public final Set<DexItem> noObfuscation;
    public final Set<DexItem> reasonAsked;
    public final Set<DexItem> keepPackageName;
    public final Set<DexItem> checkDiscarded;
    public final Map<DexItem, ProguardMemberRule> noSideEffects;
    public final Map<DexItem, ProguardMemberRule> assumedValues;
    private final Map<DexItem, Map<DexItem, ProguardKeepRule>> dependentNoShrinking;

    private boolean legalNoObfuscationItem(DexItem item) {
      if (!(item instanceof DexProgramClass
          || item instanceof DexLibraryClass
          || item instanceof DexEncodedMethod
          || item instanceof DexEncodedField)) {
      }
      assert item instanceof DexProgramClass
          || item instanceof DexLibraryClass
          || item instanceof DexEncodedMethod
          || item instanceof DexEncodedField;
      return true;
    }

    private boolean legalNoObfuscationItems(Set<DexItem> items) {
      items.forEach(this::legalNoObfuscationItem);
      return true;
    }

    private boolean legalDependentNoShrinkingItem(DexItem item) {
      if (!(item instanceof DexType
          || item instanceof DexEncodedMethod
          || item instanceof DexEncodedField)) {
      }
      assert item instanceof DexType
          || item instanceof DexEncodedMethod
          || item instanceof DexEncodedField;
      return true;
    }

    private boolean legalDependentNoShrinkingItems(
        Map<DexItem, Map<DexItem, ProguardKeepRule>> dependentNoShrinking) {
      dependentNoShrinking.keySet().forEach(this::legalDependentNoShrinkingItem);
      return true;
    }

    private RootSet(Map<DexItem, ProguardKeepRule> noShrinking,
        Set<DexItem> noOptimization, Set<DexItem> noObfuscation, Set<DexItem> reasonAsked,
        Set<DexItem> keepPackageName, Set<DexItem> checkDiscarded,
        Map<DexItem, ProguardMemberRule> noSideEffects,
        Map<DexItem, ProguardMemberRule> assumedValues,
        Map<DexItem, Map<DexItem, ProguardKeepRule>> dependentNoShrinking) {
      this.noShrinking = Collections.unmodifiableMap(noShrinking);
      this.noOptimization = Collections.unmodifiableSet(noOptimization);
      this.noObfuscation = Collections.unmodifiableSet(noObfuscation);
      this.reasonAsked = Collections.unmodifiableSet(reasonAsked);
      this.keepPackageName = Collections.unmodifiableSet(keepPackageName);
      this.checkDiscarded = Collections.unmodifiableSet(checkDiscarded);
      this.noSideEffects = Collections.unmodifiableMap(noSideEffects);
      this.assumedValues = Collections.unmodifiableMap(assumedValues);
      this.dependentNoShrinking = dependentNoShrinking;
      assert legalNoObfuscationItems(noObfuscation);
      assert legalDependentNoShrinkingItems(dependentNoShrinking);
    }

    Map<DexItem, ProguardKeepRule> getDependentItems(DexItem item) {
      assert item instanceof DexType
          || item instanceof DexEncodedMethod
          || item instanceof DexEncodedField;
      return Collections
          .unmodifiableMap(dependentNoShrinking.getOrDefault(item, Collections.emptyMap()));
    }
  }
}
