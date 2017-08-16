// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import java.util.Arrays;

/**
 * Subset of dex items that are referenced by some table index.
 */
public abstract class IndexedDexItem extends CachedHashValueDexItem implements Presorted {

  private static final int SORTED_INDEX_UNKNOWN = -1;
  private int sortedIndex = SORTED_INDEX_UNKNOWN; // assigned globally after reading.
  /**
   * Contains the indexes assigned to this item for the various virtual output files.
   *
   * <p>One DexItem might be assigned to multiple virtual files.
   *
   * <p>For a certain virtual file this DexItem has the value:
   * <ul>
   * <li>{@link #UNASSOCIATED_VALUE}, when not associated with the virtual file.
   * <li>{@link #ASSOCIATED_VALUE}, when associated with the virtual file but no index allocated.
   * <li>A zero or greater value when this item has been associated by the virtual file
   * and the value denotes the assigned index.
   * </ul>
   * <p> Note that, in case of multiple files, for a specific IndexedDexItem, we may not have
   * as many entries in the index as there are files (we only expand when we need to). If we lookup
   * the value of an entry that is out of bounds it is equivalent to {@link #UNASSOCIATED_VALUE}
   *
   * <p>This field is initialized on first write in {@link #updateVirtualFileData(int)}}. It
   * is assumed that multiple files are processed concurrently and thus the allocation of the
   * array is synchronized. However, for any a given file id, sequential access is assumed.
   */
  private int[] virtualFileIndexes;

  public abstract void collectIndexedItems(IndexedItemCollection indexedItems);

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    // Should never be visited.
    assert false;
  }

  public abstract int getOffset(ObjectToOffsetMapping mapping);

  /**
   * Constants used inside virtualFileIndexes.
   */
  public static final int UNASSOCIATED_VALUE = -2;
  public static final int ASSOCIATED_VALUE = -1;
  public static final int MIN_VALID_VALUE = 0;

  /**
   * Returns whether this item is assigned to the given file id.
   */
  public boolean hasVirtualFileData(int virtualFileId) {
    return getVirtualFileIndex(virtualFileId) != UNASSOCIATED_VALUE;
  }

  /**
   * Assigns this item to the given file id if it has not been assigned previously.
   *
   * <p>This method returns 'true' if the item was newly assigned, i.e., it was not previously
   * assigned to the file id.
   */
  public boolean assignToVirtualFile(int virtualFileId) {
    // Fast lock-free check whether already assigned.
    if (hasVirtualFileData(virtualFileId)) {
      return false;
    }
    return updateVirtualFileData(virtualFileId);
  }

  /**
   * Assigns this item to the given file id.
   *
   * <p>As a side effect, the {@link #virtualFileIndexes} field might be initialized or expanded.
   * Hence this method is synchronized. Note that the field is queried without synchronization.
   * Therefor it has to remain in a valid state at all times and must transition atomically from
   * null to an initialized allocated value.
   */
  private synchronized boolean updateVirtualFileData(int virtualFileId) {
    if (virtualFileIndexes == null) {
      int[] fileIndices = new int[virtualFileId + 1];
      Arrays.fill(fileIndices, UNASSOCIATED_VALUE);
      // This has to be an atomic transition from null to an initialized array.
      virtualFileIndexes = fileIndices;
    }
    // We increased the number of files, increase the index size.
    if (virtualFileId >= virtualFileIndexes.length) {
      int oldLength = virtualFileIndexes.length;
      int[] fileIndices = Arrays.copyOf(virtualFileIndexes, virtualFileId + 1);
      Arrays.fill(fileIndices, oldLength, virtualFileId + 1, UNASSOCIATED_VALUE);
      virtualFileIndexes = fileIndices;
    }
    assert virtualFileId < virtualFileIndexes.length;
    boolean wasAdded = virtualFileIndexes[virtualFileId] == UNASSOCIATED_VALUE;
    virtualFileIndexes[virtualFileId] = ASSOCIATED_VALUE;
    return wasAdded;
  }

  /**
   * Assigns an actual index for this item in the given file.
   *
   * <p>May only be used after this item has been assigned to the file using {@link
   * #assignToVirtualFile(int)}.
   */
  public void assignVirtualFileIndex(int virtualFileId, int index) {
    assert virtualFileIndexes != null;
    assert virtualFileIndexes[virtualFileId] < MIN_VALID_VALUE;
    virtualFileIndexes[virtualFileId] = index;
  }

  /**
   * Returns the index associated with this item for the given file id or {@link
   * #UNASSOCIATED_VALUE} if the item is not associated to the given file id.
   */
  public int getVirtualFileIndex(int virtualFileId) {
    if (virtualFileIndexes == null) {
      return UNASSOCIATED_VALUE;
    }
    // If more files were added, but this entry not associated with it, we would not have extended
    // the size of the array. So if the {@link virtualFileId} is out of bounds, it means
    // {@link #UNASSOCIATED_VALUE}
    return virtualFileIndexes.length > virtualFileId
        ? virtualFileIndexes[virtualFileId]
        : UNASSOCIATED_VALUE;
  }

  // Partial implementation of PresortedComparable.

  final public void setSortedIndex(int sortedIndex) {
    assert sortedIndex > SORTED_INDEX_UNKNOWN;
    assert this.sortedIndex == SORTED_INDEX_UNKNOWN;
    this.sortedIndex = sortedIndex;
  }

  final public int getSortedIndex() {
    return sortedIndex;
  }

  final public int sortedCompareTo(int other) {
    assert sortedIndex > SORTED_INDEX_UNKNOWN;
    assert other > SORTED_INDEX_UNKNOWN;
    return Integer.compare(sortedIndex, other);
  }

  public void flushCachedValues() {
    super.flushCachedValues();
    resetSortedIndex();
  }

  public void resetSortedIndex() {
    sortedIndex = SORTED_INDEX_UNKNOWN;
  }
}
