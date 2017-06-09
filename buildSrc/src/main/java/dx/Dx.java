// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package dx;

import java.io.File;
import java.io.IOException;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;
import utils.Utils;

public class Dx extends DefaultTask {

  private FileTree source;
  private File destination;
  private File dxExecutable;
  private boolean debug;

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
    File classesFile = destination.toPath().resolve("classes.dex").toFile();
    // The output from running DX is classes.dex in the destination directory.
    // TODO(sgjesse): Handle multidex?
    getOutputs().file(classesFile);
  }

  public File getDxExecutable() {
    return dxExecutable;
  }

  public void setDxExecutable(File dxExecutable) {
    this.dxExecutable = dxExecutable;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  @TaskAction
  void exec() {
    getProject().exec(new Action<ExecSpec>() {
      @Override
      public void execute(ExecSpec execSpec) {
        try {
          if (dxExecutable == null) {
            String dxExecutableName = Utils.toolsDir().equals("windows") ? "dx.bat" : "dx";
            dxExecutable = new File("tools/" + Utils.toolsDir() + "/dx/bin/" + dxExecutableName);
          }
          execSpec.setExecutable(dxExecutable);
          execSpec.args("--dex");
          execSpec.args("--output");
          execSpec.args(destination.getCanonicalPath());
          if (isDebug()) {
            execSpec.args("--debug");
          }
          execSpec.args(source.getFiles());
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    });
  }
}