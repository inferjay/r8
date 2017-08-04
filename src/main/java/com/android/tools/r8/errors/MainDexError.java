// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.errors;

/**
 * Exception regarding main-dex list and main dex rules.
 *
 * Depending on tool kind, this exception should be massaged, e.g., adding appropriate suggestions,
 * and re-thrown as {@link CompilationError}, which will be in turn informed to the user as an
 * expected compilation error.
 */
public class MainDexError extends RuntimeException {

  private final boolean hasMainDexList;
  private final long numOfMethods;
  private final long numOfFields;
  private final long maxNumOfEntries;

  public MainDexError(
      boolean hasMainDexList, long numOfMethods, long numOfFields, long maxNumOfEntries) {
    this.hasMainDexList = hasMainDexList;
    this.numOfMethods = numOfMethods;
    this.numOfFields = numOfFields;
    this.maxNumOfEntries = maxNumOfEntries;
  }

  private String getGeneralMessage() {
    StringBuilder messageBuilder = new StringBuilder();
    // General message: Cannot fit.
    messageBuilder.append("Cannot fit requested classes in ");
    messageBuilder.append(hasMainDexList ? "the main-" : "a single ");
    messageBuilder.append("dex file.\n");

    return messageBuilder.toString();
  }

  private String getNumberRelatedMessage() {
    StringBuilder messageBuilder = new StringBuilder();
    // Show the numbers of methods and/or fields that exceed the limit.
    if (numOfMethods > maxNumOfEntries) {
      messageBuilder.append("# methods: ");
      messageBuilder.append(numOfMethods);
      messageBuilder.append(" > ").append(maxNumOfEntries).append('\n');
    }
    if (numOfFields > maxNumOfEntries) {
      messageBuilder.append("# fields: ");
      messageBuilder.append(numOfFields);
      messageBuilder.append(" > ").append(maxNumOfEntries).append('\n');
    }

    return messageBuilder.toString();
  }

  @Override
  public String getMessage() {
    // Placeholder to generate a general error message for other (minor) utilities:
    //   Bisect, disassembler, dexsegments.
    // Implement tool-specific error message generator, like D8 and R8 below, if necessary.
    return getGeneralMessage() + getNumberRelatedMessage();
  }

  public String getMessageForD8() {
    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append(getGeneralMessage());
    if (hasMainDexList) {
      messageBuilder.append("Classes required by the main-dex list ");
      messageBuilder.append("do not fit in one dex.\n");
    } else {
      messageBuilder.append("Try supplying a main-dex list.\n");
    }
    messageBuilder.append(getNumberRelatedMessage());
    return messageBuilder.toString();
  }

  public String getMessageForR8() {
    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append(getGeneralMessage());
    if (hasMainDexList) {
      messageBuilder.append("Classes required by main dex rules and the main-dex list ");
      messageBuilder.append("do not fit in one dex.\n");
    } else {
      messageBuilder.append("Try supplying a main-dex list or main dex rules.\n");
    }
    messageBuilder.append(getNumberRelatedMessage());
    return messageBuilder.toString();
  }

}
