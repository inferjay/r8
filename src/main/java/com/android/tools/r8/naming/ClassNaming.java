// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.naming.MemberNaming.Signature;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Stores name information for a class.
 * <p>
 * This includes how the class was renamed and information on the classes members.
 */
public class ClassNaming {

  public final String originalName;
  final String renamedName;

  /**
   * Mapping from the renamed signature to the naming information for a member.
   * <p>
   * A renamed signature is a signature where the member's name has been obfuscated but not the type
   * information.
   **/
  final Map<Signature, MemberNaming> members = new LinkedHashMap<>();

  ClassNaming(String renamedName, String originalName) {
    this.renamedName = renamedName;
    this.originalName = originalName;
  }

  void addMemberEntry(MemberNaming entry) {
    Signature renamedSignature = entry.renamedSignature;
    members.put(renamedSignature, entry);
  }

  public MemberNaming lookup(Signature renamedSignature) {
    return members.get(renamedSignature);
  }

  public MemberNaming lookupByOriginalSignature(Signature original) {
    for (MemberNaming naming : members.values()) {
      if (naming.signature.equals(original)) {
        return naming;
      }
    }
    return null;
  }

  public void forAllMemberNaming(Consumer<MemberNaming> consumer) {
    members.values().forEach(consumer);
  }

  void write(Writer writer, boolean collapseRanges) throws IOException {
    writer.append(originalName);
    writer.append(" -> ");
    writer.append(renamedName);
    writer.append(":\n");
    for (MemberNaming member : members.values()) {
      member.write(writer, collapseRanges, true);
    }
  }

  @Override
  public String toString() {
    try {
      StringWriter writer = new StringWriter();
      write(writer, false);
      return writer.toString();
    } catch (IOException e) {
      return e.toString();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClassNaming)) {
      return false;
    }

    ClassNaming that = (ClassNaming) o;

    return originalName.equals(that.originalName)
        && renamedName.equals(that.renamedName)
        && members.equals(that.members);

  }

  @Override
  public int hashCode() {
    int result = originalName.hashCode();
    result = 31 * result + renamedName.hashCode();
    result = 31 * result + members.hashCode();
    return result;
  }
}

