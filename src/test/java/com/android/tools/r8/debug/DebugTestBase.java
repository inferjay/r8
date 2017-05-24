// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ArtCommandBuilder;
import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.utils.OffOrAuto;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.harmony.jpda.tests.framework.jdwp.CommandPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.Event;
import org.apache.harmony.jpda.tests.framework.jdwp.EventBuilder;
import org.apache.harmony.jpda.tests.framework.jdwp.EventPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.Frame.Variable;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPCommands;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPCommands.StackFrameCommandSet;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants.EventKind;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants.StepDepth;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants.StepSize;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants.SuspendPolicy;
import org.apache.harmony.jpda.tests.framework.jdwp.Location;
import org.apache.harmony.jpda.tests.framework.jdwp.ParsedEvent;
import org.apache.harmony.jpda.tests.framework.jdwp.ParsedEvent.EventThread;
import org.apache.harmony.jpda.tests.framework.jdwp.ReplyPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.Value;
import org.apache.harmony.jpda.tests.framework.jdwp.VmMirror;
import org.apache.harmony.jpda.tests.jdwp.share.JDWPTestCase;
import org.apache.harmony.jpda.tests.share.JPDATestOptions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

/**
 *
 * Base class for debugging tests
 */
public abstract class DebugTestBase {

  public static final StepFilter NO_FILTER = new StepFilter.NoStepFilter();
  public static final StepFilter INTELLIJ_FILTER = new StepFilter.IntelliJStepFilter();
  private static final StepFilter DEFAULT_FILTER = NO_FILTER;

  enum RuntimeKind {
    JAVA,
    ART
  }

  // Set to true to run tests with java
  private static final RuntimeKind RUNTIME_KIND = RuntimeKind.ART;

  // Set to true to enable verbose logs
  private static final boolean DEBUG_TESTS = false;

  private static final List<DexVm> UNSUPPORTED_ART_VERSIONS = ImmutableList.of(
      // Dalvik does not support command ReferenceType.Methods which is used to set breakpoint.
      // TODO(shertz) use command ReferenceType.MethodsWithGeneric instead
      DexVm.ART_4_4_4,
      // Older runtimes fail on buildbot
      // TODO(shertz) re-enable once issue is solved
      DexVm.ART_5_1_1,
      DexVm.ART_6_0_1);

  private static final Path JDWP_JAR = ToolHelper
      .getJdwpTestsJarPath(ToolHelper.getMinApiLevelForDexVm(ToolHelper.getDexVm()));
  private static final Path DEBUGGEE_JAR = Paths
      .get(ToolHelper.BUILD_DIR, "test", "debug_test_resources.jar");
  private static final Path DEBUGGEE_JAVA8_JAR = Paths
      .get(ToolHelper.BUILD_DIR, "test", "debug_test_resources_java8.jar");

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();
  private static Path jdwpDexD8 = null;
  private static Path debuggeeDexD8 = null;
  private static Path debuggeeJava8DexD8 = null;

  @Rule
  public TestName testName = new TestName();

  @BeforeClass
  public static void setUp() throws Exception {
    // Convert jar to dex with d8 with debug info
    int minSdk = ToolHelper.getMinApiLevelForDexVm(ToolHelper.getDexVm());
    {
      Path dexOutputDir = temp.newFolder("d8-jdwp-jar").toPath();
      jdwpDexD8 = dexOutputDir.resolve("classes.dex");
      ToolHelper.runD8(
          D8Command.builder()
              .addProgramFiles(JDWP_JAR)
              .setOutputPath(dexOutputDir)
              .setMinApiLevel(minSdk)
              .setMode(CompilationMode.DEBUG)
              .build());
    }
    {
      Path dexOutputDir = temp.newFolder("d8-debuggee-jar").toPath();
      debuggeeDexD8 = dexOutputDir.resolve("classes.dex");
      ToolHelper.runD8(
          D8Command.builder()
              .addProgramFiles(DEBUGGEE_JAR)
              .setOutputPath(dexOutputDir)
              .setMinApiLevel(minSdk)
              .setMode(CompilationMode.DEBUG)
              .build());
    }
    {
      Path dexOutputDir = temp.newFolder("d8-debuggee-java8-jar").toPath();
      debuggeeJava8DexD8 = dexOutputDir.resolve("classes.dex");
      ToolHelper.runD8(
          D8Command.builder()
              .addProgramFiles(DEBUGGEE_JAVA8_JAR)
              .setOutputPath(dexOutputDir)
              .setMinApiLevel(minSdk)
              .setMode(CompilationMode.DEBUG)
              .build(),
          options -> {
            // Enable desugaring for preN runtimes
            options.interfaceMethodDesugaring = OffOrAuto.Auto;
          });
    }
  }

