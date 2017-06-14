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

public class DexMerger extends DefaultTask {

  private FileTree source;
  private File destination;
  private File dexMergerExecutable;
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
    getOutputs().file(destination);
  }

  public File getDexMergerExecutable() {
    return dexMergerExecutable;
  }

  public void setDexMergerExecutable(File dexMergerExecutable) {
    this.dexMergerExecutable = dexMergerExecutable;
  }

  @TaskAction
  void exec() {
    getProject().exec(new Action<ExecSpec>() {
      @Override
      public void execute(ExecSpec execSpec) {
        try {
          if (dexMergerExecutable == null) {
            dexMergerExecutable = Utils.dexMergerExecutable();
          }
          execSpec.setExecutable(dexMergerExecutable);
          execSpec.args(destination.getCanonicalPath());
          execSpec.args(source.getFiles());
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    });
  }
}