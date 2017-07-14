// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.IndexedDexItem;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.PackageDistribution;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

public class VirtualFile {

  // The fill strategy determine how to distribute classes into dex files.
  enum FillStrategy {
    // Only put classes matches by the main dex rules into the first dex file. Distribute remaining
    // classes in additional dex files filling each dex file as much as possible.
    MINIMAL_MAIN_DEX,
    // Distribute classes in as few dex files as possible filling each dex file as much as possible.
    FILL_MAX,
    // Distribute classes keeping some space for future growth. This is mainly useful together with
    // the package map distribution.
    LEAVE_SPACE_FOR_GROWTH,
    // TODO(sgjesse): Does "minimal main dex" combined with "leave space for growth" make sense?
  }

  private static final int MAX_ENTRIES = Constants.U16BIT_MAX + 1;
  /**
   * When distributing classes across files we aim to leave some space. The amount of space left is
   * driven by this constant.
   */
  private static final int MAX_PREFILL_ENTRIES = MAX_ENTRIES - 5000;

  private final int id;
  private final VirtualFileIndexedItemCollection indexedItems;
  private final IndexedItemTransaction transaction;

  private VirtualFile(int id, NamingLens namingLens) {
    this.id = id;
    this.indexedItems = new VirtualFileIndexedItemCollection(id);
    this.transaction = new IndexedItemTransaction(indexedItems, namingLens);
  }

  public int getId() {
    return id;
  }

  public Set<String> getClassDescriptors() {
    Set<String> classDescriptors = new HashSet<>();
    for (DexProgramClass clazz : indexedItems.classes) {
      boolean added = classDescriptors.add(clazz.type.descriptor.toString());
      assert added;
    }
    return classDescriptors;
  }

  public static String deriveCommonPrefixAndSanityCheck(List<String> fileNames) {
    Iterator<String> nameIterator = fileNames.iterator();
    String first = nameIterator.next();
    if (!first.toLowerCase().endsWith(FileUtils.DEX_EXTENSION)) {
      throw new RuntimeException("Illegal suffix for dex file: `" + first + "`.");
    }
    String prefix = first.substring(0, first.length() - FileUtils.DEX_EXTENSION.length());
    int index = 2;
    while (nameIterator.hasNext()) {
      String next = nameIterator.next();
      if (!next.toLowerCase().endsWith(FileUtils.DEX_EXTENSION)) {
        throw new RuntimeException("Illegal suffix for dex file: `" + first + "`.");
      }
      if (!next.startsWith(prefix)) {
        throw new RuntimeException("Input filenames lack common prefix.");
      }
      String numberPart = next.substring(prefix.length(), next.length() - FileUtils.DEX_EXTENSION.length());
      if (Integer.parseInt(numberPart) != index++) {
        throw new RuntimeException("DEX files are not numbered consecutively.");
      }
    }
    return prefix;
  }

  private static Map<DexProgramClass, String> computeOriginalNameMapping(
      Collection<DexProgramClass> classes,
      ClassNameMapper proguardMap) {
    Map<DexProgramClass, String> originalNames = new HashMap<>();
    classes.forEach((DexProgramClass c) ->
        originalNames.put(c,
            DescriptorUtils.descriptorToJavaType(c.type.toDescriptorString(), proguardMap)));
    return originalNames;
  }

  private static String extractPrefixToken(int prefixLength, String className, boolean addStar) {
    int index = 0;
    int lastIndex = 0;
    int segmentCount = 0;
    while (lastIndex != -1 && segmentCount++ < prefixLength) {
      index = lastIndex;
      lastIndex = className.indexOf('.', index + 1);
    }
    String prefix = className.substring(0, index);
    if (addStar && segmentCount >= prefixLength) {
      // Full match, add a * to also match sub-packages.
      prefix += ".*";
    }
    return prefix;
  }