  protected final boolean supportsDefaultMethod() {
    return RUNTIME_KIND == RuntimeKind.JAVA ||
        ToolHelper.getMinApiLevelForDexVm(ToolHelper.getDexVm()) >= Constants.ANDROID_N_API;
  }

  protected final boolean isRunningJava() {
    return RUNTIME_KIND == RuntimeKind.JAVA;
  }

  protected final void runDebugTest(String debuggeeClass, JUnit3Wrapper.Command... commands)
      throws Throwable {
    runDebugTest(debuggeeClass, Arrays.asList(commands));
  }

  protected final void runDebugTest(String debuggeeClass, List<JUnit3Wrapper.Command> commands)
      throws Throwable {
    runDebugTest(false, debuggeeClass, commands);
  }

  protected final void runDebugTestJava8(String debuggeeClass, JUnit3Wrapper.Command... commands)
      throws Throwable {
    runDebugTestJava8(debuggeeClass, Arrays.asList(commands));
  }

  protected final void runDebugTestJava8(String debuggeeClass, List<JUnit3Wrapper.Command> commands)
      throws Throwable {
    runDebugTest(true, debuggeeClass, commands);
  }

  private void runDebugTest(boolean useJava8, String debuggeeClass,
      List<JUnit3Wrapper.Command> commands)
      throws Throwable {
    // Skip test due to unsupported runtime.
    Assume.assumeTrue("Skipping test " + testName.getMethodName() + " because ART is not supported",
        ToolHelper.artSupported());
    Assume.assumeFalse(
        "Skipping failing test " + testName.getMethodName() + " for runtime " + ToolHelper
            .getDexVm(), UNSUPPORTED_ART_VERSIONS.contains(ToolHelper.getDexVm()));

    String[] paths;
    if (RUNTIME_KIND == RuntimeKind.JAVA) {
      paths = new String[] {
          JDWP_JAR.toString(),
          useJava8 ? DEBUGGEE_JAVA8_JAR.toString() : DEBUGGEE_JAR.toString()
      };
    } else {
      paths = new String[] {
          jdwpDexD8.toString(),
          useJava8 ? debuggeeJava8DexD8.toString() : debuggeeDexD8.toString()
      };
    }
    new JUnit3Wrapper(debuggeeClass, paths, commands).runBare();
  }

  protected final JUnit3Wrapper.Command run() {
    return new JUnit3Wrapper.Command.RunCommand();
  }

  protected final JUnit3Wrapper.Command breakpoint(String className, String methodName) {
    return new JUnit3Wrapper.Command.BreakpointCommand(className, methodName);
  }

  protected final JUnit3Wrapper.Command stepOver() {
    return stepOver(DEFAULT_FILTER);
  }

  protected final JUnit3Wrapper.Command stepOver(StepFilter stepFilter) {
    return step(StepDepth.OVER, stepFilter);
  }

  protected final JUnit3Wrapper.Command stepOut() {
    return stepOut(DEFAULT_FILTER);
  }

  protected final JUnit3Wrapper.Command stepOut(StepFilter stepFilter) {
    return step(StepDepth.OUT, stepFilter);
  }

  protected final JUnit3Wrapper.Command stepInto() {
    return stepInto(DEFAULT_FILTER);
  }

  protected final JUnit3Wrapper.Command stepInto(StepFilter stepFilter) {
    return step(StepDepth.INTO, stepFilter);
  }

  private JUnit3Wrapper.Command step(byte stepDepth,
      StepFilter stepFilter) {
    return new JUnit3Wrapper.Command.StepCommand(stepDepth, stepFilter);
  }

