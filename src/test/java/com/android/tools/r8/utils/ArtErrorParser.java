// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import java.util.ArrayList;
import java.util.List;

public class ArtErrorParser {
  private static final String VERIFICATION_ERROR_HEADER = "Verification error in ";
  private static final String METHOD_EXCEEDS_INSTRUCITON_LIMIT =
      "Method exceeds compiler instruction limit: ";

  public static class ArtErrorParserException extends Exception {
    public ArtErrorParserException(String message) {
      super(message);
    }
  }

  private static class ParseInput {
    public final String input;
    public int pos = 0;

    public ParseInput(String input) {
      this.input = input;
    }

    public boolean hasNext() {
      return pos < input.length();
    }

    public char next() {
      assert hasNext();
      return input.charAt(pos++);
    }

    public char peek() {
      return input.charAt(pos);
    }

    public void whitespace() {
      while (peek() == ' ') {
        next();
      }
    }

    public String until(int end) {
      String result = input.substring(pos, end);
      pos = end;
      return result;
    }

    public String until(char match) throws ArtErrorParserException {
      int end = input.indexOf(match, pos);
      if (end < 0) {
        throw new ArtErrorParserException("Expected to find " + match + " at " + pos);
      }
      return until(end);
    }

    public void check(char expected) throws ArtErrorParserException {
      if (peek() != expected) {
        throw new ArtErrorParserException("Expected " + expected + " found " + pos);
      }
    }
  }

  public static abstract class ArtErrorInfo {
    public abstract int consumedLines();
    public abstract String getMessage();
    public abstract String dump(DexInspector inspector, boolean markLocation);
  }

  private static class ArtMethodError extends ArtErrorInfo {
    public final String className;
    public final String methodName;
    public final String methodReturn;
    public final List<String> methodFormals;
    public final String methodSignature;
    public final String errorMessage;
    public final List<Long> locations;
    private final int consumedLines;

    public ArtMethodError(String className,
        String methodName, String methodReturn, List<String> methodFormals,
        String methodSignature, String errorMessage,
        List<Long> locations,
        int consumedLines) {
      this.className = className;
      this.methodName = methodName;
      this.methodReturn = methodReturn;
      this.methodFormals = methodFormals;
      this.methodSignature = methodSignature;
      this.errorMessage = errorMessage;
      this.locations = locations;
      this.consumedLines = consumedLines;
    }

    @Override
    public int consumedLines() {
      return consumedLines;
    }

    @Override
    public String getMessage() {
      StringBuilder builder = new StringBuilder();
      builder.append("\n")
          .append(VERIFICATION_ERROR_HEADER)
          .append(methodSignature)
          .append(":\n")
          .append(errorMessage);
      return builder.toString();
    }

    @Override
    public String dump(DexInspector inspector, boolean markLocation) {
      ClassSubject clazz = inspector.clazz(className);
      MethodSubject method = clazz.method(methodReturn, methodName, methodFormals);
      DexEncodedMethod dex = method.getMethod();
      if (dex == null) {
        StringBuilder builder = new StringBuilder("Failed to lookup method: ");
        builder.append(className).append(".").append(methodName);
        StringUtils.append(builder, methodFormals);
        return builder.toString();
      }

      String code = method.getMethod().codeToString();
      if (markLocation && !locations.isEmpty()) {
        for (Long location : locations) {
          String locationString = "" + location + ":";
          code = code.replaceFirst(":(\\s*) " + locationString, ":$1*" + locationString);
        }
      }
      return code;
    }
  }

  public static List<ArtErrorInfo> parse(String message) throws ArtErrorParserException {
    List<ArtErrorInfo> errors = new ArrayList<>();
    String[] lines = message.split("\n");
    for (int i = 0; i < lines.length; i++) {
      ArtErrorInfo error = parse(lines, i);
      if (error != null) {
        errors.add(error);
        i += error.consumedLines();
      }
    }
    return errors;
  }

