// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package jumbostring;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class JumboString {
  public static void main(String[] args) {
    // Make sure this string sorts after the field names and string values in the StringPoolX.java
    // files to ensure this is a jumbo string.
    System.out.println("zzzz - jumbo string");
  }

  // Code for generating the StringPoolX.java files.
  //
  // We only need to generate two files to get jumbo strings. Each file has 16k static final fields
  // with values, and both the field name and the value will be in the string pool.
  public static void generate() throws IOException {
    int stringsPerFile = (1 << 14);
    for (int fileNumber = 0; fileNumber < 2; fileNumber++) {
      Path path = FileSystems.getDefault().getPath("StringPool" + fileNumber + ".java");
      PrintStream out = new PrintStream(
          Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND));

      out.println(
          "// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file");
      out.println(
          "// for details. All rights reserved. Use of this source code is governed by a");
      out.println("// BSD-style license that can be found in the LICENSE file.");
      out.println("package jumbostring;");
      out.println();
      out.println("class StringPool" + fileNumber + " {");

      int offset = fileNumber * stringsPerFile;
      for (int i = offset; i < offset + stringsPerFile; i++) {
        out.println("  public static final String s" + i + " = \"" + i + "\";");
      }
      out.println("}");
      out.close();
    }
  }
}