  protected final JUnit3Wrapper.Command checkLocal(String localName) {
    return inspect(t -> t.checkLocal(localName));
  }

  protected final JUnit3Wrapper.Command checkLocal(String localName, Value expectedValue) {
    return inspect(t -> t.checkLocal(localName, expectedValue));
  }

  protected final JUnit3Wrapper.Command checkNoLocal() {
    return inspect(t -> Assert.assertTrue(t.getLocalNames().isEmpty()));
  }

  protected final JUnit3Wrapper.Command checkLine(int line) {
    return inspect(t -> t.checkLine(line));
  }

  protected final JUnit3Wrapper.Command checkMethod(String className, String methodName) {
    return inspect(t -> t.checkMethod(className, methodName));
  }

  protected final JUnit3Wrapper.Command inspect(Consumer<JUnit3Wrapper.DebuggeeState> inspector) {
    return t -> inspector.accept(t.debuggeeState);
  }

  protected final JUnit3Wrapper.Command setLocal(String localName, Value newValue) {
    return new JUnit3Wrapper.Command.SetLocalCommand(localName, newValue);
  }

  @Ignore("Prevents Gradle from running the wrapper as a test.")
  static class JUnit3Wrapper extends JDWPTestCase {

    private final String debuggeeClassName;

    private final String[] debuggeePath;

    // Initially, the runtime is suspended so we're ready to process commands.
    private State state = State.ProcessCommand;

    /**
     * Represents the context of the debuggee suspension. This is {@code null} when the debuggee is
     * not suspended.
     */
    private DebuggeeState debuggeeState = null;

    private final Queue<Command> commandsQueue;

    // Active event requests.
    private final Map<Integer, EventHandler> events = new TreeMap<>();

    JUnit3Wrapper(String debuggeeClassName, String[] debuggeePath, List<Command> commands) {
      this.debuggeeClassName = debuggeeClassName;
      this.debuggeePath = debuggeePath;
      this.commandsQueue = new ArrayDeque<>(commands);
    }

    @Override
    protected void runTest() throws Throwable {
      if (DEBUG_TESTS) {
        logWriter.println("Starts loop with " + commandsQueue.size() + " command(s) to process");
      }

      boolean exited = false;
      while (!exited) {
        if (DEBUG_TESTS) {
          logWriter.println("Loop on state " + state.name());
        }
        switch (state) {
          case ProcessCommand: {
            Command command = commandsQueue.poll();
            assert command != null;
            if (DEBUG_TESTS) {
              logWriter.println("Process command " + command.toString());
            }
            command.perform(this);
            break;
          }
          case WaitForEvent:
            processEvents();
            break;
          case Exit:
            exited = true;
            break;
          default:
            throw new AssertionError();
        }
      }

      assertTrue("All commands have NOT been processed", commandsQueue.isEmpty());

      logWriter.println("Finish loop");
    }

    @Override
    protected String getDebuggeeClassName() {
      return debuggeeClassName;
    }

    private enum State {
      /**
       * Process next command
       */
      ProcessCommand,
      /**
       * Wait for the next event
       */
      WaitForEvent,
      /**
       * The debuggee has exited
       */
      Exit
    }

    private void processEvents() {
      EventPacket eventPacket = getMirror().receiveEvent();
      ParsedEvent[] parsedEvents = ParsedEvent.parseEventPacket(eventPacket);
      if (DEBUG_TESTS) {
        logWriter.println("Received " + parsedEvents.length + " event(s)");
        for (int i = 0; i < parsedEvents.length; ++i) {
          String msg = String.format("#%d: %s (id=%d)", Integer.valueOf(i),
              JDWPConstants.EventKind.getName(parsedEvents[i].getEventKind()),
              Integer.valueOf(parsedEvents[i].getRequestID()));
          logWriter.println(msg);
        }
      }
      // We only expect one event at a time.
      assertEquals(1, parsedEvents.length);
      ParsedEvent parsedEvent = parsedEvents[0];
      byte eventKind = parsedEvent.getEventKind();
      int requestID = parsedEvent.getRequestID();

      if (eventKind == JDWPConstants.EventKind.VM_DEATH) {
        // Special event when debuggee is about to terminate.
        assertEquals(0, requestID);
        setState(State.Exit);
      } else {
        assert parsedEvent.getSuspendPolicy() == SuspendPolicy.ALL;

        // Capture the context of the event suspension.
        updateEventContext((EventThread) parsedEvent);

        if (DEBUG_TESTS && debuggeeState.location != null) {
          // Dump location
          String classSig = getMirror().getClassSignature(debuggeeState.location.classID);
          String methodName = getMirror()
              .getMethodName(debuggeeState.location.classID, debuggeeState.location.methodID);
          String methodSig = getMirror()
              .getMethodSignature(debuggeeState.location.classID, debuggeeState.location.methodID);
          System.out.println(String
              .format("Suspended in %s#%s%s@%x", classSig, methodName, methodSig,
                  Long.valueOf(debuggeeState.location.index)));
        }

        // Handle event.
        EventHandler eh = events.get(requestID);
        assert eh != null;
        eh.handle(this);
      }
    }