  public ObjectToOffsetMapping computeMapping(DexApplication application) {
    assert transaction.isEmpty();
    return new ObjectToOffsetMapping(
        id,
        application,
        indexedItems.classes.toArray(new DexProgramClass[indexedItems.classes.size()]),
        indexedItems.protos.toArray(new DexProto[indexedItems.protos.size()]),
        indexedItems.types.toArray(new DexType[indexedItems.types.size()]),
        indexedItems.methods.toArray(new DexMethod[indexedItems.methods.size()]),
        indexedItems.fields.toArray(new DexField[indexedItems.fields.size()]),
        indexedItems.strings.toArray(new DexString[indexedItems.strings.size()]),
        indexedItems.callSites.toArray(new DexCallSite[indexedItems.callSites.size()]),
        indexedItems.methodHandles.toArray(new DexMethodHandle[indexedItems.methodHandles.size()]));
  }

  private void addClass(DexProgramClass clazz) {
    transaction.addClassAndDependencies(clazz);
  }

  private static boolean isFull(int numberOfMethods, int numberOfFields, int maximum) {
    return (numberOfMethods > maximum) || (numberOfFields > maximum);
  }

  private boolean isFull() {
    return isFull(transaction.getNumberOfMethods(), transaction.getNumberOfFields(), MAX_ENTRIES);
  }

  private boolean isFilledEnough(FillStrategy fillStrategy) {
    return isFull(
        transaction.getNumberOfMethods(),
        transaction.getNumberOfFields(),
        fillStrategy == FillStrategy.FILL_MAX ? MAX_ENTRIES : MAX_PREFILL_ENTRIES);
  }

  public void abortTransaction() {
    transaction.abort();
  }

  public void commitTransaction() {
    transaction.commit();
  }

  public boolean isEmpty() {
    return indexedItems.classes.isEmpty();
  }

  public List<DexProgramClass> classes() {
    return indexedItems.classes;
  }

  public abstract static class Distributor {
    protected final DexApplication application;
    protected final ApplicationWriter writer;
    protected final Map<Integer, VirtualFile> nameToFileMap = new HashMap<>();

    public Distributor(ApplicationWriter writer) {
      this.application = writer.application;
      this.writer = writer;
    }

    public abstract Map<Integer, VirtualFile> run() throws ExecutionException, IOException;
  }

  public static class FilePerClassDistributor extends Distributor {

    public FilePerClassDistributor(ApplicationWriter writer) {
      super(writer);
    }

    public Map<Integer, VirtualFile> run() throws ExecutionException, IOException {
      // Assign dedicated virtual files for all program classes.
      for (DexProgramClass clazz : application.classes()) {
        VirtualFile file = new VirtualFile(nameToFileMap.size(), writer.namingLens);
        nameToFileMap.put(nameToFileMap.size(), file);
        file.addClass(clazz);
        file.commitTransaction();
      }
      return nameToFileMap;
    }
  }

  public abstract static class DistributorBase extends Distributor {
    protected Set<DexProgramClass> classes;
    protected Map<DexProgramClass, String> originalNames;

    public DistributorBase(ApplicationWriter writer) {
      super(writer);

      classes = Sets.newHashSet(application.classes());
      originalNames = computeOriginalNameMapping(classes, application.getProguardMap());
    }

    protected void fillForMainDexList(Set<DexProgramClass> classes) {
      if (application.mainDexList != null) {
        VirtualFile mainDexFile = nameToFileMap.get(0);
        for (DexType type : application.mainDexList) {
          DexClass clazz = application.definitionFor(type);
          if (clazz != null && clazz.isProgramClass()) {
            DexProgramClass programClass = (DexProgramClass) clazz;
            mainDexFile.addClass(programClass);
            if (mainDexFile.isFull()) {
              throw new CompilationError("Cannot fit requested classes in main-dex file.");
            }
            classes.remove(programClass);
          } else {
            System.out.println(
                "WARNING: Application does not contain `"
                    + type.toSourceString()
                    + "` as referenced in main-dex-list.");
          }
          mainDexFile.commitTransaction();
        }
      }
    }

