// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package smali;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskAction;

public class Smali extends DefaultTask {

  private FileTree source;
  private File destination;
  private File smaliScript;

  public FileTree getSource() {
    return source;
  }

  public void setSource(FileTree source) {
    this.source = source;
    getInputs().file(source);
  }

  public File getDestination() {
    return destination;
  }

  public void setDestination(File destination) {
    this.destination = destination;
    getOutputs().file(destination);
  }

  public File getSmaliScript() {
    return smaliScript;
  }

  public void setSmaliScript(File smaliScript) {
    this.smaliScript = smaliScript;
  }

  @TaskAction
  void exec() {
    try {
      List<String> fileNames = source.getFiles().stream().map(file -> file.toString())
          .collect(Collectors.toList());
      org.jf.smali.SmaliOptions options = new org.jf.smali.SmaliOptions();
      options.outputDexFile = destination.getCanonicalPath().toString();
      org.jf.smali.Smali.assemble(options, fileNames);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}