  private static ArtErrorInfo parse(String[] lines, final int line) throws ArtErrorParserException {
    String methodSig = null;
    StringBuilder errorMessageContent = new StringBuilder();
    int currentLine = line;
    {
      int index = lines[currentLine].indexOf(VERIFICATION_ERROR_HEADER);
      if (index >= 0) {
        // Read/skip the header line.
        String lineContent = lines[currentLine++].substring(index);
        // Append the content of each subsequent line that can be parsed as an "error message".
        for (; currentLine < lines.length; ++currentLine) {
          lineContent = lines[currentLine].substring(index);
          String[] parts = lineContent.split(":");
          if (parts.length == 2) {
            if (methodSig == null) {
              methodSig = parts[0];
              errorMessageContent.append(parts[1]);
            } else if (methodSig.equals(parts[0])) {
              errorMessageContent.append(parts[1]);
            } else {
              break;
            }
          } else if (parts.length >= 3) {
            if (methodSig == null) {
              methodSig = parts[1];
              for (int i = 2; i < parts.length; ++i) {
                errorMessageContent.append(parts[i]);
              }
            } else if (methodSig.equals(parts[1])) {
              for (int i = 2; i < parts.length; ++i) {
                errorMessageContent.append(parts[i]);
              }
            } else {
              break;
            }
          } else {
            break;
          }
        }
        if (methodSig == null) {
          throw new ArtErrorParserException("Unexpected art error message: " + lineContent);
        }
      }
    }
    if (methodSig == null) {
      int index = lines[currentLine].indexOf(METHOD_EXCEEDS_INSTRUCITON_LIMIT);
      if (index >= 0) {
        String lineContent = lines[currentLine++].substring(index);
        String[] parts = lineContent.split(":");
        if (parts.length == 2) {
          errorMessageContent.append(lineContent);
          methodSig = parts[1].substring(parts[1].indexOf(" in ") + 4);
        } else {
          throw new ArtErrorParserException("Unexpected art error message parts: " + parts);
        }
      }
    }

    // Return if we failed to identify an error description.
    if (methodSig == null) {
      return null;
    }

    String errorMessage = errorMessageContent.toString();
    ParseInput input = new ParseInput(methodSig);
    String methodReturn = parseType(input);
    String[] qualifiedName = parseQualifiedName(input);
    List<String> methodFormals = parseTypeList(input);
    List<Long> locations = parseLocations(errorMessage);
    return new ArtMethodError(getClassName(qualifiedName), getMethodName(qualifiedName),
        methodReturn, methodFormals, methodSig, errorMessage, locations, currentLine - line);
  }

  private static String getClassName(String[] parts) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < parts.length - 1; i++) {
      if (i != 0) {
        builder.append('.');
      }
      builder.append(parts[i]);
    }
    return builder.toString();
  }

  private static String getMethodName(String[] parts) {
    return parts[parts.length - 1];
  }

  private static String parseType(ParseInput input) throws ArtErrorParserException {
    input.whitespace();
    String type = input.until(' ');
    assert !type.contains("<");
    input.whitespace();
    return type;
  }

  private static String[] parseQualifiedName(ParseInput input) throws ArtErrorParserException {
    input.whitespace();
    return input.until('(').split("\\.");
  }

  private static List<String> parseTypeList(ParseInput input) throws ArtErrorParserException {
    input.check('(');
    input.next();
    String content = input.until(')').trim();
    if (content.isEmpty()) {
      return new ArrayList<>();
    }
    String[] rawList = content.split(",");
    List<String> types = new ArrayList<>();
    for (String type : rawList) {
      types.add(type.trim());
    }
    input.check(')');
    input.next();
    return types;
  }

  private static boolean isHexChar(char c) {
    return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
  }

  private static List<Long> parseLocations(String input) {
    List<Long> locations = new ArrayList<>();
    int length = input.length();
    int start = 0;
    while (start < length && (start = input.indexOf("0x", start)) >= 0) {
      int end = start + 2;
      while (end < length && isHexChar(input.charAt(end))) {
        ++end;
      }
      long l = Long.parseLong(input.substring(start + 2, end), 16);
      if (l >= 0) {
        locations.add(l);
      }
      start = end;
    }
    return locations;
  }
}