    TreeSet<DexProgramClass> sortClassesByPackage(Set<DexProgramClass> classes,
        Map<DexProgramClass, String> originalNames) {
      TreeSet<DexProgramClass> sortedClasses = new TreeSet<>(
          (DexProgramClass a, DexProgramClass b) -> {
            String originalA = originalNames.get(a);
            String originalB = originalNames.get(b);
            int indexA = originalA.lastIndexOf('.');
            int indexB = originalB.lastIndexOf('.');
            if (indexA == -1 && indexB == -1) {
              // Empty package, compare the class names.
              return originalA.compareTo(originalB);
            }
            if (indexA == -1) {
              // Empty package name comes first.
              return -1;
            }
            if (indexB == -1) {
              // Empty package name comes first.
              return 1;
            }
            String prefixA = originalA.substring(0, indexA);
            String prefixB = originalB.substring(0, indexB);
            int result = prefixA.compareTo(prefixB);
            if (result != 0) {
              return result;
            }
            return originalA.compareTo(originalB);
          });
      sortedClasses.addAll(classes);
      return sortedClasses;
    }
  }

  public static class FillFilesDistributor extends DistributorBase {
    private final FillStrategy fillStrategy;

    public FillFilesDistributor(ApplicationWriter writer, boolean minimalMainDex) {
      super(writer);
      this.fillStrategy = minimalMainDex ? FillStrategy.MINIMAL_MAIN_DEX : FillStrategy.FILL_MAX;
    }

    public Map<Integer, VirtualFile> run() throws ExecutionException, IOException {
      // Strategy for distributing classes for write out:
      // 1. Place the remaining files based on their packages in sorted order.

      // Start with 1 file. The package populator will add more if needed.
      nameToFileMap.put(0, new VirtualFile(0, writer.namingLens));

      // First fill required classes into the main dex file.
      fillForMainDexList(classes);

      // Sort the remaining classes based on the original names.
      // This with make classes from the same package be adjacent.
      classes = sortClassesByPackage(classes, originalNames);

      new PackageSplitPopulator(
          nameToFileMap, classes, originalNames, null, application.dexItemFactory,
          fillStrategy, writer.namingLens)
          .call();
      return nameToFileMap;
    }
  }

  public static class MonoDexDistributor extends DistributorBase {
    public MonoDexDistributor(ApplicationWriter writer) {
      super(writer);
    }

    @Override
    public Map<Integer, VirtualFile> run() throws ExecutionException, IOException {
      VirtualFile mainDexFile = new VirtualFile(0, writer.namingLens);
      nameToFileMap.put(0, mainDexFile);

      for (DexProgramClass programClass : classes) {
        mainDexFile.addClass(programClass);
        if (mainDexFile.isFull()) {
          throw new CompilationError("Cannot fit all classes in a single dex file.");
        }
      }
      mainDexFile.commitTransaction();
      return nameToFileMap;
    }
  }

  public static class PackageMapDistributor extends DistributorBase {
    private final PackageDistribution packageDistribution;
    private final ExecutorService executorService;

    public PackageMapDistributor(
        ApplicationWriter writer,
        PackageDistribution packageDistribution,
        ExecutorService executorService) {
      super(writer);
      this.packageDistribution = packageDistribution;
      this.executorService = executorService;
    }

    public Map<Integer, VirtualFile> run() throws ExecutionException, IOException {
      // Strategy for distributing classes for write out:
      // 1. Place all files in the package distribution file in the proposed files (if any).
      // 2. Place the remaining files based on their packages in sorted order.

      int maxReferencedIndex = packageDistribution.maxReferencedIndex();
      for (int index = 0; index <= maxReferencedIndex; index++) {
        VirtualFile file = new VirtualFile(index, writer.namingLens);
        nameToFileMap.put(index, file);
      }

      // First fill required classes into the main dex file.
      fillForMainDexList(classes);

      // Sort the remaining classes based on the original names.
      // This with make classes from the same package be adjacent.
      classes = sortClassesByPackage(classes, originalNames);

      Set<String> usedPrefixes = fillForDistribution(classes, originalNames);

      // TODO(zerny): Add the package map to AndroidApp and refactor its generation.
      Map<String, Integer> newAssignments;
      if (classes.isEmpty()) {
        newAssignments = Collections.emptyMap();
      } else {
        newAssignments =
            new PackageSplitPopulator(
                nameToFileMap, classes, originalNames, usedPrefixes, application.dexItemFactory,
                FillStrategy.LEAVE_SPACE_FOR_GROWTH, writer.namingLens)
                .call();
        if (!newAssignments.isEmpty() && nameToFileMap.size() > 1) {
          System.err.println(" * The used package map is missing entries. The following default "
              + "mappings have been used:");
          writeAssignments(newAssignments, new OutputStreamWriter(System.err));
          System.err.println(" * Consider updating the map.");
        }
      }

      Path newPackageMap = Paths.get("package.map");
      System.out.println(" - " + newPackageMap.toString());
      PackageDistribution.writePackageToFileMap(newPackageMap, newAssignments, packageDistribution);

      return nameToFileMap;
    }