    @Override
    protected JPDATestOptions createTestOptions() {
      // Override properties to run debuggee with ART/Dalvik.
      class ArtTestOptions extends JPDATestOptions {

        ArtTestOptions(String[] debuggeePath) {
          // Set debuggee command-line.
          if (RUNTIME_KIND == RuntimeKind.ART) {
            ArtCommandBuilder artCommandBuilder = new ArtCommandBuilder(ToolHelper.getDexVm());
            if (ToolHelper.getDexVm().isNewerThan(DexVm.ART_5_1_1)) {
              artCommandBuilder.appendArtOption("-Xcompiler-option");
              artCommandBuilder.appendArtOption("--debuggable");
              artCommandBuilder.appendArtOption("-Xcompiler-option");
              artCommandBuilder.appendArtOption("--compiler-filter=interpret-only");
            }
            setProperty("jpda.settings.debuggeeJavaPath", artCommandBuilder.build());
          }

          // Set debuggee classpath
          String debuggeeClassPath = String.join(File.pathSeparator, debuggeePath);
          setProperty("jpda.settings.debuggeeClasspath", debuggeeClassPath);

          // Set verbosity
          setProperty("jpda.settings.verbose", Boolean.toString(DEBUG_TESTS));
        }
      }
      return new ArtTestOptions(debuggeePath);
    }

    //
    // Inspection
    //

    /**
     * Allows to inspect the state of a debuggee when it is suspended.
     */
    public class DebuggeeState {

      private final long threadId;
      private final long frameId;
      private final Location location;

      public DebuggeeState(long threadId, long frameId, Location location) {
        this.threadId = threadId;
        this.frameId = frameId;
        this.location = location;
      }

      public long getThreadId() {
        return threadId;
      }

      public long getFrameId() {
        return frameId;
      }

      public Location getLocation() {
        return this.location;
      }

      public void checkLocal(String localName) {
        getVariableAt(getLocation(), localName);
      }

      public void checkLocal(String localName, Value expectedValue) {
        Variable localVar = getVariableAt(getLocation(), localName);

        // Get value
        CommandPacket commandPacket = new CommandPacket(
            JDWPCommands.StackFrameCommandSet.CommandSetID,
            JDWPCommands.StackFrameCommandSet.GetValuesCommand);
        commandPacket.setNextValueAsThreadID(getThreadId());
        commandPacket.setNextValueAsFrameID(getFrameId());
        commandPacket.setNextValueAsInt(1);
        commandPacket.setNextValueAsInt(localVar.getSlot());
        commandPacket.setNextValueAsByte(localVar.getTag());
        ReplyPacket replyPacket = getMirror().performCommand(commandPacket);
        checkReplyPacket(replyPacket, "StackFrame.GetValues command");
        int valuesCount = replyPacket.getNextValueAsInt();
        assert valuesCount == 1;
        Value localValue = replyPacket.getNextValueAsValue();
        assertAllDataRead(replyPacket);

        Assert.assertEquals(expectedValue, localValue);
      }

      public void checkLine(int line) {
        Location location = getLocation();
        int currentLine = getMirror()
            .getLineNumber(location.classID, location.methodID, location.index);
        Assert.assertEquals(line, currentLine);
      }

