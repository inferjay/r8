package com.android.tools.r8.classmerging;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.DexInspector;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ClassMergingTest {

  private static final Path EXAMPLE_JAR = Paths.get(ToolHelper.EXAMPLES_BUILD_DIR)
      .resolve("classmerging.jar");
  private static final Path EXAMPLE_KEEP = Paths.get(ToolHelper.EXAMPLES_DIR)
      .resolve("classmerging").resolve("keep-rules.txt");

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Before
  public void runR8()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    // Disable access modification, as it otherwise is difficult to test visibility bridge methods.
    ToolHelper.runR8(
        R8Command.builder()
            .setOutputPath(Paths.get(temp.getRoot().getCanonicalPath()))
            .addProgramFiles(EXAMPLE_JAR)
            .addProguardConfigurationFiles(EXAMPLE_KEEP)
            .setMinification(false)
            .build(), o -> {
          o.allowAccessModification = false;
          o.skipClassMerging = false;
        });
    inspector = new DexInspector(
        Paths.get(temp.getRoot().getCanonicalPath()).resolve("classes.dex"));
  }

  private DexInspector inspector;

  @Test
  public void testClassesHaveBeenMerged() throws IOException, ExecutionException {
    // GenericInterface should be merged into GenericInterfaceImpl.
    Assert.assertFalse(inspector.clazz("classmerging.GenericInterface").isPresent());
    Assert.assertTrue(inspector.clazz("classmerging.GenericInterfaceImpl").isPresent());
    Assert.assertFalse(inspector.clazz("classmerging.GenericAbstractClass").isPresent());
    Assert.assertTrue(inspector.clazz("classmerging.GenericInterfaceImpl").isPresent());
    Assert.assertFalse(inspector.clazz("classmerging.Outer$SuperClass").isPresent());
    Assert.assertTrue(inspector.clazz("classmerging.Outer$SubClass").isPresent());
    Assert.assertFalse(inspector.clazz("classmerging.SuperClass").isPresent());
    Assert.assertTrue(inspector.clazz("classmerging.SubClass").isPresent());
  }


  @Test
  public void testConflictWasDetected() throws IOException, ExecutionException {
    Assert.assertTrue(inspector.clazz("classmerging.ConflictingInterface").isPresent());
    Assert.assertTrue(inspector.clazz("classmerging.ConflictingInterfaceImpl").isPresent());
  }

  @Test
  public void testSuperCallWasDetected() throws IOException, ExecutionException {
    Assert.assertTrue(inspector.clazz("classmerging.SuperClassWithReferencedMethod").isPresent());
    Assert
        .assertTrue(inspector.clazz("classmerging.SubClassThatReferencesSuperMethod").isPresent());
  }

}