    private Set<String> fillForDistribution(Set<DexProgramClass> classes,
        Map<DexProgramClass, String> originalNames) throws ExecutionException {
      Set<String> usedPrefixes = null;
      if (packageDistribution != null) {
        ArrayList<Future<List<DexProgramClass>>> futures = new ArrayList<>(nameToFileMap.size());
        usedPrefixes = packageDistribution.getFiles();
        for (VirtualFile file : nameToFileMap.values()) {
          PackageMapPopulator populator =
              new PackageMapPopulator(file, classes, packageDistribution, originalNames);
          futures.add(executorService.submit(populator));
        }
        ThreadUtils.awaitFutures(futures).forEach(classes::removeAll);
      }
      return usedPrefixes;
    }

    private void writeAssignments(Map<String, Integer> assignments, Writer output)
        throws IOException{
      for (Entry<String, Integer> entry : assignments.entrySet()) {
        output.write("    ");
        PackageDistribution.formatEntry(entry, output);
        output.write("\n");
      }
      output.flush();
    }
  }

  private static class VirtualFileIndexedItemCollection implements IndexedItemCollection {

    final int id;

    private final List<DexProgramClass> classes = new ArrayList<>();
    private final List<DexProto> protos = new ArrayList<>();
    private final List<DexType> types = new ArrayList<>();
    private final List<DexMethod> methods = new ArrayList<>();
    private final List<DexField> fields = new ArrayList<>();
    private final List<DexString> strings = new ArrayList<>();
    private final List<DexCallSite> callSites = new ArrayList<>();
    private final List<DexMethodHandle> methodHandles = new ArrayList<>();

    private final Set<DexClass> seenClasses = Sets.newIdentityHashSet();

    private VirtualFileIndexedItemCollection(int id) {
      this.id = id;
    }

    private <T extends IndexedDexItem> boolean addItem(T item, List<T> itemList) {
      assert item != null;
      if (item.assignToVirtualFile(id)) {
        itemList.add(item);
        return true;
      }
      return false;
    }

    @Override
    public boolean addClass(DexProgramClass clazz) {
      if (seenClasses.add(clazz)) {
        classes.add(clazz);
        return true;
      }
      return false;
    }

    @Override
    public boolean addField(DexField field) {
      return addItem(field, fields);
    }

    @Override
    public boolean addMethod(DexMethod method) {
      return addItem(method, methods);
    }

    @Override
    public boolean addString(DexString string) {
      return addItem(string, strings);
    }

    @Override
    public boolean addProto(DexProto proto) {
      return addItem(proto, protos);
    }

    @Override
    public boolean addCallSite(DexCallSite callSite) {
      return addItem(callSite, callSites);
    }

    @Override
    public boolean addMethodHandle(DexMethodHandle methodHandle) {
      return addItem(methodHandle, methodHandles);
    }

    @Override
    public boolean addType(DexType type) {
      return addItem(type, types);
    }

    public int getNumberOfMethods() {
      return methods.size();
    }

    public int getNumberOfFields() {
      return fields.size();
    }

    public int getNumberOfStrings() {
      return strings.size();
    }
  }

  private static class IndexedItemTransaction implements IndexedItemCollection {

    private final VirtualFileIndexedItemCollection base;
    private final NamingLens namingLens;