      public List<String> getLocalNames() {
        return getVariablesAt(location).stream().map(v -> v.getName()).collect(Collectors.toList());
      }

      public void checkMethod(String className, String methodName) {
        String currentClassSig = getMirror().getClassSignature(location.classID);
        assert currentClassSig.charAt(0) == 'L';
        String currentClassName = currentClassSig.substring(1, currentClassSig.length() - 1)
            .replace('/', '.');
        Assert.assertEquals("Incorrect class name", className, currentClassName);

        String currentMethodName = getMirror().getMethodName(location.classID, location.methodID);
        Assert.assertEquals("Incorrect method name", methodName, currentMethodName);
      }
    }

    private static boolean inScope(long index, Variable var) {
      long varStart = var.getCodeIndex();
      long varEnd = varStart + var.getLength();
      return index >= varStart && index < varEnd;
    }

    private Variable getVariableAt(Location location, String localName) {
      return getVariablesAt(location).stream()
          .filter(v -> localName.equals(v.getName()))
          .findFirst()
          .get();
    }

    private List<Variable> getVariablesAt(Location location) {
      // Get variable table and keep only variables visible at this location.
      return getVariables(location.classID, location.methodID).stream()
          .filter(v -> inScope(location.index, v))
          .collect(Collectors.toList());
    }

    private List<Variable> getVariables(long classID, long methodID) {
      List<Variable> list = getMirror().getVariableTable(classID, methodID);
      return list != null ? list : Collections.emptyList();
    }

    private void setState(State state) {
      this.state = state;
    }

    public DebuggeeState getDebuggeeState() {
      return debuggeeState;
    }

    private void updateEventContext(EventThread event) {
      long threadId = event.getThreadID();
      long frameId = -1;
      Location location = null;
      // ART returns an error if we ask for frames when there is none. Workaround by asking the frame
      // count first.
      int frameCount = getMirror().getFrameCount(threadId);
      if (frameCount > 0) {
        ReplyPacket replyPacket = getMirror().getThreadFrames(threadId, 0, 1);
        {
          int number = replyPacket.getNextValueAsInt();
          assertEquals(1, number);
        }
        frameId = replyPacket.getNextValueAsFrameID();
        location = replyPacket.getNextValueAsLocation();
        assertAllDataRead(replyPacket);
      }
      debuggeeState = new DebuggeeState(threadId, frameId, location);
    }

    private VmMirror getMirror() {
      return debuggeeWrapper.vmMirror;
    }

    private void resume() {
      debuggeeState = null;
      getMirror().resume();
      setState(State.WaitForEvent);
    }

    private boolean installBreakpoint(BreakpointInfo breakpointInfo) {
      final long classId = getMirror().getClassID(getClassSignature(breakpointInfo.className));
      if (classId == -1) {
        // The class is not ready yet. Request a CLASS_PREPARE to delay the installation of the
        // breakpoint.
        ReplyPacket replyPacket = getMirror().setClassPrepared(breakpointInfo.className);
        int classPrepareRequestId = replyPacket.getNextValueAsInt();
        assertAllDataRead(replyPacket);
        events.put(Integer.valueOf(classPrepareRequestId),
            new ClassPrepareHandler(breakpointInfo, classPrepareRequestId));
        return false;
      } else {
        int breakpointId = getMirror()
            .setBreakpointAtMethodBegin(classId, breakpointInfo.methodName);
        // Nothing to do on breakpoint
        events.put(Integer.valueOf(breakpointId), new DefaultEventHandler());
        return true;
      }
    }

    //
    // Command processing
    //
    public interface Command {

      void perform(JUnit3Wrapper testBase);

      class RunCommand implements Command {

        @Override
        public void perform(JUnit3Wrapper testBase) {
          testBase.resume();
        }

        @Override
        public String toString() {
          return "run";
        }
      }

      // TODO(shertz) add method signature support (when multiple methods have the same name)
      class BreakpointCommand implements Command {

        private final String className;
        private final String methodName;

        public BreakpointCommand(String className, String methodName) {
          assert className != null;
          assert methodName != null;
          this.className = className;
          this.methodName = methodName;
        }