    private final Set<DexProgramClass> classes = new LinkedHashSet<>();
    private final Set<DexField> fields = new LinkedHashSet<>();
    private final Set<DexMethod> methods = new LinkedHashSet<>();
    private final Set<DexType> types = new LinkedHashSet<>();
    private final Set<DexProto> protos = new LinkedHashSet<>();
    private final Set<DexString> strings = new LinkedHashSet<>();
    private final Set<DexCallSite> callSites = new LinkedHashSet<>();
    private final Set<DexMethodHandle> methodHandles = new LinkedHashSet<>();

    private IndexedItemTransaction(VirtualFileIndexedItemCollection base,
        NamingLens namingLens) {
      this.base = base;
      this.namingLens = namingLens;
    }

    private <T extends IndexedDexItem> boolean maybeInsert(T item, Set<T> set) {
      if (item.hasVirtualFileData(base.id) || set.contains(item)) {
        return false;
      }
      set.add(item);
      return true;
    }

    void addClassAndDependencies(DexProgramClass clazz) {
      clazz.collectIndexedItems(this);
    }

    @Override
    public boolean addClass(DexProgramClass dexProgramClass) {
      if (base.seenClasses.contains(dexProgramClass) || classes.contains(dexProgramClass)) {
        return false;
      }
      classes.add(dexProgramClass);
      return true;
    }

    @Override
    public boolean addField(DexField field) {
      return maybeInsert(field, fields);
    }

    @Override
    public boolean addMethod(DexMethod method) {
      return maybeInsert(method, methods);
    }

    @Override
    public boolean addString(DexString string) {
      return maybeInsert(string, strings);
    }

    @Override
    public boolean addProto(DexProto proto) {
      return maybeInsert(proto, protos);
    }

    @Override
    public boolean addType(DexType type) {
      return maybeInsert(type, types);
    }

    @Override
    public boolean addCallSite(DexCallSite callSite) {
      return maybeInsert(callSite, callSites);
    }

    @Override
    public boolean addMethodHandle(DexMethodHandle methodHandle) {
      return maybeInsert(methodHandle, methodHandles);
    }

    @Override
    public DexString getRenamedDescriptor(DexType type) {
      return namingLens.lookupDescriptor(type);
    }

    @Override
    public DexString getRenamedName(DexMethod method) {
      assert namingLens.checkTargetCanBeTranslated(method);
      return namingLens.lookupName(method);
    }

    @Override
    public DexString getRenamedName(DexField field) {
      return namingLens.lookupName(field);
    }

    int getNumberOfMethods() {
      return methods.size() + base.getNumberOfMethods();
    }

    int getNumberOfFields() {
      return fields.size() + base.getNumberOfFields();
    }

    private <T extends DexItem> void commitItemsIn(Set<T> set, Function<T, Boolean> hook) {
      set.forEach((item) -> {
        boolean newlyAdded = hook.apply(item);
        assert newlyAdded;
      });
      set.clear();
    }

    void commit() {
      commitItemsIn(classes, base::addClass);
      commitItemsIn(fields, base::addField);
      commitItemsIn(methods, base::addMethod);
      commitItemsIn(protos, base::addProto);
      commitItemsIn(types, base::addType);
      commitItemsIn(strings, base::addString);
      commitItemsIn(callSites, base::addCallSite);
      commitItemsIn(methodHandles, base::addMethodHandle);
    }

    void abort() {
      classes.clear();
      fields.clear();
      methods.clear();
      protos.clear();
      types.clear();
      strings.clear();
    }

    public boolean isEmpty() {
      return classes.isEmpty() && fields.isEmpty() && methods.isEmpty() && protos.isEmpty()
          && types.isEmpty() && strings.isEmpty();
    }

    int getNumberOfStrings() {
      return strings.size() + base.getNumberOfStrings();
    }

    int getNumberOfClasses() {
      return classes.size() + base.classes.size();
    }
  }

  /**
   * Adds all classes from the given set that are covered by a corresponding package map
   * specification to the given file.
   */
  private static class PackageMapPopulator implements Callable<List<DexProgramClass>> {

    private final VirtualFile file;
    private final Collection<DexProgramClass> classes;
    private final PackageDistribution packageDistribution;
    private final Map<DexProgramClass, String> originalNames;

    PackageMapPopulator(
        VirtualFile file,
        Collection<DexProgramClass> classes,
        PackageDistribution packageDistribution,
        Map<DexProgramClass, String> originalNames) {
      this.file = file;
      this.classes = classes;
      this.packageDistribution = packageDistribution;
      this.originalNames = originalNames;
    }

    @Override
    public List<DexProgramClass> call() {
      String currentPrefix = null;
      int currentFileId = -1;
      List<DexProgramClass> inserted = new ArrayList<>();
      for (DexProgramClass clazz : classes) {
        String originalName = originalNames.get(clazz);
        assert originalName != null;
        if (!coveredByPrefix(originalName, currentPrefix)) {
          if (currentPrefix != null) {
            file.commitTransaction();
          }
          currentPrefix = lookupPrefixFor(originalName);
          if (currentPrefix == null) {
            currentFileId = -1;
          } else {
            currentFileId = packageDistribution.get(currentPrefix);
          }
        }
        if (currentFileId == file.id) {
          file.addClass(clazz);
          inserted.add(clazz);
        }
        if (file.isFull()) {
          throw new CompilationError(
              "Cannot fit package " + currentPrefix
                  + " in requested dex file, consider removing mapping.");
        }
      }
      file.commitTransaction();
      return inserted;
    }

    private String lookupPrefixFor(String originalName) {
      // First, check whether we have a match on the full package name.
      int lastIndexOfDot = originalName.lastIndexOf('.');
      if (lastIndexOfDot < 0) {
        return null;
      }
      String prefix = originalName.substring(0, lastIndexOfDot);
      if (packageDistribution.containsFile(prefix)) {
        return prefix;
      }
      // Second, look for .* qualified entries.
      int index;
      prefix = originalName;
      while ((index = prefix.lastIndexOf('.')) != -1) {
        prefix = prefix.substring(0, index);
        if (packageDistribution.containsFile(prefix + ".*")) {
          return prefix + ".*";
        }
      }
      return null;
    }

    static boolean coveredByPrefix(String originalName, String currentPrefix) {
      if (currentPrefix == null) {
        return false;
      }
      if (currentPrefix.endsWith(".*")) {
        return originalName.startsWith(currentPrefix.substring(0, currentPrefix.length() - 2));
      } else {
        return originalName.startsWith(currentPrefix)
            && originalName.lastIndexOf('.') == currentPrefix.length();
      }
    }
  }

  /**
   * Helper class to cycle through the set of virtual files.
   *
   * Iteration starts at the first file and iterates through all files.
   *
   * When {@link VirtualFileCycler#restart()} is called iteration of all files is restarted at the
   * current file.
   *
   * If the fill strategy indicate that the main dex file should be minimal, then the main dex file
   * will not be part of the iteration.
   */
  private static class VirtualFileCycler {
    private Map<Integer, VirtualFile> files;
    private final NamingLens namingLens;
    private final FillStrategy fillStrategy;

    private int nextFileId;
    private Iterator<VirtualFile> allFilesCyclic;
    private Iterator<VirtualFile> activeFiles;

    VirtualFileCycler(Map<Integer, VirtualFile> files, NamingLens namingLens,
        FillStrategy fillStrategy) {
      this.files = files;
      this.namingLens = namingLens;
      this.fillStrategy = fillStrategy;

      nextFileId = files.size();
      if (fillStrategy == FillStrategy.MINIMAL_MAIN_DEX) {
        // The main dex file is filtered out, so ensure at least one file for the remaining
        // classes
        files.put(nextFileId, new VirtualFile(nextFileId, namingLens));
        this.files = Maps.filterKeys(files, key -> key != 0);
        nextFileId++;
      }

      reset();
    }

    private void reset() {
      allFilesCyclic = Iterators.cycle(files.values());
      restart();
    }

    boolean hasNext() {
      return activeFiles.hasNext();
    }

    VirtualFile next() {
      VirtualFile next = activeFiles.next();
      assert fillStrategy != FillStrategy.MINIMAL_MAIN_DEX || next.getId() != 0;
      return next;
    }

    // Start a new iteration over all files, starting at the current one.
    void restart() {
      activeFiles = Iterators.limit(allFilesCyclic, files.size());
    }