        @Override
        public void perform(JUnit3Wrapper testBase) {
          testBase.installBreakpoint(new BreakpointInfo(className, methodName));
        }

        @Override
        public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("breakpoint");
          sb.append(" class=");
          sb.append(className);
          sb.append(" method=");
          sb.append(methodName);
          return sb.toString();
        }
      }

      class StepCommand implements Command {

        private final byte stepDepth;
        private final StepFilter stepFilter;

        public StepCommand(byte stepDepth,
            StepFilter stepFilter) {
          this.stepDepth = stepDepth;
          this.stepFilter = stepFilter;
        }

        @Override
        public void perform(JUnit3Wrapper testBase) {
          long threadId = testBase.getDebuggeeState().getThreadId();
          int stepRequestID;
          {
            EventBuilder eventBuilder = Event.builder(EventKind.SINGLE_STEP, SuspendPolicy.ALL);
            eventBuilder.setStep(threadId, StepSize.LINE, stepDepth);
            stepFilter.getExcludedClasses().stream().forEach(s -> eventBuilder.setClassExclude(s));
            ReplyPacket replyPacket = testBase.getMirror().setEvent(eventBuilder.build());
            stepRequestID = replyPacket.getNextValueAsInt();
            testBase.assertAllDataRead(replyPacket);
          }
          testBase.events.put(stepRequestID, new StepEventHandler(stepRequestID, stepFilter));

          // Resume all threads.
          testBase.resume();
        }

        @Override
        public String toString() {
          return "step " + JDWPConstants.StepDepth.getName(stepDepth);
        }
      }

      class SetLocalCommand implements Command {

        private final String localName;
        private final Value newValue;

        public SetLocalCommand(String localName, Value newValue) {
          this.localName = localName;
          this.newValue = newValue;
        }

        @Override
        public void perform(JUnit3Wrapper testBase) {
          Variable v = testBase.getVariableAt(testBase.debuggeeState.location, localName);
          CommandPacket setValues = new CommandPacket(StackFrameCommandSet.CommandSetID,
              StackFrameCommandSet.SetValuesCommand);
          setValues.setNextValueAsThreadID(testBase.getDebuggeeState().getThreadId());
          setValues.setNextValueAsFrameID(testBase.getDebuggeeState().getFrameId());
          setValues.setNextValueAsInt(1);
          setValues.setNextValueAsInt(v.getSlot());
          setValues.setNextValueAsValue(newValue);
          ReplyPacket replyPacket = testBase.getMirror().performCommand(setValues);
          testBase.checkReplyPacket(replyPacket, "StackFrame.SetValues");
        }
      }
    }

    //
    // Event handling
    //
    private interface EventHandler {

      void handle(JUnit3Wrapper testBase);
    }

    private static class DefaultEventHandler implements EventHandler {

      @Override
      public void handle(JUnit3Wrapper testBase) {
        testBase.setState(State.ProcessCommand);
      }
    }

    private static class StepEventHandler extends DefaultEventHandler {

      private final int stepRequestID;
      private final StepFilter stepFilter;

      private StepEventHandler(int stepRequestID,
          StepFilter stepFilter) {
        this.stepRequestID = stepRequestID;
        this.stepFilter = stepFilter;
      }

      @Override
      public void handle(JUnit3Wrapper testBase) {
        if (stepFilter
            .skipLocation(testBase.getMirror(), testBase.getDebuggeeState().getLocation())) {
          // Keep the step active and resume so that we do another step.
          testBase.resume();
        } else {
          // When hit, the single step must be cleared.
          testBase.getMirror().clearEvent(EventKind.SINGLE_STEP, stepRequestID);
          testBase.events.remove(Integer.valueOf(stepRequestID));
          super.handle(testBase);
        }
      }
    }

    private static class BreakpointInfo {

      private final String className;
      private final String methodName;