    VirtualFile addFile() {
      VirtualFile newFile = new VirtualFile(nextFileId, namingLens);
      files.put(nextFileId, newFile);
      nextFileId++;

      reset();
      return newFile;
    }
  }

  /**
   * Distributes the given classes over the files in package order.
   *
   * <p>The populator avoids package splits. Big packages are split into subpackages if their size
   * exceeds 20% of the dex file. This populator also avoids filling files completely to cater for
   * future growth.
   *
   * <p>The populator cycles through the files until all classes have been successfully placed and
   * adds new files to the passed in map if it can't fit in the existing files.
   */
  private static class PackageSplitPopulator implements Callable<Map<String, Integer>> {

    /**
     * Android suggests com.company.product for package names, so the components will be at level 4
     */
    private static final int MINIMUM_PREFIX_LENGTH = 4;
    private static final int MAXIMUM_PREFIX_LENGTH = 7;
    /**
     * We allow 1/MIN_FILL_FACTOR of a file to remain empty when moving to the next file, i.e., a
     * rollback with less than 1/MAX_FILL_FACTOR of the total classes in a file will move to the
     * next file.
     */
    private static final int MIN_FILL_FACTOR = 5;

    private final List<DexProgramClass> classes;
    private final Map<DexProgramClass, String> originalNames;
    private final Set<String> previousPrefixes;
    private final DexItemFactory dexItemFactory;
    private final FillStrategy fillStrategy;
    private final VirtualFileCycler cycler;

    PackageSplitPopulator(
        Map<Integer, VirtualFile> files,
        Set<DexProgramClass> classes,
        Map<DexProgramClass, String> originalNames,
        Set<String> previousPrefixes,
        DexItemFactory dexItemFactory,
        FillStrategy fillStrategy,
        NamingLens namingLens) {
      this.classes = new ArrayList<>(classes);
      this.originalNames = originalNames;
      this.previousPrefixes = previousPrefixes;
      this.dexItemFactory = dexItemFactory;
      this.fillStrategy = fillStrategy;
      this.cycler = new VirtualFileCycler(files, namingLens, fillStrategy);
    }

    private String getOriginalName(DexProgramClass clazz) {
      return originalNames != null ? originalNames.get(clazz) : clazz.toString();
    }

    @Override
    public Map<String, Integer> call() throws IOException {
      int prefixLength = MINIMUM_PREFIX_LENGTH;
      int transactionStartIndex = 0;
      int fileStartIndex = 0;
      String currentPrefix = null;
      Map<String, Integer> newPackageAssignments = new LinkedHashMap<>();
      VirtualFile current = cycler.next();
      List<DexProgramClass> nonPackageClasses = new ArrayList<>();
      for (int classIndex = 0; classIndex < classes.size(); classIndex++) {
        DexProgramClass clazz = classes.get(classIndex);
        String originalName = getOriginalName(clazz);
        if (!PackageMapPopulator.coveredByPrefix(originalName, currentPrefix)) {
          if (currentPrefix != null) {
            current.commitTransaction();
            // Reset the cycler to again iterate over all files, starting with the current one.
            cycler.restart();
            assert !newPackageAssignments.containsKey(currentPrefix);
            newPackageAssignments.put(currentPrefix, current.id);
            // Try to reduce the prefix length if possible. Only do this on a successful commit.
            prefixLength = MINIMUM_PREFIX_LENGTH - 1;
          }
          String newPrefix;
          // Also, we need to avoid new prefixes that are a prefix of previously used prefixes, as
          // otherwise we might generate an overlap that will trigger problems when reusing the
          // package mapping generated here. For example, if an existing map contained
          //   com.android.foo.*
          // but we now try to place some new subpackage
          //   com.android.bar.*,
          // we locally could use
          //   com.android.*.
          // However, when writing out the final package map, we get overlapping patterns
          // com.android.* and com.android.foo.*.
          do {
            newPrefix = extractPrefixToken(++prefixLength, originalName, false);
          } while (currentPrefix != null &&
              (currentPrefix.startsWith(newPrefix)
              || conflictsWithPreviousPrefix(newPrefix, originalName)));
          // Don't set the current prefix if we did not extract one.
          if (!newPrefix.equals("")) {
            currentPrefix = extractPrefixToken(prefixLength, originalName, true);
          }
          transactionStartIndex = classIndex;
        }
        if (currentPrefix != null) {
          assert clazz.superType != null || clazz.type == dexItemFactory.objectType;
          current.addClass(clazz);
        } else {
          assert clazz.superType != null;
          // We don't have a package, add this to a list of classes that we will add last.
          assert current.transaction.isEmpty();
          nonPackageClasses.add(clazz);
          continue;
        }
        if (current.isFilledEnough(fillStrategy) || current.isFull()) {
          current.abortTransaction();
          // We allow for a final rollback that has at most 20% of classes in it.
          // This is a somewhat random number that was empirically chosen.
          if (classIndex - transactionStartIndex > (classIndex - fileStartIndex) / MIN_FILL_FACTOR
              && prefixLength < MAXIMUM_PREFIX_LENGTH) {
            prefixLength++;
          } else {
            // Reset the state to after the last commit and cycle through files.
            // The idea is that we do not increase the number of files, so it has to fit
            // somewhere.
            fileStartIndex = transactionStartIndex;
            if (!cycler.hasNext()) {
              // Special case where we simply will never be able to fit the current package into
              // one dex file. This is currently the case for Strings in jumbo tests, see:
              // b/33227518
              if (current.transaction.getNumberOfClasses() == 0) {
                for (int j = transactionStartIndex; j <= classIndex; j++) {
                  nonPackageClasses.add(classes.get(j));
                }
                transactionStartIndex = classIndex + 1;
              }
              // All files are filled up to the 20% mark.
              cycler.addFile();
            }
            current = cycler.next();
          }
          currentPrefix = null;
          // Go back to previous start index.
          classIndex = transactionStartIndex - 1;
          assert current != null;
        }
      }
      current.commitTransaction();
      assert !newPackageAssignments.containsKey(currentPrefix);
      if (currentPrefix != null) {
        newPackageAssignments.put(currentPrefix, current.id);
      }
      if (nonPackageClasses.size() > 0) {
        addNonPackageClasses(cycler, nonPackageClasses);
      }
      return newPackageAssignments;
    }

    private void addNonPackageClasses(
        VirtualFileCycler cycler, List<DexProgramClass> nonPackageClasses) {
      cycler.restart();
      VirtualFile current;
      current = cycler.next();
      for (DexProgramClass clazz : nonPackageClasses) {
        if (current.isFilledEnough(fillStrategy)) {
          current = getVirtualFile(cycler);
        }
        current.addClass(clazz);
        while (current.isFull()) {
          // This only happens if we have a huge class, that takes up more than 20% of a dex file.
          current.abortTransaction();
          current = getVirtualFile(cycler);
          boolean wasEmpty = current.isEmpty();
          current.addClass(clazz);
          if (wasEmpty && current.isFull()) {
            throw new InternalCompilerError(
                "Class " + clazz.toString() + " does not fit into a single dex file.");
          }
        }
        current.commitTransaction();
      }
    }

    private VirtualFile getVirtualFile(VirtualFileCycler cycler) {
      VirtualFile current = null;
      while (cycler.hasNext()
          && (current = cycler.next()).isFilledEnough(fillStrategy)) {}
      if (current == null || current.isFilledEnough(fillStrategy)) {
        current = cycler.addFile();
      }
      return current;
    }

    private boolean conflictsWithPreviousPrefix(String newPrefix, String originalName) {
      if (previousPrefixes == null) {
        return false;
      }
      for (String previous : previousPrefixes) {
        // Check whether a previous prefix starts with this new prefix and, if so,
        // whether the new prefix already is maximal. So for example a new prefix of
        //   foo.bar
        // would match
        //   foo.bar.goo.*
        // However, if the original class is
        //   foo.bar.X
        // then this prefix is the best we can do, and will not turn into a .* prefix and
        // thus does not conflict.
        if (previous.startsWith(newPrefix)
            && (originalName.lastIndexOf('.') > newPrefix.length())) {
          return true;
        }
      }

      return false;
    }
  }
}