      private BreakpointInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
      }
    }

    /**
     * CLASS_PREPARE signals us that we can install a breakpoint
     */
    private static class ClassPrepareHandler implements EventHandler {

      private final BreakpointInfo breakpointInfo;
      private final int classPrepareRequestId;

      private ClassPrepareHandler(BreakpointInfo breakpointInfo, int classPrepareRequestId) {
        this.breakpointInfo = breakpointInfo;
        this.classPrepareRequestId = classPrepareRequestId;
      }

      @Override
      public void handle(JUnit3Wrapper testBase) {
        // Remove the CLASS_PREPARE
        testBase.events.remove(Integer.valueOf(classPrepareRequestId));
        testBase.getMirror().clearEvent(JDWPConstants.EventKind.CLASS_PREPARE,
            classPrepareRequestId);

        // Install breakpoint now.
        boolean success = testBase.installBreakpoint(breakpointInfo);
        Assert.assertTrue("Failed to insert breakpoint after class has been prepared", success);

        // Resume now
        testBase.resume();
      }
    }
  }

  //
  // Step filtering
  //

  interface StepFilter {

    /**
     * Provides a list of class name to be skipped when single stepping. This can be a fully
     * qualified name (like java.lang.String) or a subpackage (like java.util.*).
     */
    List<String> getExcludedClasses();

    /**
     * Indicates whether the given location must be skipped.
     */
    boolean skipLocation(VmMirror mirror, Location location);

    /**
     * A {@link StepFilter} that does not filter anything.
     */
    class NoStepFilter implements StepFilter {

      @Override
      public List<String> getExcludedClasses() {
        return Collections.emptyList();
      }

      @Override
      public boolean skipLocation(VmMirror mirror, Location location) {
        return false;
      }
    }

    /**
     * A {@link StepFilter} that matches the default behavior of IntelliJ regarding single
     * stepping.
     */
    class IntelliJStepFilter implements StepFilter {
      // This is the value specified by JDWP in documentation of ReferenceType.Methods command.
      private static final int SYNTHETIC_FLAG = 0xF0000000;

      @Override
      public List<String> getExcludedClasses() {
        return Arrays.asList(
            "com.sun.*",
            "java.*",
            "javax.*",
            "org.omg.*",
            "sun.*",
            "jdk.internal.*",
            "junit.*",
            "com.intellij.rt.*",
            "com.yourkit.runtime.*",
            "com.springsource.loaded.*",
            "org.springsource.loaded.*",
            "javassist.*",
            "org.apache.webbeans.*",
            "com.ibm.ws.*",
            "kotlin.*"
        );
      }

      @Override
      public boolean skipLocation(VmMirror mirror, Location location) {
        // TODO(shertz) we also need to skip class loaders to act like IntelliJ.
        // Skip synthetic methods.
        if (isLambdaMethod(mirror, location)) {
          // Lambda methods are synthetic but we do want to stop there.
          if (DEBUG_TESTS) {
            System.out.println("NOT skipping lambda implementation method");
          }
          return false;
        }
        if (isInLambdaClass(mirror, location)) {
          // Lambda classes must be skipped since they are only wrappers around lambda code.
          if (DEBUG_TESTS) {
            System.out.println("Skipping lambda class wrapper method");
          }
          return true;
        }
        if (isSyntheticMethod(mirror, location)) {
          if (DEBUG_TESTS) {
            System.out.println("Skipping synthetic method");
          }
          return true;
        }
        return false;
      }

      private static boolean isSyntheticMethod(VmMirror mirror, Location location) {
        // We must gather the modifiers of the method. This is only possible using
        // ReferenceType.Methods command which gather information about all methods in a class.
        ReplyPacket reply = mirror.getMethods(location.classID);
        int methodsCount = reply.getNextValueAsInt();
        for (int i = 0; i < methodsCount; ++i) {
          long methodId = reply.getNextValueAsMethodID();
          reply.getNextValueAsString();  // skip method name
          reply.getNextValueAsString();  // skip method signature
          int modifiers = reply.getNextValueAsInt();
          if (methodId == location.methodID &&
              ((modifiers & SYNTHETIC_FLAG) != 0)) {
            return true;
          }
        }
        return false;
      }

      private static boolean isInLambdaClass(VmMirror mirror, Location location) {
        String classSig = mirror.getClassSignature(location.classID);
        return classSig.contains("$$Lambda$");
      }

      private boolean isLambdaMethod(VmMirror mirror, Location location) {
        String methodName = mirror.getMethodName(location.classID, location.methodID);
        return methodName.startsWith("lambda$");
      }
    }
  }

}
