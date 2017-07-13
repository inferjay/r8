// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import static com.android.tools.r8.TestCondition.D8_COMPILER;
import static com.android.tools.r8.TestCondition.R8_AFTER_D8_COMPILER;
import static com.android.tools.r8.TestCondition.R8_COMPILER;
import static com.android.tools.r8.TestCondition.R8_NOT_AFTER_D8_COMPILER;
import static com.android.tools.r8.TestCondition.any;
import static com.android.tools.r8.TestCondition.match;
import static com.android.tools.r8.TestCondition.runtimes;

import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.R8RunArtTestsTest.DexTool;
import com.android.tools.r8.ToolHelper.DexVm;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;

public class JctfTestSpecifications {

  public enum Outcome {
    PASSES,
    FAILS_WITH_ART,
    TIMEOUTS_WITH_ART,
    FLAKY_WITH_ART
  }

  public static final Multimap<String, TestCondition> failuresToTriage =
      new ImmutableListMultimap.Builder<String, TestCondition>()

          .put("math.BigInteger.nextProbablePrime.BigInteger_nextProbablePrime_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.ArithmeticException

          .put("math.BigInteger.ConstructorLjava_lang_String.BigInteger_Constructor_A02", any())
          // 1) t03
          // java.lang.AssertionError: Expected exception: java.lang.NumberFormatException

          .put("lang.StringBuffer.insertILjava_lang_Object.StringBuffer_insert_A01",
              match(runtimes(DexVm.ART_DEFAULT)))
          // 1) t01
          // java.lang.StringIndexOutOfBoundsException: length=21; regionStart=0; regionLength=42

          .put("lang.StringBuffer.serialization.StringBuffer_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/StringBuffer/serialization/StringBuffer_serialization_A01.golden.ser

          .put(
              "lang.CloneNotSupportedException.serialization.CloneNotSupportedException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/CloneNotSupportedException/serialization/CloneNotSupportedException_serialization_A01.golden.0.ser

          .put("lang.NumberFormatException.serialization.NumberFormatException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NumberFormatException/serialization/NumberFormatException_serialization_A01.golden.0.ser

          .put("lang.StrictMath.roundF.StrictMath_round_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t01
          // java.lang.AssertionError: Wrong result produced for argument: 0.49999997 expected:<1.0> but was:<0.0>

          .put("lang.StrictMath.roundD.StrictMath_round_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t01
          // java.lang.AssertionError: Wrong result produced for argument: 0.49999999999999994 expected:<1.0> but was:<0.0>

          .put("lang.StrictMath.atan2DD.StrictMath_atan2_A01", any())
          // 1) t01
          // java.lang.AssertionError: Bad value returned for arguments: 2.225073858507201E-308 0.9999999999999999 expected:<2.2250738585072014E-308> but was:<2.225073858507201E-308>

          .put("lang.Thread.stop.Thread_stop_A05", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.resume.Thread_resume_A02", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.suspend.Thread_suspend_A02", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException
          // 3) t03
          // java.lang.UnsupportedOperationException
          // 4) t04
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.stop.Thread_stop_A03", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException
          // 3) t03
          // java.lang.UnsupportedOperationException
          // 4) t04
          // java.lang.UnsupportedOperationException
          // 5) t05
          // java.lang.UnsupportedOperationException
          // 6) t06
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.interrupt.Thread_interrupt_A03", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.Thread.stop.Thread_stop_A04", any())
          // 1) t01
          // java.lang.UnsupportedOperationException

          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0, DexVm.ART_6_0_1)))
          // 1) t01
          // java.lang.OutOfMemoryError: pthread_create (-8589934591GB stack) failed: Resource temporarily unavailable

          .put("lang.Thread.getUncaughtExceptionHandler.Thread_getUncaughtExceptionHandler_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: expected null, but was:<java.lang.ThreadGroup[name=main,maxpri=10]>

          .put("lang.Thread.getStackTrace.Thread_getStackTrace_A02", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<get[]StackTrace> but was:<get[Thread]StackTrace>

          .put("lang.Thread.enumerate_Ljava_lang_Thread.Thread_enumerate_A02", any())
          // 1) t01
          // java.lang.AssertionError: test failed with error:java.lang.SecurityException

          .put("lang.Thread.countStackFrames.Thread_countStackFrames_A01", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalThreadStateException
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.IllegalThreadStateException
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.IllegalThreadStateException
          // 4) t04
          // java.lang.AssertionError: Expected exception: java.lang.IllegalThreadStateException

          .put("lang.Thread.getAllStackTraces.Thread_getAllStackTraces_A01",
              match(runtimes(DexVm.ART_7_0_0)))
          // 1) t01
          // java.lang.AssertionError

          .put("lang.Thread.destroy.Thread_destroy_A01", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NoSuchMethodError> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.NoSuchMethodError> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException

          .put("lang.Thread.isAlive.Thread_isAlive_A01", any())
          // 1) t01
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.stopLjava_lang_Throwable.Thread_stop_A04", any())
          // 1) t01
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.stopLjava_lang_Throwable.Thread_stop_A03", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException
          // 3) t03
          // java.lang.UnsupportedOperationException
          // 4) t05
          // java.lang.UnsupportedOperationException
          // 5) t06
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.stopLjava_lang_Throwable.Thread_stop_A05", any())
          // 1) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException

          .put("lang.Thread.getPriority.Thread_getPriority_A01", any())
          // 1) t02
          // java.lang.UnsupportedOperationException

          .put("lang.Thread.getContextClassLoader.Thread_getContextClassLoader_A03",
              match(runtimes(DexVm.ART_7_0_0)))
          // 1) t01
          // java.lang.AssertionError: improper ClassLoader expected same:<null> was not:<dalvik.system.PathClassLoader[DexPathList[[dex file "/tmp/junit7794202178392390143/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]]>

          .put("lang.OutOfMemoryError.serialization.OutOfMemoryError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/OutOfMemoryError/serialization/OutOfMemoryError_serialization_A01.golden.0.ser

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_StringLjava_lang_String.RuntimePermission_Constructor_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected:<a> but was:<null>
          // 2) t02
          // java.lang.AssertionError: expected:<2/3/2> but was:<null>

          .put("lang.RuntimePermission.serialization.RuntimePermission_serialization_A01", any())
          // 1) t01
          // java.lang.RuntimeException: Failed to detect comparator
          // 2) t02
          // java.lang.RuntimeException: Failed to detect comparator

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_StringLjava_lang_String.RuntimePermission_Constructor_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_StringLjava_lang_String.RuntimePermission_Constructor_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A17", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_String.RuntimePermission_Constructor_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_String.RuntimePermission_Constructor_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.RuntimePermission.ConstructorLjava_lang_String.RuntimePermission_Constructor_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected:<a> but was:<null>
          // 2) t02
          // java.lang.AssertionError: expected:<2/3/2> but was:<null>

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A26", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A04", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String/Thread_Constructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A04" on path: DexPathList[[dex file "/tmp/junit4094594533964383293/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ/Thread_Constructor_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A05" on path: DexPathList[[dex file "/tmp/junit4094594533964383293/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String/Thread_Constructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A04" on path: DexPathList[[dex file "/tmp/junit4094594533964383293/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ/Thread_Constructor_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A05" on path: DexPathList[[dex file "/tmp/junit4094594533964383293/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A03", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setContextClassLoaderLjava_lang_ClassLoader/Thread_setContextClassLoader_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setContextClassLoaderLjava_lang_ClassLoader.Thread_setContextClassLoader_A02" on path: DexPathList[[dex file "/tmp/junit8983002984475576815/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setContextClassLoaderLjava_lang_ClassLoader/Thread_setContextClassLoader_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setContextClassLoaderLjava_lang_ClassLoader.Thread_setContextClassLoader_A02" on path: DexPathList[[dex file "/tmp/junit8983002984475576815/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A25", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setDefaultUncaughtExceptionHandler/Thread_setDefaultUncaughtExceptionHandler_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setDefaultUncaughtExceptionHandler.Thread_setDefaultUncaughtExceptionHandler_A02" on path: DexPathList[[dex file "/tmp/junit1629291049961551929/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setDefaultUncaughtExceptionHandler/Thread_setDefaultUncaughtExceptionHandler_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setDefaultUncaughtExceptionHandler.Thread_setDefaultUncaughtExceptionHandler_A02" on path: DexPathList[[dex file "/tmp/junit1629291049961551929/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A06", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/Constructor/SecurityManager_Constructor_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.Constructor.SecurityManager_Constructor_A01" on path: DexPathList[[dex file "/tmp/junit4316252965733666132/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/Constructor/SecurityManager_Constructor_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.Constructor.SecurityManager_Constructor_A01" on path: DexPathList[[dex file "/tmp/junit4316252965733666132/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A21", any())
          // 1) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkPackageDefinitionLjava_lang_String/SecurityManager_checkPackageDefinition_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkPackageDefinitionLjava_lang_String.SecurityManager_checkPackageDefinition_A01" on path: DexPathList[[dex file "/tmp/junit622469116149972008/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A22", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredClasses/Class_getDeclaredClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredClasses.Class_getDeclaredClasses_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructors/Class_getDeclaredConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructors.Class_getDeclaredConstructors_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructor_Ljava_lang_Class/Class_getDeclaredConstructor_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A03" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFields/Class_getDeclaredFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFields.Class_getDeclaredFields_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFieldLjava_lang_String/Class_getDeclaredField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFieldLjava_lang_String.Class_getDeclaredField_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethods/Class_getDeclaredMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 7) t07
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethodLjava_lang_String_Ljava_lang_Class/Class_getDeclaredMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A05" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 8) t08
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredClasses/Class_getDeclaredClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredClasses.Class_getDeclaredClasses_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 9) t09
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructors/Class_getDeclaredConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructors.Class_getDeclaredConstructors_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 10) t10
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructor_Ljava_lang_Class/Class_getDeclaredConstructor_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A03" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 11) t11
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFields/Class_getDeclaredFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFields.Class_getDeclaredFields_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 12) t12
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFieldLjava_lang_String/Class_getDeclaredField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFieldLjava_lang_String.Class_getDeclaredField_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 13) t13
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethods/Class_getDeclaredMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 14) t14
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethodLjava_lang_String_Ljava_lang_Class/Class_getDeclaredMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A05" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 15) t15
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/newInstance/Class_newInstance_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.newInstance.Class_newInstance_A07" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 16) t16
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/newInstance/Class_newInstance_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.newInstance.Class_newInstance_A07" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 17) t17
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getClasses/Class_getClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getClasses.Class_getClasses_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 18) t18
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructors/Class_getConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructors.Class_getConstructors_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 19) t19
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructor_Ljava_lang_Class/Class_getConstructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 20) t20
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFields/Class_getFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFields.Class_getFields_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 21) t21
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFieldLjava_lang_String/Class_getField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 22) t22
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethods/Class_getMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethods.Class_getMethods_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 23) t23
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethodLjava_lang_String_Ljava_lang_Class/Class_getMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A05" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 24) t24
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getClasses/Class_getClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getClasses.Class_getClasses_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 25) t25
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructors/Class_getConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructors.Class_getConstructors_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 26) t26
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructor_Ljava_lang_Class/Class_getConstructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 27) t27
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFields/Class_getFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFields.Class_getFields_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 28) t28
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFieldLjava_lang_String/Class_getField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A04" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 29) t29
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethods/Class_getMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethods.Class_getMethods_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 30) t30
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethodLjava_lang_String_Ljava_lang_Class/Class_getMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A05" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 31) t31
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkMemberAccessLjava_lang_ClassI/SecurityManager_checkMemberAccess_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkMemberAccessLjava_lang_ClassI.SecurityManager_checkMemberAccess_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 32) t32
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkMemberAccessLjava_lang_ClassI/SecurityManager_checkMemberAccess_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkMemberAccessLjava_lang_ClassI.SecurityManager_checkMemberAccess_A02" on path: DexPathList[[dex file "/tmp/junit2603421343038865741/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A11", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setInLjava_io_InputStream/System_setIn_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setInLjava_io_InputStream.System_setIn_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setOutLjava_io_PrintStream/System_setOut_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setOutLjava_io_PrintStream.System_setOut_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setErrLjava_io_PrintStream/System_setErr_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setErrLjava_io_PrintStream.System_setErr_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setInLjava_io_InputStream/System_setIn_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setInLjava_io_InputStream.System_setIn_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setOutLjava_io_PrintStream/System_setOut_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setOutLjava_io_PrintStream.System_setOut_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setErrLjava_io_PrintStream/System_setErr_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setErrLjava_io_PrintStream.System_setErr_A02" on path: DexPathList[[dex file "/tmp/junit8702631411569316964/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A08", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/exitI/Runtime_exit_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A04" on path: DexPathList[[dex file "/tmp/junit161743829053407699/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/haltI/Runtime_halt_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A03" on path: DexPathList[[dex file "/tmp/junit161743829053407699/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/exitI/Runtime_exit_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A04" on path: DexPathList[[dex file "/tmp/junit161743829053407699/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/haltI/Runtime_halt_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A03" on path: DexPathList[[dex file "/tmp/junit161743829053407699/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A16", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A12", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/interrupt/Thread_interrupt_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.interrupt.Thread_interrupt_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/stop/Thread_stop_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.stop.Thread_stop_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/suspend/Thread_suspend_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.suspend.Thread_suspend_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/resume/Thread_resume_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.resume.Thread_resume_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setDaemonZ/Thread_setDaemon_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setDaemonZ.Thread_setDaemon_A03" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setPriorityI/Thread_setPriority_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setPriorityI.Thread_setPriority_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 7) t07
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setNameLjava_lang_String/Thread_setName_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setNameLjava_lang_String.Thread_setName_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 8) t08
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setUncaughtExceptionHandler/Thread_setUncaughtExceptionHandler_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setUncaughtExceptionHandler.Thread_setUncaughtExceptionHandler_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 9) t09
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/interrupt/Thread_interrupt_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.interrupt.Thread_interrupt_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 10) t10
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/suspend/Thread_suspend_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.suspend.Thread_suspend_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 11) t11
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/stop/Thread_stop_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.stop.Thread_stop_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 12) t12
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/resume/Thread_resume_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.resume.Thread_resume_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 13) t13
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setDaemonZ/Thread_setDaemon_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setDaemonZ.Thread_setDaemon_A03" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 14) t14
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setPriorityI/Thread_setPriority_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setPriorityI.Thread_setPriority_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 15) t15
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setNameLjava_lang_String/Thread_setName_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setNameLjava_lang_String.Thread_setName_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 16) t16
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/setUncaughtExceptionHandler/Thread_setUncaughtExceptionHandler_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.setUncaughtExceptionHandler.Thread_setUncaughtExceptionHandler_A02" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 17) t17
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkAccessLjava_lang_Thread/SecurityManager_checkAccess_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkAccessLjava_lang_Thread.SecurityManager_checkAccess_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 18) t18
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkAccessLjava_lang_Thread/SecurityManager_checkAccess_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkAccessLjava_lang_Thread.SecurityManager_checkAccess_A01" on path: DexPathList[[dex file "/tmp/junit1615879415958114883/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A24", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/getAllStackTraces/Thread_getAllStackTraces_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.getAllStackTraces.Thread_getAllStackTraces_A02" on path: DexPathList[[dex file "/tmp/junit6143640280960303446/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/getStackTrace/Thread_getStackTrace_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.getStackTrace.Thread_getStackTrace_A03" on path: DexPathList[[dex file "/tmp/junit6143640280960303446/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/getStackTrace/Thread_getStackTrace_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.getStackTrace.Thread_getStackTrace_A03" on path: DexPathList[[dex file "/tmp/junit6143640280960303446/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Thread/getAllStackTraces/Thread_getAllStackTraces_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Thread.getAllStackTraces.Thread_getAllStackTraces_A02" on path: DexPathList[[dex file "/tmp/junit6143640280960303446/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A23", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A18", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A19", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/loadLjava_lang_String/Runtime_load_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.loadLjava_lang_String.Runtime_load_A03" on path: DexPathList[[dex file "/tmp/junit3066867775929885441/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/loadLibraryLjava_lang_String/Runtime_loadLibrary_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A03" on path: DexPathList[[dex file "/tmp/junit3066867775929885441/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/loadLjava_lang_String/Runtime_load_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.loadLjava_lang_String.Runtime_load_A03" on path: DexPathList[[dex file "/tmp/junit3066867775929885441/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/loadLibraryLjava_lang_String/Runtime_loadLibrary_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A03" on path: DexPathList[[dex file "/tmp/junit3066867775929885441/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A07", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/getenv/System_getenv_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.getenv.System_getenv_A04" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/getenvLjava_lang_String/System_getenv_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.getenvLjava_lang_String.System_getenv_A03" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ProcessBuilder/environment/ProcessBuilder_environment_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ProcessBuilder.environment.ProcessBuilder_environment_A07" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/getenv/System_getenv_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.getenv.System_getenv_A04" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/getenvLjava_lang_String/System_getenv_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.getenvLjava_lang_String.System_getenv_A03" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ProcessBuilder/environment/ProcessBuilder_environment_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ProcessBuilder.environment.ProcessBuilder_environment_A07" on path: DexPathList[[dex file "/tmp/junit8429314639688357329/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A20", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredClasses/Class_getDeclaredClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredClasses.Class_getDeclaredClasses_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructors/Class_getDeclaredConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructors.Class_getDeclaredConstructors_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructor_Ljava_lang_Class/Class_getDeclaredConstructor_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A03" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFields/Class_getDeclaredFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFields.Class_getDeclaredFields_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFieldLjava_lang_String/Class_getDeclaredField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFieldLjava_lang_String.Class_getDeclaredField_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethods/Class_getDeclaredMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 7) t07
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethodLjava_lang_String_Ljava_lang_Class/Class_getDeclaredMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A05" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 8) t08
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredClasses/Class_getDeclaredClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredClasses.Class_getDeclaredClasses_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 9) t09
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructors/Class_getDeclaredConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructors.Class_getDeclaredConstructors_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 10) t10
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredConstructor_Ljava_lang_Class/Class_getDeclaredConstructor_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A03" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 11) t11
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFields/Class_getDeclaredFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFields.Class_getDeclaredFields_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 12) t12
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredFieldLjava_lang_String/Class_getDeclaredField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredFieldLjava_lang_String.Class_getDeclaredField_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 13) t13
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethods/Class_getDeclaredMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 14) t14
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getDeclaredMethodLjava_lang_String_Ljava_lang_Class/Class_getDeclaredMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A05" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 15) t15
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/newInstance/Class_newInstance_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.newInstance.Class_newInstance_A07" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 16) t16
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/newInstance/Class_newInstance_A07;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.newInstance.Class_newInstance_A07" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 17) t17
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getClasses/Class_getClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getClasses.Class_getClasses_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 18) t18
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructors/Class_getConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructors.Class_getConstructors_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 19) t19
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructor_Ljava_lang_Class/Class_getConstructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 20) t20
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFields/Class_getFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFields.Class_getFields_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 21) t21
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFieldLjava_lang_String/Class_getField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 22) t22
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethods/Class_getMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethods.Class_getMethods_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 23) t23
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethodLjava_lang_String_Ljava_lang_Class/Class_getMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A05" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 24) t24
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getClasses/Class_getClasses_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getClasses.Class_getClasses_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 25) t25
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructors/Class_getConstructors_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructors.Class_getConstructors_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 26) t26
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getConstructor_Ljava_lang_Class/Class_getConstructor_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 27) t27
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFields/Class_getFields_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFields.Class_getFields_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 28) t28
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getFieldLjava_lang_String/Class_getField_A04;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A04" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 29) t29
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethods/Class_getMethods_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethods.Class_getMethods_A02" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 30) t30
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getMethodLjava_lang_String_Ljava_lang_Class/Class_getMethod_A05;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A05" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 31) t32
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkPackageAccessLjava_lang_String/SecurityManager_checkPackageAccess_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkPackageAccessLjava_lang_String.SecurityManager_checkPackageAccess_A01" on path: DexPathList[[dex file "/tmp/junit7609456538458065688/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A15", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getProtectionDomain/Class_getProtectionDomain_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getProtectionDomain.Class_getProtectionDomain_A02" on path: DexPathList[[dex file "/tmp/junit2159138358960857439/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Class/getProtectionDomain/Class_getProtectionDomain_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.getProtectionDomain.Class_getProtectionDomain_A02" on path: DexPathList[[dex file "/tmp/junit2159138358960857439/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A05", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setSecurityManagerLjava_lang_SecurityManager/System_setSecurityManager_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setSecurityManagerLjava_lang_SecurityManager.System_setSecurityManager_A01" on path: DexPathList[[dex file "/tmp/junit5991060062859827030/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/System/setSecurityManagerLjava_lang_SecurityManager/System_setSecurityManager_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.System.setSecurityManagerLjava_lang_SecurityManager.System_setSecurityManager_A01" on path: DexPathList[[dex file "/tmp/junit5991060062859827030/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A09", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/addShutdownHookLjava_lang_Thread/Runtime_addShutdownHook_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A01" on path: DexPathList[[dex file "/tmp/junit719694264001364171/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/removeShutdownHookLjava_lang_Thread/Runtime_removeShutdownHook_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A01" on path: DexPathList[[dex file "/tmp/junit719694264001364171/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/addShutdownHookLjava_lang_Thread/Runtime_addShutdownHook_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A01" on path: DexPathList[[dex file "/tmp/junit719694264001364171/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/Runtime/removeShutdownHookLjava_lang_Thread/Runtime_removeShutdownHook_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A01" on path: DexPathList[[dex file "/tmp/junit719694264001364171/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A10", any())
          // 1) t01
          // java.lang.AssertionError: Not implemented

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A01", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ClassLoader/Constructor/ClassLoader_Constructor_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ClassLoader.Constructor.ClassLoader_Constructor_A02" on path: DexPathList[[dex file "/tmp/junit6765412840574788386/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ClassLoader/ConstructorLjava_lang_ClassLoader/ClassLoader_Constructor_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ClassLoader.ConstructorLjava_lang_ClassLoader.ClassLoader_Constructor_A02" on path: DexPathList[[dex file "/tmp/junit6765412840574788386/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ClassLoader/Constructor/ClassLoader_Constructor_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ClassLoader.Constructor.ClassLoader_Constructor_A02" on path: DexPathList[[dex file "/tmp/junit6765412840574788386/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ClassLoader/ConstructorLjava_lang_ClassLoader/ClassLoader_Constructor_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ClassLoader.ConstructorLjava_lang_ClassLoader.ClassLoader_Constructor_A02" on path: DexPathList[[dex file "/tmp/junit6765412840574788386/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A06",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.RuntimePermission.Class.RuntimePermission_class_A14", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/interrupt/ThreadGroup_interrupt_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.interrupt.ThreadGroup_interrupt_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/stop/ThreadGroup_stop_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.stop.ThreadGroup_stop_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/suspend/ThreadGroup_suspend_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.suspend.ThreadGroup_suspend_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/resume/ThreadGroup_resume_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.resume.ThreadGroup_resume_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t05
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/setDaemonZ/ThreadGroup_setDaemon_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.setDaemonZ.ThreadGroup_setDaemon_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 6) t06
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/setMaxPriorityI/ThreadGroup_setMaxPriority_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 7) t07
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/getParent/ThreadGroup_getParent_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.getParent.ThreadGroup_getParent_A03" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 8) t08
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/enumerate_ThreadGroup/ThreadGroup_enumerate_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.enumerate_ThreadGroup.ThreadGroup_enumerate_A03" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 9) t09
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/interrupt/ThreadGroup_interrupt_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.interrupt.ThreadGroup_interrupt_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 10) t10
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/suspend/ThreadGroup_suspend_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.suspend.ThreadGroup_suspend_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 11) t11
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/stop/ThreadGroup_stop_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.stop.ThreadGroup_stop_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 12) t12
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/resume/ThreadGroup_resume_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.resume.ThreadGroup_resume_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 13) t13
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/setDaemonZ/ThreadGroup_setDaemon_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.setDaemonZ.ThreadGroup_setDaemon_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 14) t14
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/setMaxPriorityI/ThreadGroup_setMaxPriority_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 15) t15
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/getParent/ThreadGroup_getParent_A02;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.getParent.ThreadGroup_getParent_A02" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 16) t16
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ThreadGroup/enumerate_ThreadGroup/ThreadGroup_enumerate_A03;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ThreadGroup.enumerate_ThreadGroup.ThreadGroup_enumerate_A03" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 17) t17
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkAccessLjava_lang_ThreadGroup/SecurityManager_checkAccess_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkAccessLjava_lang_ThreadGroup.SecurityManager_checkAccess_A01" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 18) t18
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/SecurityManager/checkAccessLjava_lang_ThreadGroup/SecurityManager_checkAccess_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.SecurityManager.checkAccessLjava_lang_ThreadGroup.SecurityManager_checkAccess_A01" on path: DexPathList[[dex file "/tmp/junit7453598412317397853/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A05",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A02",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A04",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A03",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClassLjava_lang_String_BII.ClassLoader_defineClass_A07",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException: can't load this type of class file

          .put(
              "lang.ClassLoader.setPackageAssertionStatusLjava_lang_StringZ.ClassLoader_setPackageAssertionStatus_A02",
              any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setPackageAssertionStatusLjava_lang_StringZ.ClassLoader_setPackageAssertionStatus_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setPackageAssertionStatusLjava_lang_StringZ.ClassLoader_setPackageAssertionStatus_A03",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.loadClassLjava_lang_StringZ.ClassLoader_loadClass_A03", any())
          // 1) t03
          // java.lang.AssertionError: ClassNotFoundException expected for class: java/lang/Object

          .put("lang.ClassLoader.loadClassLjava_lang_StringZ.ClassLoader_loadClass_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.loadClassLjava_lang_StringZ.ClassLoader_loadClass_A04", any())
          // 1) t01(com.google.jctf.test.lib.java.lang.ClassLoader.loadClassLjava_lang_StringZ.ClassLoader_loadClass_A04)
          // java.lang.AssertionError: NoClassDefFoundError expected for class: com/google/jctf/test/lib/java/lang/ClassLoader/loadClassLjava_lang_StringZ/ClassLoader_loadClass_A04

          .put(
              "lang.ClassLoader.definePackageLjava_lang_String6Ljava_net_URL.ClassLoader_definePackage_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.NoClassDefFoundError: Class not found using the boot class loader; no stack trace available

          .put(
              "lang.ClassLoader.definePackageLjava_lang_String6Ljava_net_URL.ClassLoader_definePackage_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.ClassLoader.definePackageLjava_lang_String6Ljava_net_URL.ClassLoader_definePackage_A03",
              any())
          // 1) t02
          // java.lang.AssertionError: IllegalArgumentException expected for package name: name
          // 2) t03
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A05",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.getResourceLjava_lang_String.ClassLoader_getResource_A01", any())
          // 1) t01
          // java.lang.AssertionError: Resource not found: java/lang/ClassLoader.class
          // 2) t02
          // java.lang.AssertionError: Resource not found:

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A02",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A06",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A03",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A07",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException: can't load this type of class file

          .put(
              "lang.ClassLoader.defineClassLjava_lang_StringLjava_nio_ByteBufferLjava_security_ProtectionDomain.ClassLoader_defineClass_A04",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setSignersLjava_lang_Class_Ljava_lang_Object.ClassLoader_setSigners_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t04
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.ClassLoader.clearAssertionStatus.ClassLoader_clearAssertionStatus_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.Constructor.ClassLoader_Constructor_A02", any())
          // 1) t02
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException

          .put(
              "lang.ClassLoader.getSystemResourceLjava_lang_String.ClassLoader_getSystemResource_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Resource not found: java/lang/ClassLoader.class
          // 2) t02
          // java.lang.AssertionError: Resource not found:

          .put("lang.ClassLoader.getResourcesLjava_lang_String.ClassLoader_getResources_A01", any())
          // 1) t01
          // java.lang.AssertionError: Resource not found: java/lang/ClassLoader.class
          // 2) t04
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.ClassLoader.resolveClassLjava_lang_Class.ClassLoader_resolveClass_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.ClassLoader.getResourceAsStreamLjava_lang_String.ClassLoader_getResourceAsStream_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 2) t04
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.ClassLoader.findLoadedClassLjava_lang_String.ClassLoader_findLoadedClass_A01",
              any())
          // 1) initializationError
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/ClassLoader/findLoadedClassLjava_lang_String/TestLoader;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.ClassLoader.findLoadedClassLjava_lang_String.TestLoader" on path: DexPathList[[dex file "/tmp/junit1789265657215742712/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A02",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.resolveClassLjava_lang_Class.ClassLoader_resolveClass_A01", any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setDefaultAssertionStatusZ.ClassLoader_setDefaultAssertionStatus_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A05",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A06",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A08",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException: can't load this type of class file

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A03",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.getSystemResourcesLjava_lang_String.ClassLoader_getSystemResources_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Resource not found: java/lang/ClassLoader.class
          // 2) t02
          // java.lang.AssertionError: Resource not found:

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A07",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A09",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException: can't load this type of class file

          .put(
              "lang.ClassLoader.defineClassLjava_lang_String_BIILjava_security_ProtectionDomain.ClassLoader_defineClass_A04",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClass_BII.ClassLoader_defineClass_A02", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.getSystemResourceAsStreamLjava_lang_String.ClassLoader_getSystemResourceAsStream_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.getPackages.ClassLoader_getPackages_A01", any())
          // 1) t02
          // java.lang.AssertionError:
          // Expected: (an array containing <package java.lang, Unknown, version 0.0> and an array containing <package java.security, Unknown, version 0.0> and an array containing <package java.util, Unknown, version 0.0>)
          // but: an array containing <package java.lang, Unknown, version 0.0> was []
          // 2) t04
          // java.lang.AssertionError:
          // Expected: an array containing <package com.google.jctf.test.lib.java.lang.ClassLoader.getPackages, Unknown, version 0.0>
          // but: was []
          // 3) t05
          // java.lang.AssertionError: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.ClassNotFoundException: com.android.okhttp.HttpHandler
          // Caused by: java.lang.NoClassDefFoundError: Class not found using the boot class loader; no stack trace available

          .put(
              "lang.ClassLoader.setClassAssertionStatusLjava_lang_StringZ.ClassLoader_setClassAssertionStatus_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClass_BII.ClassLoader_defineClass_A03", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClass_BII.ClassLoader_defineClass_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.defineClass_BII.ClassLoader_defineClass_A04", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.UnsupportedOperationException>
          // Caused by: java.lang.UnsupportedOperationException: can't load this type of class file

          .put("lang.ClassLoader.getParent.ClassLoader_getParent_A01", any())
          // 1) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setClassAssertionStatusLjava_lang_StringZ.ClassLoader_setClassAssertionStatus_A04",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.ClassLoader.setClassAssertionStatusLjava_lang_StringZ.ClassLoader_setClassAssertionStatus_A02",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.ClassLoader.getParent.ClassLoader_getParent_A02", any())
          // 1) t02
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.ClassLoader.getSystemClassLoader.ClassLoader_getSystemClassLoader_A02", any())
          // 1) t02
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException

          .put("lang.ClassLoader.ConstructorLjava_lang_ClassLoader.ClassLoader_Constructor_A02",
              any())
          // 1) t02
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException

          .put("lang.ClassLoader.findSystemClassLjava_lang_String.ClassLoader_findSystemClass_A04",
              any())
          // 1) t01
          // java.lang.ClassNotFoundException: Invalid name: com/google/jctf/test/lib/java/lang/ClassLoader/findSystemClassLjava_lang_String/ClassLoader_findSystemClass_A04

          .put("lang.ClassLoader.getPackageLjava_lang_String.ClassLoader_getPackage_A01", any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.Package.getName()' on a null object reference
          // 2) t04
          // java.lang.AssertionError: expected:<package com.google.jctf.test.lib.java.lang.ClassLoader.getPackageLjava_lang_String, Unknown, version 0.0> but was:<null>
          // 3) t05
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.NoClassDefFoundError.serialization.NoClassDefFoundError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NoClassDefFoundError/serialization/NoClassDefFoundError_serialization_A01.golden.0.ser

          .put(
              "lang.TypeNotPresentException.serialization.TypeNotPresentException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/TypeNotPresentException/serialization/TypeNotPresentException_serialization_A01.golden.0.ser

          .put(
              "lang.IndexOutOfBoundsException.serialization.IndexOutOfBoundsException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IndexOutOfBoundsException/serialization/IndexOutOfBoundsException_serialization_A01.golden.0.ser

          .put("lang.Enum.serialization.Enum_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Enum/serialization/Enum_serialization_A01.golden.0.ser

          .put("lang.Enum.ConstructorLjava_lang_StringI.Enum_Constructor_A01", any())
          // 1) t02
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put("lang.InternalError.serialization.InternalError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/InternalError/serialization/InternalError_serialization_A01.golden.0.ser

          .put("lang.Error.serialization.Error_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Error/serialization/Error_serialization_A01.golden.0.ser

          .put("lang.Runtime.loadLjava_lang_String.Runtime_load_A02", any())
          // 1) t05
          // java.lang.Exception: Unexpected exception, expected<java.lang.UnsatisfiedLinkError> but was<java.lang.AssertionError>
          // Caused by: java.lang.AssertionError: Misconfiguration, java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[DexPathList[[dex file "/tmp/junit955082866383345627/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]] couldn't find "libstub.so"
          // 2) t06
          // java.lang.Exception: Unexpected exception, expected<java.lang.UnsatisfiedLinkError> but was<java.lang.AssertionError>
          // Caused by: java.lang.AssertionError: Misconfiguration, java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[DexPathList[[dex file "/tmp/junit955082866383345627/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]] couldn't find "libstub.so"

          .put("lang.Runtime.loadLjava_lang_String.Runtime_load_A05", any())
          // 1) t01
          // java.lang.AssertionError
          // 2) t02
          // java.lang.AssertionError

          .put("lang.Runtime.loadLjava_lang_String.Runtime_load_A03", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.AssertionError

          .put("lang.Runtime.loadLjava_lang_String.Runtime_load_A04", any())
          // 1) t01
          // java.lang.AssertionError: Misconfiguration, missing property: 'jctf.library.path'
          // 2) t02
          // java.lang.AssertionError

          .put("lang.Runtime.exec_Ljava_lang_String.Runtime_exec_A02", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exec_Ljava_lang_String.Runtime_exec_A02$T01
          // ]>

          .put("lang.Runtime.exec_Ljava_lang_String.Runtime_exec_A03", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Runtime.exec_Ljava_lang_String.Runtime_exec_A01", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[com google jctf test lib java lang Runtime]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // ]>

          .put("lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A04", any())
          // 1) t01
          // java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[DexPathList[[dex file "/tmp/junit958205418142169834/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]] couldn't find "libstub.so"
          // 2) t02
          // java.lang.AssertionError

          .put("lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A05", any())
          // 1) t01
          // java.lang.AssertionError
          // 2) t02
          // java.lang.AssertionError

          .put("lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A03", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.AssertionError

          .put("lang.Runtime.execLjava_lang_String.Runtime_exec_A02", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.execLjava_lang_String.Runtime_exec_A02$T01
          // ]>

          .put("lang.Runtime.execLjava_lang_String.Runtime_exec_A03", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Runtime.loadLibraryLjava_lang_String.Runtime_loadLibrary_A02", any())
          // 1) t03
          // java.lang.Exception: Unexpected exception, expected<java.lang.UnsatisfiedLinkError> but was<java.lang.AssertionError>
          // Caused by: java.lang.AssertionError: Misconfiguration, missing property: 'jctf.library.path'

          .put("lang.Runtime.traceMethodCallsZ.Runtime_traceMethodCalls_A01", any())
          // 1) t01
          // java.lang.UnsupportedOperationException

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A08", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A08$T01
          // expected:<0> but was:<1>

          .put("lang.Runtime.execLjava_lang_String.Runtime_exec_A01", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[com google jctf test lib java lang Runtime]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // ]>

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A03", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A03$T01
          // expected:<0> but was:<1>

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A07", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A07$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A07$T02
          // expected:<0> but was:<1>

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a string containing "TERMINATED_BY_EXCEPTION"
          // but: was "Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T02
          // "
          // 3) t03
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T03
          // expected:<0> but was:<1>
          // 4) t04
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T04
          // expected:<0> but was:<1>
          // 5) t05
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T05
          // expected:<0> but was:<1>
          // 6) t06
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T06
          // expected:<0> but was:<1>
          // 7) t07
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T07
          // expected:<0> but was:<1>
          // 8) t08
          // java.lang.AssertionError:
          // Expected: a string containing "java.lang.UnknownError: Main Thread Exit"
          // but: was "Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A05$T08
          // "

          .put("lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A06", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A06$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.addShutdownHookLjava_lang_Thread.Runtime_addShutdownHook_A06$T02
          // expected:<0> but was:<1>

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_String.Runtime_exec_A03", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_String.Runtime_exec_A02", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t01]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoEnv
          // ]>
          // 2) t02
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.execLjava_lang_String_Ljava_lang_String.Runtime_exec_A02$T02
          // ]>

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_String.Runtime_exec_A01", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[com google jctf test lib java lang Runtime]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // ]>

          .put("lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A02$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A02$T02
          // expected:<0> but was:<1>

          .put("lang.Runtime.exec_Ljava_lang_String_Ljava_lang_String.Runtime_exec_A01", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[com google jctf test lib java lang Runtime]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // ]>

          .put("lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A01",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Runtime.exec_Ljava_lang_String_Ljava_lang_String.Runtime_exec_A02", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t01]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoEnv
          // ]>
          // 2) t02
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exec_Ljava_lang_String$Ljava_lang_String.Runtime_exec_A02$T02
          // ]>

          .put("lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A03",
              any())
          // 1) t03
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.removeShutdownHookLjava_lang_Thread.Runtime_removeShutdownHook_A03$T03
          // expected:<0> but was:<1>

          .put(
              "lang.Runtime.exec_Ljava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: actual=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // : array lengths differed, expected.length=8 actual.length=9

          .put("lang.Runtime.exec_Ljava_lang_String_Ljava_lang_String.Runtime_exec_A03", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put(
              "lang.Runtime.exec_Ljava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A02",
              any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t01]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoEnv
          // ]>
          // 2) t02
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exec_Ljava_lang_String$Ljava_lang_StringLjava_io_File.Runtime_exec_A02$T02
          // ]>

          .put("lang.Runtime.haltI.Runtime_halt_A02", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A02$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A02$T02
          // expected:<0> but was:<1>

          .put(
              "lang.Runtime.exec_Ljava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A03",
              any())
          // 1) t02
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Runtime.haltI.Runtime_halt_A03", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A03$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A03$T02
          // expected:<0> but was:<1>

          .put("lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T02
          // expected:<0> but was:<1>
          // 3) t03
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T03
          // expected:<0> but was:<1>
          // 4) t04
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T04
          // expected:<0> but was:<1>
          // 5) t06
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T06
          // expected:<0> but was:<1>
          // 6) t07
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T07
          // expected:<0> but was:<1>
          // 7) t08
          // java.lang.AssertionError:
          // Expected: a string containing "java.lang.UnknownError: Main Thread Exit"
          // but: was "Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A01$T08
          // "

          .put("lang.Runtime.haltI.Runtime_halt_A01", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A01$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.haltI.Runtime_halt_A01$T02
          // expected:<0> but was:<1>

          .put("lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A03", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A03$T03
          // expected:<0> but was:<1>

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A03",
              any())
          // 1) t02
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A01",
              any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[com google jctf test lib java lang Runtime]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoArgs
          // ]>

          .put("lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A02", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.runFinalizersOnExitZ.Runtime_runFinalizersOnExit_A02$T01
          // expected:<0> but was:<1>

          .put("lang.Runtime.exitI.Runtime_exit_A03", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A03$T01
          // expected:<123> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A03$T01
          // 3) t03
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A03$T03

          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A02",
              any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<[t01]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.EchoEnv
          // ]>
          // 2) t02
          // org.junit.ComparisonFailure: expected:<[t02]> but was:<[Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.execLjava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A02$T02
          // ]>

          .put("lang.Runtime.exitI.Runtime_exit_A04", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A04$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A04$T02
          // expected:<0> but was:<1>

          .put("lang.NoSuchMethodException.serialization.NoSuchMethodException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NoSuchMethodException/serialization/NoSuchMethodException_serialization_A01.golden.0.ser

          .put("lang.Runtime.exitI.Runtime_exit_A01", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A01$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A01$T02
          // expected:<0> but was:<1>
          // 3) t03
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A01$T03
          // out=
          // 4) t04
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A01$T04
          // expected:<-1> but was:<1>
          // 5) t05
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A01$T05
          // expected:<0> but was:<1>

          .put("lang.Runtime.exitI.Runtime_exit_A02", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T01
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T02
          // out=
          // 3) t03
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T03
          // out=
          // 4) t04
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T04
          // expected:<0> but was:<1>
          // 5) t05
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T05
          // out=
          // 6) t06
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T06
          // expected:<0> but was:<1>
          // 7) t07
          // java.lang.AssertionError: Process did not block but exited with code 1;
          // err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.Runtime.exitI.Runtime_exit_A02$T07
          // out=

          .put("lang.InstantiationException.serialization.InstantiationException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/InstantiationException/serialization/InstantiationException_serialization_A01.golden.0.ser

          .put("lang.Exception.serialization.Exception_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Exception/serialization/Exception_serialization_A01.golden.0.ser

          .put("lang.StackOverflowError.serialization.StackOverflowError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/StackOverflowError/serialization/StackOverflowError_serialization_A01.golden.0.ser

          .put("lang.NoSuchFieldException.serialization.NoSuchFieldException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NoSuchFieldException/serialization/NoSuchFieldException_serialization_A01.golden.0.ser

          .put(
              "lang.NegativeArraySizeException.serialization.NegativeArraySizeException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NegativeArraySizeException/serialization/NegativeArraySizeException_serialization_A01.golden.0.ser

          .put(
              "lang.ArrayIndexOutOfBoundsException.serialization.ArrayIndexOutOfBoundsException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ArrayIndexOutOfBoundsException/serialization/ArrayIndexOutOfBoundsException_serialization_A01.golden.0.ser

          .put("lang.VerifyError.serialization.VerifyError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/VerifyError/serialization/VerifyError_serialization_A01.golden.0.ser

          .put(
              "lang.IllegalArgumentException.serialization.IllegalArgumentException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalArgumentException/serialization/IllegalArgumentException_serialization_A01.golden.0.ser

          .put("lang.IllegalStateException.serialization.IllegalStateException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalStateException/serialization/IllegalStateException_serialization_A01.golden.0.ser

          .put("lang.Double.serialization.Double_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Double/serialization/Double_serialization_A01.golden.0.ser

          .put("lang.Double.toStringD.Double_toString_A05", any())
          // 1) t01
          // org.junit.ComparisonFailure: expected:<0.001[0]> but was:<0.001[]>

          .put("lang.ArithmeticException.serialization.ArithmeticException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ArithmeticException/serialization/ArithmeticException_serialization_A01.golden.0.ser

          .put(
              "lang.ExceptionInInitializerError.serialization.ExceptionInInitializerError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ExceptionInInitializerError/serialization/ExceptionInInitializerError_serialization_A01.golden.0.ser

          .put("lang.ThreadLocal.Class.ThreadLocal_class_A01", any())
          // 1) t02
          // java.lang.AssertionError: Stale thread-local value was not finalized

          .put("lang.Byte.serialization.Byte_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Byte/serialization/Byte_serialization_A01.golden.0.ser

          .put("lang.Byte.parseByteLjava_lang_StringI.Byte_parseByte_A02", any())
          // 1) t01
          // java.lang.AssertionError: Parsed Byte instance from string:+1 radix:10

          .put("lang.Byte.valueOfLjava_lang_StringI.Byte_valueOf_A02", any())
          // 1) t01
          // java.lang.AssertionError: Parsed Byte instance from string:+1 radix:10

          .put("lang.Byte.valueOfLjava_lang_String.Byte_ValueOf_A02", any())
          // 1) t02
          // java.lang.AssertionError: Parsed Byte instance from string:+1

          .put("lang.Byte.decodeLjava_lang_String.Byte_decode_A04", any())
          // 1) t01
          // java.lang.AssertionError: Decoded Byte instance from string:+1

          .put("lang.LinkageError.serialization.LinkageError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/LinkageError/serialization/LinkageError_serialization_A01.golden.0.ser

          .put("lang.ClassCastException.serialization.ClassCastException_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ClassCastException/serialization/ClassCastException_serialization_A01.golden.0.ser

          .put("lang.Byte.ConstructorLjava_lang_String.Byte_Constructor_A02", any())
          // 1) t02
          // java.lang.AssertionError: Parsed Byte instance from string:+1

          .put("lang.Byte.parseByteLjava_lang_String.Byte_parseByte_A02", any())
          // 1) t02
          // java.lang.AssertionError: Parsed Byte instance from string:+1

          .put("lang.NoSuchFieldError.serialization.NoSuchFieldError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NoSuchFieldError/serialization/NoSuchFieldError_serialization_A01.golden.0.ser

          .put(
              "lang.UnsupportedOperationException.serialization.UnsupportedOperationException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/UnsupportedOperationException/serialization/UnsupportedOperationException_serialization_A01.golden.0.ser

          .put("lang.NoSuchMethodError.serialization.NoSuchMethodError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NoSuchMethodError/serialization/NoSuchMethodError_serialization_A01.golden.0.ser

          .put(
              "lang.IllegalMonitorStateException.serialization.IllegalMonitorStateException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalMonitorStateException/serialization/IllegalMonitorStateException_serialization_A01.golden.0.ser

          .put(
              "lang.StringIndexOutOfBoundsException.serialization.StringIndexOutOfBoundsException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/StringIndexOutOfBoundsException/serialization/StringIndexOutOfBoundsException_serialization_A01.golden.0.ser

          .put("lang.SecurityException.serialization.SecurityException_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/SecurityException/serialization/SecurityException_serialization_A01.golden.0.ser

          .put("lang.IllegalAccessError.serialization.IllegalAccessError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalAccessError/serialization/IllegalAccessError_serialization_A01.golden.0.ser

          .put("lang.ArrayStoreException.serialization.ArrayStoreException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ArrayStoreException/serialization/ArrayStoreException_serialization_A01.golden.0.ser

          .put("lang.UnknownError.serialization.UnknownError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/UnknownError/serialization/UnknownError_serialization_A01.golden.0.ser

          .put("lang.Boolean.serialization.Boolean_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Boolean/serialization/Boolean_serialization_A01.golden.0.ser

          .put("lang.Integer.valueOfLjava_lang_StringI.Integer_valueOf_A02", any())
          // 1) t07
          // java.lang.AssertionError: NumberFormatException expected for input: +1 and radix: 10

          .put("lang.Integer.serialization.Integer_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Integer/serialization/Integer_serialization_A01.golden.0.ser

          .put("lang.Integer.parseIntLjava_lang_String.Integer_parseInt_A02", any())
          // 1) t06
          // java.lang.AssertionError: Expected exception: java.lang.NumberFormatException

          .put("lang.Integer.getIntegerLjava_lang_StringI.Integer_getInteger_A02", any())
          // 1) t03
          // java.lang.AssertionError: expected:<6031769> but was:<1>

          .put("lang.Integer.valueOfLjava_lang_String.Integer_valueOf_A02", any())
          // 1) t07
          // java.lang.AssertionError: NumberFormatException expected for input: +1

          .put("lang.Integer.decodeLjava_lang_String.Integer_decode_A04", any())
          // 1) t06
          // java.lang.AssertionError: Expected exception: java.lang.NumberFormatException

          .put("lang.Integer.parseIntLjava_lang_StringI.Integer_parseInt_A02", any())
          // 1) t06
          // java.lang.AssertionError: Expected exception: java.lang.NumberFormatException

          .put("lang.Integer.getIntegerLjava_lang_StringLjava_lang_Integer.Integer_getInteger_A02",
              any())
          // 1) t03
          // java.lang.AssertionError: expected:<6031769> but was:<1>

          .put("lang.Integer.ConstructorLjava_lang_String.Integer_Constructor_A02", any())
          // 1) t06
          // java.lang.AssertionError: Expected exception: java.lang.NumberFormatException

          .put("lang.Integer.getIntegerLjava_lang_String.Integer_getInteger_A02", any())
          // 1) t03
          // java.lang.AssertionError: expected null, but was:<1>

          .put("lang.ref.PhantomReference.isEnqueued.PhantomReference_isEnqueued_A01", any())
          // 1) t04
          // java.lang.AssertionError: reference is not enqueued after 2 sec

          .put("lang.ref.SoftReference.isEnqueued.SoftReference_isEnqueued_A01", any())
          // 1) t03
          // java.lang.AssertionError: reference is not enqueued after 2 sec

          .put("lang.ref.SoftReference.get.SoftReference_get_A01", any())
          // 1) t03
          // java.lang.AssertionError: expected null, but was:<[I@e2603b4>

          .put("lang.ref.ReferenceQueue.poll.ReferenceQueue_poll_A01", any())
          // 1) t03
          // java.lang.AssertionError: reference is not enqueued after 2 sec

          .put("lang.StackTraceElement.serialization.StackTraceElement_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/StackTraceElement/serialization/StackTraceElement_serialization_A01.golden.0.ser

          .put("lang.ref.WeakReference.get.WeakReference_get_A01", any())
          // 1) t03
          // java.lang.AssertionError: expected null, but was:<[I@1b32f32>

          .put("lang.ref.WeakReference.isEnqueued.WeakReference_isEnqueued_A01", any())
          // 1) t03
          // java.lang.AssertionError: reference is not enqueued after 2 sec

          .put("lang.StackTraceElement.toString.StackTraceElement_toString_A01",
              match(runtimes(DexVm.ART_DEFAULT)))
          // 1) t03
          // org.junit.ComparisonFailure: expected:<...ethod(Unknown Source[])> but was:<...ethod(Unknown Source[:1])>

          .put("lang.NullPointerException.serialization.NullPointerException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/NullPointerException/serialization/NullPointerException_serialization_A01.golden.0.ser

          .put("lang.VirtualMachineError.serialization.VirtualMachineError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/VirtualMachineError/serialization/VirtualMachineError_serialization_A01.golden.0.ser

          .put("lang.ClassCircularityError.serialization.ClassCircularityError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ClassCircularityError/serialization/ClassCircularityError_serialization_A01.golden.0.ser

          .put("lang.ThreadDeath.serialization.ThreadDeath_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ThreadDeath/serialization/ThreadDeath_serialization_A01.golden.0.ser

          .put("lang.InstantiationError.serialization.InstantiationError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/InstantiationError/serialization/InstantiationError_serialization_A01.golden.0.ser

          .put(
              "lang.IllegalThreadStateException.serialization.IllegalThreadStateException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalThreadStateException/serialization/IllegalThreadStateException_serialization_A01.golden.0.ser

          .put("lang.ProcessBuilder.environment.ProcessBuilder_environment_A05", any())
          // 1) t01
          // java.lang.AssertionError: Input Stream should not be empty

          .put("lang.ProcessBuilder.environment.ProcessBuilder_environment_A06", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ProcessBuilder.start.ProcessBuilder_start_A05", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ProcessBuilder.start.ProcessBuilder_start_A06", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ClassFormatError.serialization.ClassFormatError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ClassFormatError/serialization/ClassFormatError_serialization_A01.golden.0.ser

          .put("lang.Math.cbrtD.Math_cbrt_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0, DexVm.ART_6_0_1)))
          // 1) t01
          // java.lang.AssertionError: cbrt(27.) expected:<3.0> but was:<3.0000000000000004>

          .put("lang.Math.powDD.Math_pow_A08", any())
          // 1) t01
          // java.lang.AssertionError: expected:<NaN> but was:<1.0>
          // 2) t02
          // java.lang.AssertionError: expected:<NaN> but was:<1.0>
          // 3) t03
          // java.lang.AssertionError: expected:<NaN> but was:<1.0>
          // 4) t04
          // java.lang.AssertionError: expected:<NaN> but was:<1.0>

          .put(
              "lang.IncompatibleClassChangeError.serialization.IncompatibleClassChangeError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IncompatibleClassChangeError/serialization/IncompatibleClassChangeError_serialization_A01.golden.0.ser

          .put("lang.Float.serialization.Float_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Float/serialization/Float_serialization_A01.golden.0.ser

          .put("lang.Float.toStringF.Float_toString_A02", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<0.001[0]> but was:<0.001[]>
          // 2) t04
          // org.junit.ComparisonFailure: expected:<0.001[0]> but was:<0.001[]>

          .put("lang.Short.valueOfLjava_lang_StringI.Short_valueOf_A02", any())
          // 1) t03
          // java.lang.AssertionError: Missing NumberFormatException for radix=10

          .put("lang.Short.valueOfLjava_lang_String.Short_valueOf_A02", any())
          // 1) t03
          // java.lang.AssertionError: Missing NumberFormatException for arg="+1"

          .put("lang.Short.serialization.Short_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Short/serialization/Short_serialization_A01.golden.0.ser

          .put("lang.Short.parseShortLjava_lang_String.Short_parseShort_A02", any())
          // 1) t01
          // java.lang.AssertionError: Parsed Short instance from string:+1

          .put("lang.Short.decodeLjava_lang_String.Short_decode_A04", any())
          // 1) t01
          // java.lang.AssertionError: Decoded Short instance from string:+1

          .put("lang.Short.ConstructorLjava_lang_String.Short_Constructor_A02", any())
          // 1) t02
          // java.lang.AssertionError: Created Short instance from string:+1

          .put("lang.ClassNotFoundException.serialization.ClassNotFoundException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/ClassNotFoundException/serialization/ClassNotFoundException_serialization_A01.golden.0.ser

          .put(
              "lang.annotation.AnnotationFormatError.serialization.AnnotationFormatError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/annotation/AnnotationFormatError/serialization/AnnotationFormatError_serialization_A01.golden.0.ser

          .put("lang.Short.parseShortLjava_lang_StringI.Short_parseShort_A02", any())
          // 1) t01
          // java.lang.AssertionError: Parsed Short instance from string:+1 radix:10

          .put(
              "lang.annotation.IncompleteAnnotationException.ConstructorLjava_lang_ClassLjava_lang_String.IncompleteAnnotationException_Constructor_A01",
              match(runtimes(DexVm.ART_DEFAULT)))
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.String.toString()' on a null object reference
          // 2) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.String.toString()' on a null object reference

          .put("lang.InterruptedException.serialization.InterruptedException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/InterruptedException/serialization/InterruptedException_serialization_A01.golden.0.ser

          .put(
              "lang.annotation.IncompleteAnnotationException.Class.IncompleteAnnotationException_class_A01",
              any())
          // 1) t01
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.IncompleteAnnotationException.Class.IncompleteAnnotationClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.IncompleteAnnotationException.Class.IncompleteAnnotationClass" on path: DexPathList[[dex file "/tmp/junit6988968562481945570/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.annotation.Annotation.Class.Annotation_class_A03", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.annotation.Annotation.serialization.Annotation_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/annotation/Annotation/serialization/Annotation_serialization_A01.golden.0.ser

          .put("lang.annotation.Annotation.annotationType.Annotation_annotationType_A01", any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t03
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/annotation/Annotation/annotationType/Mark;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.annotationType.Mark" on path: DexPathList[[dex file "/tmp/junit2356208730386617024/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t04
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/annotation/Annotation/annotationType/Mark;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.annotationType.Mark" on path: DexPathList[[dex file "/tmp/junit2356208730386617024/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t05
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/annotation/Annotation/annotationType/Mark;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.annotationType.Mark" on path: DexPathList[[dex file "/tmp/junit2356208730386617024/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t06
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/annotation/Annotation/annotationType/Mark;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.annotationType.Mark" on path: DexPathList[[dex file "/tmp/junit2356208730386617024/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.annotation.IncompleteAnnotationException.serialization.IncompleteAnnotationException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/annotation/IncompleteAnnotationException/serialization/IncompleteAnnotationException_serialization_A01.golden.0.ser

          .put("lang.annotation.Annotation.Class.Annotation_class_A02", any())
          // 1) t04
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingClassValue
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingClassValue" on path: DexPathList[[dex file "/tmp/junit5702619070125761074/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t05
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingClassArrayValue
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingClassArrayValue" on path: DexPathList[[dex file "/tmp/junit5702619070125761074/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t06
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingEnumValue
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingEnumValue" on path: DexPathList[[dex file "/tmp/junit5702619070125761074/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 4) t07
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingEnumArrayValue
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.Annotation.Class.AnnotationMissingEnumArrayValue" on path: DexPathList[[dex file "/tmp/junit5702619070125761074/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 5) t08
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 6) t09
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.annotation.Retention.Retention_class_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.annotation.AnnotationTypeMismatchException.Class.AnnotationTypeMismatchException_class_A01",
              any())
          // 1) t01
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.AnnotationTypeMismatchException.Class.AnnotationTypeMismatchClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.AnnotationTypeMismatchException.Class.AnnotationTypeMismatchClass" on path: DexPathList[[dex file "/tmp/junit7410548264446836680/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.annotation.AnnotationTypeMismatchException.Class.AnnotationTypeArrayMismatchClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.annotation.AnnotationTypeMismatchException.Class.AnnotationTypeArrayMismatchClass" on path: DexPathList[[dex file "/tmp/junit7410548264446836680/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Long.serialization.Long_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Long/serialization/Long_serialization_A01.golden.0.ser

          .put("lang.ThreadGroup.resume.ThreadGroup_resume_A01", any())
          // 1) t01
          // java.lang.UnsupportedOperationException

          .put("lang.AbstractMethodError.serialization.AbstractMethodError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/AbstractMethodError/serialization/AbstractMethodError_serialization_A01.golden.0.ser

          .put("lang.RuntimeException.serialization.RuntimeException_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/RuntimeException/serialization/RuntimeException_serialization_A01.golden.0.ser

          .put("lang.ThreadGroup.suspend.ThreadGroup_suspend_A01", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException

          .put(
              "lang.ThreadGroup.ConstructorLjava_lang_ThreadGroupLjava_lang_String.ThreadGroup_Constructor_A03",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ThreadGroup.stop.ThreadGroup_stop_A01", any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t02
          // java.lang.UnsupportedOperationException

          .put("lang.ThreadGroup.enumerate_Thread.ThreadGroup_enumerate_A01", any())
          // 1) t05
          // java.lang.UnsupportedOperationException

          .put(
              "lang.ThreadGroup.ConstructorLjava_lang_ThreadGroupLjava_lang_String.ThreadGroup_Constructor_A04",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ThreadGroup.parentOfLjava_lang_ThreadGroup.ThreadGroup_parentOf_A01", any())
          // 1) t05
          // java.lang.SecurityException

          .put("lang.ThreadGroup.getMaxPriority.ThreadGroup_getMaxPriority_A02", any())
          // 1) t02
          // java.lang.AssertionError: expected:<1> but was:<5>

          .put("lang.ThreadGroup.checkAccess.ThreadGroup_checkAccess_A03", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ThreadGroup.enumerate_ThreadZ.ThreadGroup_enumerate_A01", any())
          // 1) t06
          // java.lang.UnsupportedOperationException

          .put(
              "lang.ThreadGroup.uncaughtExceptionLjava_lang_ThreadLjava_lang_Throwable.ThreadGroup_uncaughtException_A01",
              any())
          // 1) t01
          // java.lang.UnsupportedOperationException
          // 2) t01
          // java.lang.IllegalThreadStateException

          .put("lang.ThreadGroup.checkAccess.ThreadGroup_checkAccess_A02", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.ThreadGroup.ConstructorLjava_lang_String.ThreadGroup_Constructor_A04", any())
          // 1) t01
          // java.lang.AssertionError: test failed with error:java.lang.SecurityException

          .put("lang.ThreadGroup.activeCount.ThreadGroup_activeCount_A01", any())
          // 1) t04
          // java.lang.UnsupportedOperationException

          .put("lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A03", any())
          // 1) t01
          // java.lang.AssertionError: Maximum priority should not be changed. expected same:<10> was not:<1>

          .put("lang.ThreadGroup.ConstructorLjava_lang_String.ThreadGroup_Constructor_A03", any())
          // 1) t01
          // java.lang.AssertionError: test failed with error:java.lang.SecurityException

          .put("lang.ThreadGroup.getParent.ThreadGroup_getParent_A03", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Class.getDeclaredConstructors.Class_getDeclaredConstructors_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.AssertionError.serialization.AssertionError_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/AssertionError/serialization/AssertionError_serialization_A01.golden.0.ser

          .put("lang.Class.getClassLoader.Class_getClassLoader_A01", any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t03
          // java.lang.AssertionError: expected null, but was:<java.lang.BootClassLoader@d17167d>

          .put("lang.Class.getDeclaringClass.Class_getDeclaringClass_A01", any())
          // 1) t04
          // java.lang.AssertionError: expected null, but was:<class com.google.jctf.test.lib.java.lang.Class.getDeclaringClass.Class_getDeclaringClass_A01>

          .put("lang.Class.getDeclaredFields.Class_getDeclaredFields_A01", any())
          // 1) t02
          // java.lang.AssertionError: array lengths differed, expected.length=0 actual.length=2

          .put("lang.Class.getClassLoader.Class_getClassLoader_A02", any())
          // 1) t02
          // java.lang.AssertionError: ClassLoader of int[] expected null, but was:<java.lang.BootClassLoader@34f2660>

          .put("lang.Class.getClassLoader.Class_getClassLoader_A03", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.SecurityException

          .put("lang.Class.getDeclaredFields.Class_getDeclaredFields_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getResourceLjava_lang_String.Class_getResource_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getPath()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getPath()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getPath()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getPath()' on a null object reference
          // 5) t06
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getPath()' on a null object reference
          // 6) t07
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A03", any())
          // 1) t03
          // java.lang.AssertionError: Vague error message

          .put("lang.Class.forNameLjava_lang_StringZLjava_lang_ClassLoader.Class_forName_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.LinkageError

          .put("lang.Class.forNameLjava_lang_StringZLjava_lang_ClassLoader.Class_forName_A07",
              any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException

          .put("lang.Class.forNameLjava_lang_StringZLjava_lang_ClassLoader.Class_forName_A01",
              any())
          // 1) t05
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.forNameLjava_lang_StringZLjava_lang_ClassLoader.Class_forName_A01$TestFixture
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t06
          // java.lang.ClassNotFoundException: [[[Lcom.google.jctf.test.lib.java.lang.Class.forNameLjava_lang_StringZLjava_lang_ClassLoader.Class_forName_A01$TestFixture;
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.Class.getConstructor_Ljava_lang_Class.Class_getConstructor_A04", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.serialization.Class_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Class/serialization/Class_serialization_A01-Object.golden.ser

          .put("lang.Class.getMethods.Class_getMethods_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put(
              "lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A05",
              any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getClasses.Class_getClasses_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put(
              "lang.Class.getDeclaredMethodLjava_lang_String_Ljava_lang_Class.Class_getDeclaredMethod_A03",
              any())
          // 1) t05
          // java.lang.AssertionError: Vague error message

          .put("lang.Class.getClasses.Class_getClasses_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t03
          // java.lang.AssertionError: Array lengths expected:<2> but was:<3>

          .put("lang.Class.getProtectionDomain.Class_getProtectionDomain_A01", any())
          // 1) t01
          // java.lang.AssertionError: unexpected null
          // 2) t02
          // java.lang.AssertionError

          .put("lang.Class.getProtectionDomain.Class_getProtectionDomain_A02", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t03
          // java.lang.AssertionError: Array lengths expected:<1> but was:<3>

          .put("lang.Class.getMethods.Class_getMethods_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t03
          // java.lang.AssertionError: Array lengths expected:<1> but was:<3>

          .put("lang.Class.getGenericInterfaces.Class_getGenericInterfaces_A04", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.BadSignatureClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.BadSignatureClass" on path: DexPathList[[dex file "/tmp/junit3946430209802684584/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Class.getDeclaredFieldLjava_lang_String.Class_getDeclaredField_A04", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getDeclaredMethods.Class_getDeclaredMethods_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getResourceAsStreamLjava_lang_String.Class_getResourceAsStream_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.InputStream.close()' on a null object reference
          // 5) t06
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 6) t07
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.Class.getGenericInterfaces.Class_getGenericInterfaces_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.MalformedSuperinterface
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.MalformedSuperinterface" on path: DexPathList[[dex file "/tmp/junit4997784696243614791/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.MalformedSuperclass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.MalformedSuperclass" on path: DexPathList[[dex file "/tmp/junit4997784696243614791/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Class.getAnnotationLjava_lang_Class.Class_getAnnotation_A01", any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.Class.getGenericInterfaces.Class_getGenericInterfaces_A03", any())
          // 1) t01
          // java.lang.AssertionError: Should throw TypeNotPresentException for class: interface com.google.jctf.test.lib.java.lang.Class.getGenericInterfaces.Class_getGenericInterfaces_A03$I01

          .put("lang.Class.getDeclaredClasses.Class_getDeclaredClasses_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.desiredAssertionStatus.Class_desiredAssertionStatus_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 3) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 4) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 5) t05
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 6) t06
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 7) t07
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 8) t08
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.Class.getPackage.Class_getPackage_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected same:<package java.lang, Unknown, version 0.0> was not:<package java.lang, Unknown, version 0.0>
          // 2) t03
          // java.lang.AssertionError: expected null, but was:<package [Ljava.lang, Unknown, version 0.0>

          .put("lang.Class.getFieldLjava_lang_String.Class_getField_A04", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getTypeParameters.Class_getTypeParameters_A02", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.BadSignatureClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.BadSignatureClass" on path: DexPathList[[dex file "/tmp/junit8470679547599572122/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Class.getDeclaredAnnotations.Class_getDeclaredAnnotations_A01", any())
          // 1) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Class.getConstructors.Class_getConstructors_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.isAnnotationPresentLjava_lang_Class.Class_isAnnotationPresent_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t04
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.Class.getFields.Class_getFields_A02", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getGenericSuperclass.Class_getGenericSuperclass_A03", any())
          // 1) t01
          // java.lang.AssertionError: Should throw TypeNotPresentException for class: class com.google.jctf.test.lib.java.lang.Class.getGenericSuperclass.Class_getGenericSuperclass_A03$C01

          .put("lang.Class.getGenericSuperclass.Class_getGenericSuperclass_A04", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.MalformedSuperclass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.MalformedSuperclass" on path: DexPathList[[dex file "/tmp/junit552939904349045383/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.MalformedSuperinterface
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.MalformedSuperinterface" on path: DexPathList[[dex file "/tmp/junit552939904349045383/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Class.getSigners.Class_getSigners_A01", any())
          // 1) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'java.security.CodeSource java.security.ProtectionDomain.getCodeSource()' on a null object reference
          // 2) t04
          // java.lang.AssertionError: Unable to configure default providers
          // 3) t05
          // java.lang.NoClassDefFoundError: sun.security.jca.Providers
          // Caused by: java.lang.AssertionError: Unable to configure default providers

          .put("lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A01",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) t04
          // java.lang.AssertionError: expected:<interface com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A01$I1> but was:<interface com.google.jctf.test.lib.java.lang.Class.getMethodLjava_lang_String$Ljava_lang_Class.Class_getMethod_A01$I2>

          .put("lang.Class.getGenericSuperclass.Class_getGenericSuperclass_A01", any())
          // 1) t03
          // java.lang.AssertionError: expected same:<class java.lang.reflect.AccessibleObject> was not:<class java.lang.reflect.Executable>

          .put("lang.Class.getGenericSuperclass.Class_getGenericSuperclass_A02", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.Class.BadSignatureClass
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.Class.BadSignatureClass" on path: DexPathList[[dex file "/tmp/junit6101943207514034648/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.Class.newInstance.Class_newInstance_A07", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put(
              "lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A02",
              any())
          // 1) t03
          // java.lang.AssertionError: Vague error message

          .put("lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A05", any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.forNameLjava_lang_String.Class_forName_A01", any())
          // 1) t05
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t06
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.Class.getDeclaredConstructor_Ljava_lang_Class.Class_getDeclaredConstructor_A03",
              any())
          // 1) t01
          // java.lang.SecurityException
          // 2) t03
          // java.lang.SecurityException
          // 3) t04
          // java.lang.SecurityException

          .put("lang.Class.getMethodLjava_lang_String_Ljava_lang_Class.Class_getMethod_A03", any())
          // 1) t03
          // java.lang.AssertionError: Vague error message

          .put("lang.Class.forNameLjava_lang_String.Class_forName_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.LinkageError

          .put("lang.UnsatisfiedLinkError.serialization.UnsatisfiedLinkError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/UnsatisfiedLinkError/serialization/UnsatisfiedLinkError_serialization_A01.golden.0.ser

          .put("lang.Class.getAnnotations.Class_getAnnotations_A01", any())
          // 1) t04
          // java.lang.AssertionError: expected:<0> but was:<1>
          // 2) t06
          // java.lang.AssertionError: Misconfigured test

          .put(
              "lang.EnumConstantNotPresentException.serialization.EnumConstantNotPresentException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/EnumConstantNotPresentException/serialization/EnumConstantNotPresentException_serialization_A01.golden.0.ser

          .put("lang.String.toLowerCase.String_toLowerCase_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<i[]> but was:<i[̇]>

          .put("lang.String.splitLjava_lang_StringI.String_split_A01", any())
          // 1) t06
          // java.lang.AssertionError: array lengths differed, expected.length=1 actual.length=2

          .put("lang.String.serialization.String_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/String/serialization/String_serialization_A01.golden.0.ser

          .put("lang.String.regionMatchesZILjava_lang_StringII.String_regionMatches_A01", any())
          // 1) t07
          // java.lang.AssertionError

          .put("lang.String.valueOfF.String_valueOf_A01", any())
          // 1) t09
          // org.junit.ComparisonFailure: Incorrect double string returned expected:<0.001[0]> but was:<0.001[]>

          .put("lang.String.Constructor_BLjava_nio_charset_Charset.String_Constructor_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<�[]> but was:<�[�]>
          // 2) t03
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>
          // 3) t04
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>
          // 4) t05
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>

          .put("lang.String.concatLjava_lang_String.String_concat_A01", any())
          // 1) t02
          // java.lang.AssertionError: expected not same

          .put("lang.String.matchesLjava_lang_String.String_matches_A01", any())
          // 1) t15
          // java.lang.AssertionError: pattern: [^a-d[^m-p]]*abb input: admpabb
          // 2) t19
          // java.util.regex.PatternSyntaxException: Syntax error in regexp pattern near index 11
          // .*(?<=abc)*\.log$
          // ^

          .put("lang.String.CASE_INSENSITIVE_ORDER.serialization.String_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/String/CASE_INSENSITIVE_ORDER/serialization/String_serialization_A01.golden.0.ser

          .put("lang.String.getBytesLjava_lang_String.String_getBytes_A14", any())
          // 1) t07
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 2) t12
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 3) t22
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 4) t32
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 5) t42
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 6) t52
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 7) t62
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>

          .put("lang.String.splitLjava_lang_String.String_split_A01", any())
          // 1) t06
          // java.lang.AssertionError: array lengths differed, expected.length=1 actual.length=2

          .put("lang.String.getBytesII_BI.String_getBytes_A03", any())
          // 1) t04
          // java.lang.AssertionError: Should throws IndexOutOfBoundsException: 0
          // 2) t05
          // java.lang.AssertionError: Should throws IndexOutOfBoundsException: 0

          .put("lang.String.getBytesII_BI.String_getBytes_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.String.toLowerCaseLjava_util_Locale.String_toLowerCase_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<i[]> but was:<i[̇]>

          .put("lang.String.Constructor_BIILjava_nio_charset_Charset.String_Constructor_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<�[]> but was:<�[�]>
          // 2) t03
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>
          // 3) t04
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>
          // 4) t05
          // org.junit.ComparisonFailure: expected:<[�]> but was:<[�]>

          .put("lang.String.getBytesLjava_nio_charset_Charset.String_getBytes_A01", any())
          // 1) t05
          // arrays first differed at element [0]; expected:<-40> but was:<-37>
          // Caused by: java.lang.AssertionError: expected:<-40> but was:<-37>

          .put("lang.String.valueOfD.String_valueOf_A01", any())
          // 1) t09
          // org.junit.ComparisonFailure: Incorrect double string returned expected:<0.001[0]> but was:<0.001[]>

          .put("lang.String.getBytesLjava_nio_charset_Charset.String_getBytes_A14", any())
          // 1) t07
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 2) t12
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 3) t22
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 4) t32
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 5) t42
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 6) t52
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>
          // 7) t62
          // arrays first differed at element [0]; expected:<-2> but was:<-1>
          // Caused by: java.lang.AssertionError: expected:<-2> but was:<-1>

          .put("lang.Package.isSealed.Package_isSealed_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getSpecificationVersion.Package_getSpecificationVersion_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getAnnotationLjava_lang_Class.Package_getAnnotation_A01", any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 3) t05
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.isAnnotationPresentLjava_lang_Class.Package_isAnnotationPresent_A02",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) testIsAnnotationPresent_Null2
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getName.Package_getName_A01", any())
          // 1) t03
          // java.lang.ClassNotFoundException: com.simpleClass1
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getImplementationVersion.Package_getImplementationVersion_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getDeclaredAnnotations.Package_getDeclaredAnnotations_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference
          // 2) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.Package.getSpecificationVendor.Package_getSpecificationVendor_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getAnnotationLjava_lang_Class.Package_getAnnotation_A02",
              match(runtimes(DexVm.ART_DEFAULT, DexVm.ART_7_0_0)))
          // 1) testGetAnnotation_Null2
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.isCompatibleWithLjava_lang_String.Package_isCompatibleWith_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.toString.Package_toString_A01", any())
          // 1) t01
          // java.lang.AssertionError: expect: package com.google.jctf.test.lib.java.lang.Package.toString, Unknown, version 0.0, actual: package com.google.jctf.test.lib.java.lang.Package.toString
          // 2) t03
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getAnnotations.Package_getAnnotations_A01", any())
          // 1) t03
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference
          // 2) t04
          // java.lang.AssertionError: expected:<0> but was:<1>
          // 3) t06
          // java.lang.AssertionError: Misconfigured test

          .put("lang.Package.isAnnotationPresentLjava_lang_Class.Package_isAnnotationPresent_A01",
              any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 3) t04
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.Package.getSpecificationTitle.Package_getSpecificationTitle_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getImplementationTitle.Package_getImplementationTitle_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getPackages.Package_getPackages_A01", any())
          // 1) t01
          // java.lang.AssertionError: Package getPackages failed to retrieve a packages

          .put("lang.Package.hashCode.Package_hashCode_A01", any())
          // 1) t03
          // java.lang.ClassNotFoundException: com.simpleClass1
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.getPackageLjava_lang_String.Package_getPackage_A01", any())
          // 1) t01
          // java.lang.AssertionError: Package getPackage failed for java.lang expected same:<package java.lang, Unknown, version 0.0> was not:<package java.lang, Unknown, version 0.0>

          .put("lang.Package.getImplementationVendor.Package_getImplementationVendor_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.Package.isSealedLjava_net_URL.Package_isSealed_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.simpleClass
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.net.URL.getProtocol()' on a null object reference

          .put("lang.StringBuilder.serialization.StringBuilder_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/StringBuilder/serialization/StringBuilder_serialization_A01.golden.ser

          .put(
              "lang.SecurityManager.checkReadLjava_io_FileDescriptor.SecurityManager_checkRead_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkAwtEventQueueAccess.SecurityManager_checkAwtEventQueueAccess_A01",
              any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit5851789677967468571/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit5851789677967468571/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.SecurityManager.checkWriteLjava_lang_String.SecurityManager_checkWrite_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.inClassLoader.SecurityManager_inClassLoader_A01", any())
          // 1) t01
          // java.lang.AssertionError

          .put(
              "lang.SecurityManager.checkPermissionLjava_security_PermissionLjava_lang_Object.SecurityManager_checkPermission_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkReadLjava_io_FileDescriptor.SecurityManager_checkRead_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@cf3e8f1>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.inCheck.SecurityManager_inCheck_A01", any())
          // 1) t02
          // java.lang.AssertionError: inCheck field must always remain false
          // 2) t03
          // java.lang.AssertionError: inCheck field must always remain false

          .put("lang.SecurityManager.currentClassLoader.SecurityManager_currentClassLoader_A02",
              any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.SecurityManager.checkPrintJobAccess.SecurityManager_checkPrintJobAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@a49048c>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkWriteLjava_lang_String.SecurityManager_checkWrite_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@9656315>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkPackageAccessLjava_lang_String.SecurityManager_checkPackageAccess_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkAcceptLjava_lang_StringI.SecurityManager_checkAccept_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPermissionLjava_security_PermissionLjava_lang_Object.SecurityManager_checkPermission_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <com.google.jctf.test.lib.java.lang.SecurityManager.checkPermissionLjava_security_PermissionLjava_lang_Object.SecurityManager_checkPermission_A01$1@5d6dd39>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException
          // 3) t03
          // java.lang.AssertionError: SecurityException should be thrown for null context

          .put("lang.SecurityManager.currentClassLoader.SecurityManager_currentClassLoader_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.SecurityManager.checkMulticastLjava_net_InetAddress.SecurityManager_checkMulticast_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkListenI.SecurityManager_checkListen_A01", any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@32bf8d4>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.getSecurityContext.SecurityManager_getSecurityContext_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected:<java.security.AccessControlContext@248575d> but was:<null>
          // 2) t02
          // java.lang.AssertionError: expected:<java.security.AccessControlContext@259b8d2> but was:<null>

          .put(
              "lang.SecurityManager.checkPackageAccessLjava_lang_String.SecurityManager_checkPackageAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@3230dd4>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@a1bab7d>
          // but:
          // 3) t03
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@bb64a72>
          // but:
          // 4) t05
          // java.lang.AssertionError: SecurityException should be thrown for restricted package: 1234

          .put(
              "lang.SecurityManager.checkMemberAccessLjava_lang_ClassI.SecurityManager_checkMemberAccess_A02",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@81146f>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkMulticastLjava_net_InetAddressB.SecurityManager_checkMulticast_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkAcceptLjava_lang_StringI.SecurityManager_checkAccept_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@eb9d181>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@db9b226>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkMulticastLjava_net_InetAddress.SecurityManager_checkMulticast_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@dd6300a>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@891b07b>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.Constructor.SecurityManager_Constructor_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.SecurityManager.getClassContext.SecurityManager_getClassContext_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to read from null array

          .put(
              "lang.SecurityManager.checkMemberAccessLjava_lang_ClassI.SecurityManager_checkMemberAccess_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkDeleteLjava_lang_String.SecurityManager_checkDelete_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@96408b7>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkReadLjava_lang_StringLjava_lang_Object.SecurityManager_checkRead_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkMulticastLjava_net_InetAddressB.SecurityManager_checkMulticast_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@79cc5c9>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@479a4ce>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkListenI.SecurityManager_checkListen_A02", any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@6b92452>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkAccessLjava_lang_Thread.SecurityManager_checkAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@5d582db>
          // but:
          // 2) t02
          // java.lang.AssertionError: SecurityException should be thrown

          .put(
              "lang.SecurityManager.checkWriteLjava_io_FileDescriptor.SecurityManager_checkWrite_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkDeleteLjava_lang_String.SecurityManager_checkDelete_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPropertiesAccess.SecurityManager_checkPropertiesAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.util.PropertyPermission@32a9e76>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkReadLjava_lang_StringLjava_lang_Object.SecurityManager_checkRead_A02",
              any())
          // 1) t03
          // java.lang.AssertionError: SecurityException should be thrown for null context

          .put(
              "lang.SecurityManager.checkAccessLjava_lang_ThreadGroup.SecurityManager_checkAccess_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkAccessLjava_lang_Thread.SecurityManager_checkAccess_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPackageDefinitionLjava_lang_String.SecurityManager_checkPackageDefinition_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkReadLjava_lang_String.SecurityManager_checkRead_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkWriteLjava_io_FileDescriptor.SecurityManager_checkWrite_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@cf13435>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkReadLjava_lang_StringLjava_lang_Object.SecurityManager_checkRead_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@c0d92be>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkExecLjava_lang_String.SecurityManager_checkExec_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPackageDefinitionLjava_lang_String.SecurityManager_checkPackageDefinition_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@8bc5330>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@a83ba9>
          // but:
          // 3) t03
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@785152e>
          // but:
          // 4) t05
          // java.lang.AssertionError: SecurityException should be thrown for restricted package: 1234

          .put("lang.SecurityManager.checkExecLjava_lang_String.SecurityManager_checkExec_A02",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@79b6b6b>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkCreateClassLoader.SecurityManager_checkCreateClassLoader_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@6b7c9f4>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkReadLjava_lang_String.SecurityManager_checkRead_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@5d4287d>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkAccessLjava_lang_ThreadGroup.SecurityManager_checkAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@4f08706>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.inClassLjava_lang_String.SecurityManager_inClass_A03", any())
          // 1) t01
          // java.lang.AssertionError

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringILjava_lang_Object.SecurityManager_checkConnect_A03",
              any())
          // 1) t03
          // java.lang.AssertionError: SecurityException should be thrown for null context

          .put("lang.SecurityManager.checkExecLjava_lang_String.SecurityManager_checkExec_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.io.FilePermission@162012a>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkSetFactory.SecurityManager_checkSetFactory_A01", any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@162012a>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringILjava_lang_Object.SecurityManager_checkConnect_A04",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPermissionLjava_security_Permission.SecurityManager_checkPermission_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <com.google.jctf.test.lib.java.lang.SecurityManager.checkPermissionLjava_security_Permission.SecurityManager_checkPermission_A01$2@f9abe3c>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.inClassLjava_lang_String.SecurityManager_inClass_A01", any())
          // 1) t01
          // java.lang.AssertionError: Method from class: com.google.jctf.test.lib.java.lang.SecurityManager.inClassLjava_lang_String.SecurityManager_inClass_A01 must be on execution stack.

          .put("lang.SecurityManager.inClassLjava_lang_String.SecurityManager_inClass_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPropertyAccessLjava_lang_String.SecurityManager_checkPropertyAccess_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.checkExitI.SecurityManager_checkExit_A01", any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@c0c3860>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringILjava_lang_Object.SecurityManager_checkConnect_A02",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@b2896e9>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@f796f6e>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringILjava_lang_Object.SecurityManager_checkConnect_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@96153fb>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@5296c18>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.classLoaderDepth.SecurityManager_classLoaderDepth_A02", any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.SecurityManager.classDepthLjava_lang_String.SecurityManager_classDepth_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkPropertyAccessLjava_lang_String.SecurityManager_checkPropertyAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.util.PropertyPermission@79a110d>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkPropertyAccessLjava_lang_String.SecurityManager_checkPropertyAccess_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringI.SecurityManager_checkConnect_A02",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@4ef2ca8>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@b6163c1>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.SecurityManager.checkLinkLjava_lang_String.SecurityManager_checkLink_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.classLoaderDepth.SecurityManager_classLoaderDepth_A01", any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.SecurityManager.checkPermissionLjava_security_Permission.SecurityManager_checkPermission_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.currentLoadedClass.SecurityManager_currentLoadedClass_A01",
              any())
          // 1) t01
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.SecurityManager.checkSecurityAccessLjava_lang_String.SecurityManager_checkSecurityAccess_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringI.SecurityManager_checkConnect_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.SecurityManager.checkConnectLjava_lang_StringI.SecurityManager_checkConnect_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@f9963de>
          // but:
          // 2) t02
          // java.lang.AssertionError:
          // Expected: a collection containing <java.net.SocketPermission@c7159bf>
          // but:
          // 3) t03
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put(
              "lang.SecurityManager.checkTopLevelWindowLjava_lang_Object.SecurityManager_checkTopLevelWindow_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.SecurityManager.currentLoadedClass.SecurityManager_currentLoadedClass_A02",
              any())
          // 1) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.SecurityManager.classDepthLjava_lang_String.SecurityManager_classDepth_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected:<1> but was:<-1>

          .put(
              "lang.SecurityManager.checkSecurityAccessLjava_lang_String.SecurityManager_checkSecurityAccess_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.security.SecurityPermission@a439b14>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.Throwable.serialization.Throwable_serialization_A01", any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Throwable/serialization/Throwable_serialization_A01.golden.0.ser

          .put(
              "lang.SecurityManager.checkTopLevelWindowLjava_lang_Object.SecurityManager_checkTopLevelWindow_A01",
              any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit9085263025235375443/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit9085263025235375443/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.SecurityManager.checkLinkLjava_lang_String.SecurityManager_checkLink_A01",
              any())
          // 1) t01
          // java.lang.AssertionError:
          // Expected: a collection containing <java.lang.RuntimePermission@87c5826>
          // but:
          // 2) t02
          // java.lang.AssertionError: Expected exception: java.lang.SecurityException

          .put("lang.Throwable.getStackTrace.Throwable_getStackTrace_A01", any())
          // 1) t04
          // org.junit.ComparisonFailure: wrongly omit constructor frame for other exception expected:<[<init>]> but was:<[t04]>

          .put(
              "lang.SecurityManager.checkSystemClipboardAccess.SecurityManager_checkSystemClipboardAccess_A01",
              any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit529120552959989860/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.SecurityException> but was<java.lang.NoClassDefFoundError>
          // Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/awt/AWTPermission;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "java.awt.AWTPermission" on path: DexPathList[[dex file "/tmp/junit529120552959989860/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.SecurityManager.checkSecurityAccessLjava_lang_String.SecurityManager_checkSecurityAccess_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_String.ReflectPermission_Constructor_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.reflect.MalformedParameterizedTypeException.serialization.MalformedParameterizedTypeException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/reflect/MalformedParameterizedTypeException/serialization/MalformedParameterizedTypeException_serialization_A01.golden.0.ser

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_StringLjava_lang_String.ReflectPermission_Constructor_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put(
              "lang.UnsupportedClassVersionError.serialization.UnsupportedClassVersionError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/UnsupportedClassVersionError/serialization/UnsupportedClassVersionError_serialization_A01.golden.0.ser

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_String.ReflectPermission_Constructor_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Incorrect permission constructed
          // 2) t02
          // java.lang.AssertionError: expected:<a> but was:<null>
          // 3) t03
          // java.lang.AssertionError: expected:<2/3/2> but was:<null>

          .put("lang.reflect.ReflectPermission.Class.ReflectPermission_class_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.reflect.Proxy.serialization.Proxy_serialization_A01", any())
          // 1) t01
          // java.lang.RuntimeException: java.lang.reflect.InvocationTargetException
          // Caused by: java.lang.reflect.InvocationTargetException
          // Caused by: java.lang.NullPointerException
          // 2) t02
          // java.lang.RuntimeException: java.lang.reflect.InvocationTargetException
          // Caused by: java.lang.reflect.InvocationTargetException
          // Caused by: java.lang.NullPointerException

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_StringLjava_lang_String.ReflectPermission_Constructor_A03",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_String.ReflectPermission_Constructor_A02",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.NullPointerException

          .put("lang.reflect.ReflectPermission.Class.ReflectPermission_class_A02", any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "lang.reflect.ReflectPermission.Constructor_java_lang_StringLjava_lang_String.ReflectPermission_Constructor_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Incorrect permission constructed
          // 2) t04
          // java.lang.AssertionError: expected:<a> but was:<null>
          // 3) t05
          // java.lang.AssertionError: expected:<2/3/2> but was:<null>

          .put(
              "lang.reflect.Proxy.getInvocationHandlerLjava_lang_Object.Proxy_getInvocationHandler_A02",
              any())
          // 1) t02
          // java.lang.Exception: Unexpected exception, expected<java.lang.IllegalArgumentException> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException

          .put("lang.reflect.Proxy.Class.Proxy_class_A01", any())
          // 1) t04
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t05
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.reflect.Proxy.getProxyClassLjava_lang_ClassLoader_Ljava_lang_Class.Proxy_getProxyClass_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected same:<null> was not:<java.lang.BootClassLoader@ecc20b9>
          // 2) t04
          // java.lang.AssertionError: expected same:<null> was not:<java.lang.BootClassLoader@ecc20b9>

          .put(
              "lang.reflect.Proxy.getProxyClassLjava_lang_ClassLoader_Ljava_lang_Class.Proxy_getProxyClass_A03",
              any())
          // 1) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t05
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Proxy.h.Proxy_h_A01", match(runtimes(DexVm.ART_DEFAULT)))
          // 1) t01
          // java.lang.reflect.InvocationTargetException
          // Caused by: java.lang.NullPointerException

          .put("lang.reflect.Proxy.serialization.Proxy_serialization_A02", any())
          // 1) t01
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/reflect/Proxy/serialization/Proxy_serialization_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Proxy.serialization.Proxy_serialization_A01" on path: DexPathList[[dex file "/tmp/junit3110030363172925878/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t02
          // java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/jctf/test/lib/java/lang/reflect/Proxy/serialization/Proxy_serialization_A01;
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Proxy.serialization.Proxy_serialization_A01" on path: DexPathList[[dex file "/tmp/junit3110030363172925878/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 3) t03
          // java.lang.AssertionError: Unable to configure default providers

          .put(
              "lang.reflect.GenericSignatureFormatError.serialization.GenericSignatureFormatError_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/reflect/GenericSignatureFormatError/serialization/GenericSignatureFormatError_serialization_A01.golden.0.ser

          .put(
              "lang.reflect.Proxy.newProxyInstanceLjava_lang_ClassLoader_Ljava_lang_ClassLjava_lang_reflect_InvocationHandler.Proxy_newProxyInstance_A02",
              any())
          // 1) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put(
              "lang.reflect.Proxy.ConstructorLjava_lang_reflect_InvocationHandler.Proxy_Constructor_A01",
              match(runtimes(DexVm.ART_DEFAULT)))
          // 1) t01
          // java.lang.NullPointerException

          .put(
              "lang.reflect.Proxy.newProxyInstanceLjava_lang_ClassLoader_Ljava_lang_ClassLjava_lang_reflect_InvocationHandler.Proxy_newProxyInstance_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Bad classloader expected:<null> but was:<java.lang.BootClassLoader@fda9ca7>

          .put("lang.reflect.Modifier.isStrictI.Modifier_isStrict_A01", any())
          // 1) t05
          // java.lang.AssertionError

          .put("lang.reflect.Method.getGenericReturnType.Method_getGenericReturnType_A03", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MissingReturnTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MissingReturnTypeMethod" on path: DexPathList[[dex file "/tmp/junit7196800508165091862/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getGenericReturnType.Method_getGenericReturnType_A02", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod" on path: DexPathList[[dex file "/tmp/junit1047440880764311474/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getAnnotationLjava_lang_Class.Method_getAnnotation_A01", any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A02",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod" on path: DexPathList[[dex file "/tmp/junit2055893562046815723/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.isBridge.Method_isBridge_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.BridgeTestMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.BridgeTestMethod" on path: DexPathList[[dex file "/tmp/junit1244663784930832031/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.isSynthetic.Method_isSynthetic_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.SyntheticTestMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.SyntheticTestMethod" on path: DexPathList[[dex file "/tmp/junit5876026561576323251/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getGenericReturnType.Method_getGenericReturnType_A04", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MalformedReturnTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MalformedReturnTypeMethod" on path: DexPathList[[dex file "/tmp/junit4310478819215974904/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A01",
              any())
          // 1) t03
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A01$Third
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A01$Third" on path: DexPathList[[dex file "/tmp/junit8600081041276641493/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t04
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A01$Fourth
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A01$Fourth" on path: DexPathList[[dex file "/tmp/junit8600081041276641493/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.invokeLjava_lang_Object_Ljava_lang_Object.Method_invoke_A07",
              any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalAccessException

          .put("lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A04",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MalformedExceptionTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MalformedExceptionTypeMethod" on path: DexPathList[[dex file "/tmp/junit1512043528789417983/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getTypeParameters.Method_getTypeParameters_A02", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod" on path: DexPathList[[dex file "/tmp/junit2853449662835100192/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getGenericExceptionTypes.Method_getGenericExceptionTypes_A03",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MissingExceptionTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MissingExceptionTypeMethod" on path: DexPathList[[dex file "/tmp/junit1347702687417623444/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.getDeclaredAnnotations.Method_getDeclaredAnnotations_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.reflect.Method.getGenericParameterTypes.Method_getGenericParameterTypes_A04",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MalformedParameterTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MalformedParameterTypeMethod" on path: DexPathList[[dex file "/tmp/junit2056931399679564203/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Method.toGenericString.Method_toGenericString_A01", any())
          // 1) t03
          // org.junit.ComparisonFailure: expected:<public static final [synchronized ]native void com.goog...> but was:<public static final []native void com.goog...>
          // 2) t04
          // org.junit.ComparisonFailure: expected:<..._toGenericString_A01[.com.google.jctf.test.lib.java.lang.reflect.Method.toGenericString.Method_toGenericString_A01]$GenericClass<java.l...> but was:<..._toGenericString_A01[]$GenericClass<java.l...>
          // 3) t05
          // org.junit.ComparisonFailure: expected:<..._toGenericString_A01[.com.google.jctf.test.lib.java.lang.reflect.Method.toGenericString.Method_toGenericString_A01]$GenericClass<java.l...> but was:<..._toGenericString_A01[]$GenericClass<java.l...>

          .put("lang.reflect.Method.getGenericParameterTypes.Method_getGenericParameterTypes_A03",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.MissingParameterTypeMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.MissingParameterTypeMethod" on path: DexPathList[[dex file "/tmp/junit3534060116722105133/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.InvocationHandler.invokeLjava_lang_ObjectLjava_lang_reflect_Method_Ljava_lang_Object.InvocationHandler_invoke_A02",
              any())
          // 1) t04
          // java.lang.AssertionError: ClassCastException should be thrown

          .put("lang.reflect.Method.getDefaultValue.Method_getDefaultValue_A02", any())
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.TypeNotPresentException

          .put("lang.reflect.Method.toString.Method_toString_A01", any())
          // 1) t04
          // org.junit.ComparisonFailure: expected:<public static final [synchronized ]native void com.goog...> but was:<public static final []native void com.goog...>

          .put("lang.reflect.Method.getGenericParameterTypes.Method_getGenericParameterTypes_A02",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Method.BadSignatureMethod" on path: DexPathList[[dex file "/tmp/junit7973288126499824876/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Field.getFloatLjava_lang_Object.Field_getFloat_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getDeclaringClass.Field_getDeclaringClass_A01", any())
          // 1) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getByteLjava_lang_Object.Field_getByte_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getCharLjava_lang_Object.Field_getChar_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getBooleanLjava_lang_Object.Field_getBoolean_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setByteLjava_lang_ObjectB.Field_setByte_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setByteLjava_lang_ObjectB.Field_setByte_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: bytePublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@b1b0f3d

          .put("lang.reflect.Field.setBooleanLjava_lang_ObjectZ.Field_setBoolean_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: booleanPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@953cc4f

          .put("lang.reflect.Field.setCharLjava_lang_ObjectC.Field_setChar_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.isSynthetic.Field_isSynthetic_A01", any())
          // 1) t01
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Field.TestSyntheticField
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Field.TestSyntheticField" on path: DexPathList[[dex file "/tmp/junit8256784459468391222/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Field.setBooleanLjava_lang_ObjectZ.Field_setBoolean_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getType.Field_getType_A01", any())
          // 1) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setCharLjava_lang_ObjectC.Field_setChar_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: charPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@95271f1

          .put("lang.reflect.Field.getDoubleLjava_lang_Object.Field_getDouble_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setFloatLjava_lang_ObjectF.Field_setFloat_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: floatPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@3fca927

          .put("lang.reflect.Field.getAnnotationLjava_lang_Class.Field_getAnnotation_A01", any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.reflect.Field.getIntLjava_lang_Object.Field_getInt_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setFloatLjava_lang_ObjectF.Field_setFloat_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getShortLjava_lang_Object.Field_getShort_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getGenericType.Field_getGenericType_A03", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Field.TestMissingTypeField
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Field.TestMissingTypeField" on path: DexPathList[[dex file "/tmp/junit1097443728550054537/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Field.getDeclaredAnnotations.Field_getDeclaredAnnotations_A01", any())
          // 1) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put("lang.reflect.Field.getGenericType.Field_getGenericType_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<...Field.TestOtherField[.com.google.jctf.test.lib.java.lang.reflect.Field.TestOtherField]$SomeClass<?>> but was:<...Field.TestOtherField[]$SomeClass<?>>
          // 2) t03
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setIntLjava_lang_ObjectI.Field_setInt_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getGenericType.Field_getGenericType_A02", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Field.TestBadSignatureField
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Field.TestBadSignatureField" on path: DexPathList[[dex file "/tmp/junit8638189152422058286/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Field.toGenericString.Field_toGenericString_A01", any())
          // 1) t02
          // org.junit.ComparisonFailure: expected:<...Field.TestOtherField[.com.google.jctf.test.lib.java.lang.reflect.Field.TestOtherField]$SomeClass<?> com.go...> but was:<...Field.TestOtherField[]$SomeClass<?> com.go...>

          .put("lang.reflect.Field.getGenericType.Field_getGenericType_A04", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Field.TestMalformedTypeField
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Field.TestMalformedTypeField" on path: DexPathList[[dex file "/tmp/junit1860681606366685174/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.Field.setIntLjava_lang_ObjectI.Field_setInt_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: intPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@94fbd35

          .put("lang.reflect.Field.setDoubleLjava_lang_ObjectD.Field_setDouble_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setShortLjava_lang_ObjectS.Field_setShort_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: shortPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@7887a47

          .put("lang.reflect.Field.setLongLjava_lang_ObjectJ.Field_setLong_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setLongLjava_lang_ObjectJ.Field_setLong_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: longPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@5c13759

          .put("lang.reflect.Field.setDoubleLjava_lang_ObjectD.Field_setDouble_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: doublePublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@4dd95e2

          .put("lang.reflect.Field.setShortLjava_lang_ObjectS.Field_setShort_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getLjava_lang_Object.Field_get_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.getLongLjava_lang_Object.Field_getLong_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setLjava_lang_ObjectLjava_lang_Object.Field_set_A05", any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.ExceptionInInitializerError> but was<java.lang.NullPointerException>
          // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference
          // 2) t02
          // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.io.InputStream.available()' on a null object reference

          .put("lang.reflect.Field.setLjava_lang_ObjectLjava_lang_Object.Field_set_A01", any())
          // 1) t02
          // java.lang.AssertionError: Exception is not thrown: field: shortPublicField, object: com.google.jctf.test.lib.java.lang.reflect.Field.TestStaticFinalPrimitiveField@bf7ecde

          .put("lang.reflect.Constructor.newInstance_Ljava_lang_Object.Constructor_newInstance_A06",
              any())
          // 1) t05
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put("lang.reflect.Constructor.isSynthetic.Constructor_isSynthetic_A01", any())
          // 1) t02
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.SyntheticConstructorTestData
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.SyntheticConstructorTestData" on path: DexPathList[[dex file "/tmp/junit1480674965150331230/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A03",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.MissingExceptionTypeConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.MissingExceptionTypeConstructor" on path: DexPathList[[dex file "/tmp/junit4009528913069484861/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A02",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor" on path: DexPathList[[dex file "/tmp/junit1321049867835327167/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A01",
              any())
          // 1) t03
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A01$Third
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A01$Third" on path: DexPathList[[dex file "/tmp/junit8133550864036959380/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]
          // 2) t04
          // java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A01$Fourth
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A01$Fourth" on path: DexPathList[[dex file "/tmp/junit8133550864036959380/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getAnnotationLjava_lang_Class.Constructor_getAnnotation_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put(
              "lang.reflect.Constructor.getDeclaredAnnotations.Constructor_getDeclaredAnnotations_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put(
              "lang.reflect.Constructor.getGenericExceptionTypes.Constructor_getGenericExceptionTypes_A04",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.MalformedExceptionTypeConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.MalformedExceptionTypeConstructor" on path: DexPathList[[dex file "/tmp/junit376255323471566097/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.InvocationTargetException.serialization.InvocationTargetException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/reflect/InvocationTargetException/serialization/InvocationTargetException_serialization_A01.golden.0.ser

          .put("lang.reflect.Constructor.toGenericString.Constructor_toGenericString_A01", any())
          // 1) t04
          // org.junit.ComparisonFailure: expected:<..._toGenericString_A01[.com.google.jctf.test.lib.java.lang.reflect.Constructor.toGenericString.Constructor_toGenericString_A01]$GenericClass<java.l...> but was:<..._toGenericString_A01[]$GenericClass<java.l...>

          .put("lang.reflect.Constructor.getTypeParameters.Constructor_getTypeParameters_A02",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor" on path: DexPathList[[dex file "/tmp/junit7135581864552916266/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericParameterTypes.Constructor_getGenericParameterTypes_A03",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.TypeNotPresentException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.MissingParameterTypeConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.MissingParameterTypeConstructor" on path: DexPathList[[dex file "/tmp/junit1307676357171999053/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericParameterTypes.Constructor_getGenericParameterTypes_A04",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.MalformedParameterizedTypeException> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.MalformedParameterTypeConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.MalformedParameterTypeConstructor" on path: DexPathList[[dex file "/tmp/junit4591001470670613975/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put(
              "lang.reflect.Constructor.getGenericParameterTypes.Constructor_getGenericParameterTypes_A02",
              any())
          // 1) t01
          // java.lang.Exception: Unexpected exception, expected<java.lang.reflect.GenericSignatureFormatError> but was<java.lang.ClassNotFoundException>
          // Caused by: java.lang.ClassNotFoundException: com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor
          // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.google.jctf.test.lib.java.lang.reflect.Constructor.BadSignatureConstructor" on path: DexPathList[[dex file "/tmp/junit4070388768283971494/classes.dex"],nativeLibraryDirectories=[r8/tools/linux/art/bin/../lib, r8/tools/linux/art/bin/../lib]]

          .put("lang.reflect.AccessibleObject.setAccessibleZ.AccessibleObject_setAccessible_A03",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "lang.reflect.UndeclaredThrowableException.serialization.UndeclaredThrowableException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/reflect/UndeclaredThrowableException/serialization/UndeclaredThrowableException_serialization_A01.golden.0.ser

          .put("lang.reflect.AccessibleObject.setAccessibleZ.AccessibleObject_setAccessible_A02",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "lang.reflect.AccessibleObject.setAccessible_Ljava_lang_reflect_AccessibleObjectZ.AccessibleObject_setAccessible_A03",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "lang.reflect.AccessibleObject.isAnnotationPresentLjava_lang_Class.AccessibleObject_isAnnotationPresent_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t04
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put(
              "lang.reflect.AccessibleObject.setAccessible_Ljava_lang_reflect_AccessibleObjectZ.AccessibleObject_setAccessible_A02",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.reflect.AccessibleObject.getAnnotations.AccessibleObject_getAnnotations_A01",
              any())
          // 1) t04
          // java.lang.AssertionError: expected:<0> but was:<1>
          // 2) t06
          // java.lang.AssertionError: Misconfigured test

          .put(
              "lang.reflect.AccessibleObject.getDeclaredAnnotations.AccessibleObject_getDeclaredAnnotations_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: expected:<0> but was:<1>

          .put(
              "lang.reflect.AccessibleObject.getAnnotationLjava_lang_Class.AccessibleObject_getAnnotation_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible
          // 2) t03
          // java.lang.AssertionError: Misconfiguration: MissingAntn should not be accessible

          .put("lang.IllegalAccessException.serialization.IllegalAccessException_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/IllegalAccessException/serialization/IllegalAccessException_serialization_A01.golden.0.ser

          .put("lang.Character.getTypeI.Character_getType_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<22> but was:<28>

          .put("lang.Character.isDigitI.Character_isDigit_A01", any())
          // 1) t02
          // java.lang.AssertionError

          .put("lang.Character.getTypeC.Character_getType_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<22> but was:<28>

          .put("lang.Character.serialization.Character_serialization_A01", any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/lang/Character/serialization/Character_serialization_A01.golden.0.ser

          .put("lang.Character.isDigitC.Character_isDigit_A01", any())
          // 1) t01
          // java.lang.AssertionError

          .put("lang.Character.digitCI.Character_digit_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<1> but was:<-1>

          .put("lang.Character.digitII.Character_digit_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<1> but was:<-1>

          .put("lang.Character.isLowerCaseC.Character_isLowerCase_A01", any())
          // 1) t01
          // java.lang.AssertionError

          .put("lang.Character.getDirectionalityI.Character_getDirectionality_A01", any())
          // 1) t01
          // java.lang.AssertionError

          .put("lang.Character.UnicodeBlock.ofC.UnicodeBlock_of_A01", any())
          // 1) t02
          // java.lang.AssertionError: expected null, but was:<ARABIC_SUPPLEMENT>

          .put("lang.Character.UnicodeBlock.ofI.UnicodeBlock_of_A01", any())
          // 1) t02
          // java.lang.AssertionError: expected null, but was:<ANCIENT_GREEK_NUMBERS>

          .put("lang.Character.isLowerCaseI.Character_isLowerCase_A01", any())
          // 1) t01
          // java.lang.AssertionError

          .put("lang.Process.waitFor.Process_waitFor_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: expected:<127> but was:<1>

          .put("lang.System.getProperties.System_getProperties_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Process.getErrorStream.Process_getErrorStream_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<69>

          .put("lang.Character.getDirectionalityC.Character_getDirectionality_A01", any())
          // 1) t01
          // java.lang.AssertionError: Char #0

          .put("lang.Process.exitValue.Process_exitValue_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: expected:<127> but was:<1>

          .put("lang.System.loadLjava_lang_String.System_load_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.Process.getInputStream.Process_getInputStream_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<0> but was:<-1>

          .put("lang.System.loadLibraryLjava_lang_String.System_loadLibrary_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "lang.System.setSecurityManagerLjava_lang_SecurityManager.System_setSecurityManager_A02",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.System.runFinalizersOnExitZ.System_runFinalizersOnExit_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.System.getenvLjava_lang_String.System_getenv_A01", any())
          // 1) t01
          // java.lang.AssertionError: Error: Could not find or load main class com.google.jctf.test.lib.java.lang.System.getenvLjava_lang_String.System_getenv_A01
          // expected:<0> but was:<1>

          .put("lang.System.getenv.System_getenv_A01", any())
          // 1) t01
          // java.lang.AssertionError: Error: Could not find or load main class com.google.jctf.test.lib.java.lang.System.getenv.System_getenv_A01
          // expected:<0> but was:<1>

          .put("lang.System.getPropertyLjava_lang_StringLjava_lang_String.System_getProperty_A01",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.System.exitI.System_exit_A01", any())
          // 1) t01
          // java.lang.AssertionError: expected:<88> but was:<1>

          .put(
              "util.concurrent.ArrayBlockingQueue.serialization.ArrayBlockingQueue_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/ArrayBlockingQueue/serialization/ArrayBlockingQueue_serialization_A01.golden.0.ser

          .put("lang.System.arraycopyLjava_lang_ObjectILjava_lang_ObjectII.System_arraycopy_A04",
              any())
          // 1) t05
          // java.lang.ArrayIndexOutOfBoundsException: src.length=3 srcPos=0 dst.length=1 dstPos=1 length=2
          // 2) t06
          // java.lang.ArrayIndexOutOfBoundsException: src.length=1 srcPos=0 dst.length=3 dstPos=1 length=2
          // 3) t07
          // java.lang.ArrayIndexOutOfBoundsException: src.length=1 srcPos=0 dst.length=1 dstPos=1 length=100

          .put("lang.System.setPropertiesLjava_util_Properties.System_setProperties_A02", any())
          // 1) t01
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.System.setPropertiesLjava_util_Properties.System_setProperties_A02
          // expected:<0> but was:<1>
          // 2) t02
          // java.lang.AssertionError: Bad exit code of spawned java proccess, err=Error: Could not find or load main class com.google.jctf.test.lib.java.lang.System.setPropertiesLjava_util_Properties.System_setProperties_A02
          // expected:<0> but was:<1>

          .put("lang.System.clearPropertyLjava_lang_String.System_clearProperty_A02", any())
          // 1) t01
          // java.lang.SecurityException

          .put("lang.System.getPropertyLjava_lang_String.System_getProperty_A01", any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "util.concurrent.LinkedBlockingQueue.serialization.LinkedBlockingQueue_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/LinkedBlockingQueue/serialization/LinkedBlockingQueue_serialization_A01.golden.0.ser

          .put(
              "util.concurrent.LinkedBlockingDeque.serialization.LinkedBlockingDeque_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/LinkedBlockingDeque/serialization/LinkedBlockingDeque_serialization_A01.golden.0.ser

          .put(
              "util.concurrent.ConcurrentLinkedQueue.serialization.ConcurrentLinkedQueue_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/ConcurrentLinkedQueue/serialization/ConcurrentLinkedQueue_serialization_A01.golden.0.ser

          .put("util.concurrent.SynchronousQueue.serialization.SynchronousQueue_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers
          // 2) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/SynchronousQueue/serialization/SynchronousQueue_serialization_A01.golden.0.ser

          .put(
              "util.concurrent.CopyOnWriteArrayList.serialization.CopyOnWriteArrayList_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/CopyOnWriteArrayList/serialization/CopyOnWriteArrayList_serialization_A01.golden.0.ser

          .put("util.concurrent.CopyOnWriteArrayList.subListII.CopyOnWriteArrayList_subList_A01",
              any())
          // 1) t03
          // java.util.ConcurrentModificationException

          .put(
              "util.concurrent.ScheduledThreadPoolExecutor.getTaskCount.ScheduledThreadPoolExecutor_getTaskCount_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: expected:<1> but was:<2>

          .put(
              "util.concurrent.ConcurrentHashMap.serialization.ConcurrentHashMap_serialization_A01",
              any())
          // 1) t01
          // java.lang.AssertionError: Unable to configure default providers

          .put("util.concurrent.ConcurrentHashMap.keySet.ConcurrentHashMap_keySet_A01", any())
          // 1) t01
          // java.lang.NoSuchMethodError: No virtual method keySet()Ljava/util/concurrent/ConcurrentHashMap$KeySetView; in class Ljava/util/concurrent/ConcurrentHashMap; or its super classes (declaration of 'java.util.concurrent.ConcurrentHashMap' appears in r8/tools/linux/art/framework/core-oj-hostdex.jar)
          // 2) t02
          // java.lang.NoSuchMethodError: No virtual method keySet()Ljava/util/concurrent/ConcurrentHashMap$KeySetView; in class Ljava/util/concurrent/ConcurrentHashMap; or its super classes (declaration of 'java.util.concurrent.ConcurrentHashMap' appears in r8/tools/linux/art/framework/core-oj-hostdex.jar)

          .put(
              "util.concurrent.Executors.privilegedThreadFactory.Executors_privilegedThreadFactory_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Unexpected exception: java.lang.AssertionError: no AccessControlException
          // 2) t02
          // java.lang.AssertionError: java.lang.AssertionError: no AccessControlException
          // Caused by: java.lang.AssertionError: no AccessControlException
          // 3) t03
          // java.lang.AssertionError: Unexpected exception: java.lang.AssertionError: no AccessControlException
          // 4) t03
          // java.lang.AssertionError: java.lang.AssertionError: no AccessControlException
          // Caused by: java.lang.AssertionError: no AccessControlException

          .put(
              "util.concurrent.Executors.privilegedCallableLjava_util_concurrent_Callable.Executors_privilegedCallable_A01",
              any())
          // 1) t01
          // java.lang.SecurityException

          .put(
              "util.concurrent.CopyOnWriteArraySet.serialization.CopyOnWriteArraySet_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/CopyOnWriteArraySet/serialization/CopyOnWriteArraySet_serialization_A01.golden.0.ser

          .put(
              "util.concurrent.Executors.privilegedCallableUsingCurrentClassLoaderLjava_util_concurrent_Callable.Executors_privilegedCallableUsingCurrentClassLoader_A01",
              any())
          // 1) t02
          // java.lang.SecurityException

          .put(
              "util.concurrent.PriorityBlockingQueue.ConstructorLjava_util_Collection.PriorityBlockingQueue_Constructor_A01",
              any())
          // 1) t03
          // java.lang.AssertionError: expected same:<com.google.jctf.test.lib.java.util.concurrent.PriorityBlockingQueue.MyReverseComparator@735f1e1> was not:<null>

          .put(
              "util.concurrent.PriorityBlockingQueue.serialization.PriorityBlockingQueue_serialization_A01",
              any())
          // 1) t02
          // java.lang.AssertionError: Failed to load serialization resource file: serialization/com/google/jctf/test/lib/java/util/concurrent/PriorityBlockingQueue/serialization/PriorityBlockingQueue_serialization_A01.golden.0.ser

          .put("lang.ThreadGroup.destroy.ThreadGroup_destroy_A01", match(R8_NOT_AFTER_D8_COMPILER))
          // 1) t05
          // java.lang.AssertionError: Destroyed thread group was not finalized

          .put("lang.ThreadGroup.destroy.ThreadGroup_destroy_A01",
              match(D8_COMPILER, runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.IllegalThreadStateException: Thread group still contains threads: Test group
          // 2) t04
          // java.lang.IllegalThreadStateException: Thread group still contains threads:  Depth = 2, N = 0
          // 3) t05
          // java.lang.AssertionError: Destroyed thread group was not finalized

          .put("lang.Thread.start.Thread_start_A01",
              match(runtimes(DexVm.ART_7_0_0)))
          // 1) t01(com.google.jctf.test.lib.java.lang.Thread.start.Thread_start_A01)
          // java.lang.AssertionError: no IllegalThreadStateException 1

          .put("lang.String.getBytesLjava_lang_String.String_getBytes_A02",
              match(runtimes(DexVm.ART_7_0_0, DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01(com.google.jctf.test.lib.java.lang.String.getBytesLjava_lang_String.String_getBytes_A02)
          // java.lang.Exception: Unexpected exception, expected<java.lang.NullPointerException> but was<java.io.UnsupportedEncodingException>

          .put(
              "util.concurrent.CopyOnWriteArrayList.lastIndexOfLjava_lang_ObjectI.CopyOnWriteArrayList_lastIndexOf_A02",
              match(runtimes(DexVm.ART_7_0_0, DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // java.lang.AssertionError: Expected exception: java.lang.IndexOutOfBoundsException

          .put(
              "util.concurrent.CopyOnWriteArrayList.lastIndexOfLjava_lang_ObjectI.CopyOnWriteArrayList_lastIndexOf_A01",
              match(runtimes(DexVm.ART_7_0_0, DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01(com.google.jctf.test.lib.java.util.concurrent.CopyOnWriteArrayList.lastIndexOfLjava_lang_ObjectI.CopyOnWriteArrayList_lastIndexOf_A01)
          // java.lang.AssertionError: expected:<3> but was:<1>
          // 2) t02(com.google.jctf.test.lib.java.util.concurrent.CopyOnWriteArrayList.lastIndexOfLjava_lang_ObjectI.CopyOnWriteArrayList_lastIndexOf_A01)
          // java.lang.ArrayIndexOutOfBoundsException: length=3; index=2147483647

          .put("lang.StringBuffer.getCharsII_CI.StringBuffer_getChars_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t03
          // java.lang.NullPointerException: dst == null

          .put("lang.StringBuffer.appendF.StringBuffer_append_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError: Buffer is invalid length after append expected:<26> but was:<25>

          .put("lang.StringBuffer.insertI_CII.StringBuffer_insert_A02",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.NullPointerException: Attempt to get length of null array

          .put("lang.StrictMath.scalbDI.StrictMath_scalb_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Wrong result provided for argument: -1.7976931348623157E308 scaleFactor: 2147483647 expected:<-Infinity> but was:<-0.0>

          .put("lang.StrictMath.scalbDI.StrictMath_scalb_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t03
          // java.lang.AssertionError: Wrong result provided for argument: -2.2250738585072014E-308 scaleFactor: -2147483647 expected:<-0.0> but was:<-Infinity>
          // 2) t04
          // java.lang.AssertionError: Wrong result provided for argument: 1.7976931348623157E308 scaleFactor: -2046 expected:<2.2250738585072014E-308> but was:<2.225073858507201E-308>

          .put("lang.StrictMath.scalbFI.StrictMath_scalb_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Wrong result provided for argument: -3.4028235E38 scaleFactor: 2147483647 expected:<-Infinity> but was:<-0.0>

          .put("lang.StrictMath.scalbFI.StrictMath_scalb_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError: Wrong result provided for argument: 3.4028235E38 scaleFactor: -254 expected:<1.1754943508222875E-38> but was:<1.1754942106924411E-38>
          // 2) t03
          // java.lang.AssertionError: Wrong result provided for argument: -1.1754944E-38 scaleFactor: -2147483647 expected:<-0.0> but was:<-Infinity>

          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A07",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError: wrong daemonism expected:<true> but was:<false>

          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A07",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError: wrong daemonism expected:<true> but was:<false>

          .put("lang.Thread.toString.Thread_toString_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError

          .put("lang.Thread.start.Thread_start_A02",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.IllegalThreadStateException: Thread group still contains threads: start

          .put("lang.Thread.setPriorityI.Thread_setPriority_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError: expected:<5> but was:<10>

          .put("lang.ClassLoader.ConstructorLjava_lang_ClassLoader.ClassLoader_Constructor_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.NullPointerException: parentLoader == null && !nullAllowed
          // 2) t03
          // java.lang.NullPointerException: parentLoader == null && !nullAllowed

          .put("lang.Enum.compareToLjava_lang_Enum.Enum_compareTo_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.ClassCastException

          .put("lang.Enum.hashCode.Enum_hashCode_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError

          .put("lang.StackTraceElement.hashCode.StackTraceElement_hashCode_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // java.lang.AssertionError

          .put("lang.ProcessBuilder.environment.ProcessBuilder_environment_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: should throw ClassCastException.

          .put("lang.ProcessBuilder.environment.ProcessBuilder_environment_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: should throw ClassCastException.

          .put("lang.Float.toStringF.Float_toString_A04",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // org.junit.ComparisonFailure: Invalid string produced for bits: 4efffffa expected:<2.147482[88]E9> but was:<2.147482[9]E9>

          .put("lang.Float.toStringF.Float_toString_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t02
          // org.junit.ComparisonFailure: expected:<-1.175494[35]E-38> but was:<-1.175494[4]E-38>

          .put("lang.ThreadGroup.getMaxPriority.ThreadGroup_getMaxPriority_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: New value should be the same as we set expected:<2> but was:<1>

          .put(
              "lang.ThreadGroup.uncaughtExceptionLjava_lang_ThreadLjava_lang_Throwable.ThreadGroup_uncaughtException_A02",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t05
          // java.lang.AssertionError: Non-informative exception info: java.lang.RuntimeException

          .put("lang.ThreadGroup.list.ThreadGroup_list_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t04
          // java.lang.IllegalThreadStateException: Thread group still contains threads: Test group(list)

          .put("lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.IllegalThreadStateException: Thread group still contains threads: Test root(setMaxPriority)

          .put("lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A04",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: New value should be the same as we set expected:<2> but was:<1>
          // 2) t02
          // java.lang.AssertionError: expected:<4> but was:<1>
          // 3) t03
          // java.lang.AssertionError: expected:<7> but was:<1>

          .put("lang.ThreadGroup.toString.ThreadGroup_toString_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // org.junit.ComparisonFailure: toString does not follow the RI expected:<... group(toString),max[pri]=10]> but was:<... group(toString),max[Priority]=10]>

          .put("lang.Class.getFieldLjava_lang_String.Class_getField_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t04
          // java.lang.AssertionError: expected:<interface com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A01$I1> but was:<class com.google.jctf.test.lib.java.lang.Class.getFieldLjava_lang_String.Class_getField_A01$S1>

          .put("lang.String.replaceCC.String_replace_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t04
          // java.lang.AssertionError: expected same:<aaaaaa> was not:<aaaaaa>

          .put("lang.Package.isCompatibleWithLjava_lang_String.Package_isCompatibleWith_A02",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: NumberFormatException isn't thrown for desired . and current 1.0
          // 2) t03
          // java.lang.AssertionError: NumberFormatException isn't thrown for desired 1.0 and current .

          .put("lang.StringBuilder.appendF.StringBuilder_append_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Invalid length of created builder expected:<14> but was:<13>

          .put("lang.StringBuilder.insertIF.StringBuilder_insert_A01",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Invalid length of created builder expected:<14> but was:<13>

          .put(
              "lang.reflect.AccessibleObject.setAccessible_Ljava_lang_reflect_AccessibleObjectZ.AccessibleObject_setAccessible_A04",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: SecurityException expected.
          // 2) t02
          // java.lang.AssertionError: SecurityException expected.
          // 3) t03
          // java.lang.AssertionError: SecurityException expected.
          // 4) t04
          // java.lang.AssertionError: SecurityException expected.

          .put("lang.Character.UnicodeBlock.forName_java_lang_String.UnicodeBlock_forName_A03",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

          .put("lang.System.loadLjava_lang_String.System_load_A02",
              match(runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1)))
          // 1) t03
          // java.lang.AssertionError: Expected exception: java.lang.UnsatisfiedLinkError

          .put("lang.StrictMath.nextAfterFD.StrictMath_nextAfter_A01",
              match(R8_NOT_AFTER_D8_COMPILER, runtimes(DexVm.ART_5_1_1)))
          // 1) t01
          // java.lang.AssertionError: Wrong value returned for start: Infinity direction: NaN expected:<Infinity> but was:<NaN>
          // 2) t02
          // java.lang.AssertionError: Wrong value returned for start: -0.0 direction: NaN expected:<-1.4E-45> but was:<NaN>

          .put("lang.Math.hypotDD.Math_hypot_A04", match(runtimes(DexVm.ART_5_1_1)))
          // 1) t04
          // java.lang.AssertionError

          .put("lang.reflect.Field.getLjava_lang_Object.Field_get_A04", match(R8_AFTER_D8_COMPILER))
          // 1) t02
          // java.lang.AssertionError: expected:<9223372036854775807> but was:<72057594037927935>

          .put("lang.reflect.Field.getLongLjava_lang_Object.Field_getLong_A04", match(R8_AFTER_D8_COMPILER))
          // 1)
          // java.lang.AssertionError: expected:<9223372036854775807> but was:<72057594037927935>

          .build(); // end of failuresToTriage

  public static final Multimap<String, TestCondition> flakyWithArt =
      new ImmutableListMultimap.Builder<String, TestCondition>()

          .put("lang.Object.notifyAll.Object_notifyAll_A03", any())
          // AssertionError: Unexpected art failure: '+ invoke_with=
          // AssertionError: expected:<BLOCKED> but was:<WAITING>

          .put("lang.Object.notify.Object_notify_A03", any())
          // AssertionError: Unexpected art failure: '+ invoke_with=
          // AssertionError: expected:<BLOCKED> but was:<WAITING>

          .put(
              "util.concurrent.ConcurrentSkipListSet.addLjava_lang_Object.ConcurrentSkipListSet_add_A01",
              any())
          // AssertionError: Unexpected art failure: '+ invoke_with=
          // AssertionError: Expected exception: java.lang.ClassCastException

          .put("util.concurrent.SynchronousQueue.ConstructorZ", any())
          // Only on bots:
          // 1) t02
          // java.lang.AssertionError: java.lang.AssertionError: expected:<5> but was:<4>

          .put("lang.Thread.interrupt.Thread_interrupt_A04", any())
          // Been running fine for a while then this happened on a bot:
          // 1) t01
          // java.lang.AssertionError: expected:<BLOCKED> but was:<RUNNABLE>

          .put("util.concurrent.SynchronousQueue.ConstructorZ.SynchronousQueue_Constructor_A01",
              any())
          // Failed on bot only:
          // 1) t02
          // java.lang.AssertionError: java.lang.AssertionError: expected:<7> but was:<6>

          .put("lang.Thread.getState.Thread_getState_A01", any())
          // Failed on bot only (R8):
          // 1) t02
          // java.lang.AssertionError: java.lang.AssertionError: expected:<7> but was:<6>

          .build(); // end of flakyWithArt

  public static final Multimap<String, TestCondition> timeoutsWithArt =
      new ImmutableListMultimap.Builder<String, TestCondition>()
          .put("lang.Thread.interrupt.Thread_interrupt_A01", any())
          .put("lang.Thread.resume.Thread_resume_A01", any())
          .put("lang.Thread.stop.Thread_stop_A01", any())
          .put("lang.Thread.suspend.Thread_suspend_A01", any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A04",
              any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A03",
              any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_StringJ.Thread_Constructor_A05",
              any())
          .put("lang.Thread.setNameLjava_lang_String.Thread_setName_A02", any())
          .put("lang.Thread.stop.Thread_stop_A02", any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A02",
              any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A03",
              any())
          .put("lang.Thread.getStackTrace.Thread_getStackTrace_A03", any())
          .put(
              "lang.Thread.setDefaultUncaughtExceptionHandler.Thread_setDefaultUncaughtExceptionHandler_A02",
              any())
          .put("lang.Thread.checkAccess.Thread_checkAccess_A01", any())
          .put(
              "lang.Thread.ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String.Thread_Constructor_A04",
              any())
          .put("lang.Thread.setUncaughtExceptionHandler.Thread_setUncaughtExceptionHandler_A02",
              any())
          .put("lang.Thread.stopLjava_lang_Throwable.Thread_stop_A01", any())
          .put("lang.Thread.getAllStackTraces.Thread_getAllStackTraces_A02", any())
          .put(
              "lang.Thread.setContextClassLoaderLjava_lang_ClassLoader.Thread_setContextClassLoader_A02",
              any())
          .put("lang.Thread.setPriorityI.Thread_setPriority_A02", any())
          .put("lang.Thread.stopLjava_lang_Throwable.Thread_stop_A02", any())
          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A04",
              any())
          .put("lang.Thread.getContextClassLoader.Thread_getContextClassLoader_A02", any())
          .put("lang.ThreadGroup.suspend.ThreadGroup_suspend_A02", any())
          .put("lang.Thread.setDaemonZ.Thread_setDaemon_A03", any())
          .put("lang.ProcessBuilder.environment.ProcessBuilder_environment_A07", any())
          .put(
              "lang.Runtime.exec_Ljava_lang_String_Ljava_lang_StringLjava_io_File.Runtime_exec_A04",
              any())
          .put("lang.Runtime.execLjava_lang_String_Ljava_lang_String.Runtime_exec_A04", any())
          .put("lang.Runtime.exec_Ljava_lang_String.Runtime_exec_A04", any())
          .put("lang.Runtime.execLjava_lang_String.Runtime_exec_A04", any())
          .put("lang.System.clearPropertyLjava_lang_String.System_clearProperty_A03", any())
          .put("lang.System.getSecurityManager.System_getSecurityManager_A01", any())
          .put("lang.System.setInLjava_io_InputStream.System_setIn_A02", any())
          .put("lang.System.setOutLjava_io_PrintStream.System_setOut_A02", any())
          .put("lang.ThreadGroup.destroy.ThreadGroup_destroy_A04", any())
          .put("lang.ThreadGroup.enumerate_ThreadGroupZ.ThreadGroup_enumerate_A03", any())
          .put("lang.ThreadGroup.enumerate_Thread.ThreadGroup_enumerate_A03", any())
          .put("lang.ThreadGroup.enumerate_ThreadZ.ThreadGroup_enumerate_A03", any())
          .put("lang.ThreadGroup.interrupt.ThreadGroup_interrupt_A02", any())
          .put("lang.ThreadGroup.resume.ThreadGroup_resume_A02", any())
          .put("lang.ThreadGroup.setMaxPriorityI.ThreadGroup_setMaxPriority_A02", any())
          .put("lang.Runtime.exec_Ljava_lang_String_Ljava_lang_String.Runtime_exec_A04", any())
          .put("lang.System.getenvLjava_lang_String.System_getenv_A03", any())
          .put("lang.System.setPropertyLjava_lang_StringLjava_lang_String.System_setProperty_A02",
              any())
          .put("lang.ThreadGroup.enumerate_ThreadGroup.ThreadGroup_enumerate_A03", any())
          .put("lang.ThreadGroup.getParent.ThreadGroup_getParent_A02", any())
          .put("lang.ThreadGroup.setDaemonZ.ThreadGroup_setDaemon_A02", any())
          .put("lang.ThreadGroup.stop.ThreadGroup_stop_A02", any())
          .put("lang.Class.getSuperclass.Class_getSuperclass_A01", any())
          .put("lang.System.getenv.System_getenv_A03", any())
          .put("lang.System.inheritedChannel.System_inheritedChannel_A01", any())
          .put(
              "util.concurrent.ArrayBlockingQueue.containsLjava_lang_Object.ArrayBlockingQueue_contains_A01",
              any())
          .put("lang.System.arraycopyLjava_lang_ObjectILjava_lang_ObjectII.System_arraycopy_A03",
              any())
          .put("lang.System.setErrLjava_io_PrintStream.System_setErr_A02", any())
          .put(
              "util.concurrent.ArrayBlockingQueue.containsLjava_lang_Object.ArrayBlockingQueue_contains_A01",
              any())
          .put(
              "lang.System.setSecurityManagerLjava_lang_SecurityManager.System_setSecurityManager_A01",
              any())
          .put(
              "util.concurrent.ArrayBlockingQueue.containsLjava_lang_Object.ArrayBlockingQueue_contains_A01",
              any())
          .put(
              "util.concurrent.ArrayBlockingQueue.containsLjava_lang_Object.ArrayBlockingQueue_contains_A01",
              any())
          .put("lang.System.setPropertiesLjava_util_Properties.System_setProperties_A01", any())
          .put(
              "util.concurrent.CopyOnWriteArrayList.ConstructorLjava_util_Collection.CopyOnWriteArrayList_Constructor_A02",
              any())
          .put("util.concurrent.CyclicBarrier.reset.CyclicBarrier_reset_A03", any())
          .put("lang.System.clearPropertyLjava_lang_String.System_clearProperty_A01", any())
          .put("lang.System.getenv.System_getenv_A04", any())
          .put("lang.RuntimePermission.Class.RuntimePermission_class_A02", any())
          .put("lang.RuntimePermission.Class.RuntimePermission_class_A13", any())
          .build(); // end of timeoutsWithArt

  private static final boolean testMatch(
      Multimap<String, TestCondition> testConditions,
      String name,
      CompilerUnderTest compilerUnderTest,
      DexVm dexVm,
      CompilationMode compilationMode) {
    Collection<TestCondition> entries = testConditions.get(name);
    for (TestCondition entry : entries) {
      if (entry.test(DexTool.NONE, compilerUnderTest, dexVm, compilationMode)) {
        return true;
      }
    }
    return false;
  }

  public static final Outcome getExpectedOutcome(
      String name,
      CompilerUnderTest compilerUnderTest,
      DexVm dexVm,
      CompilationMode compilationMode) {

    Outcome outcome = null;

    if (testMatch(failuresToTriage, name, compilerUnderTest, dexVm, compilationMode)) {
      outcome = Outcome.FAILS_WITH_ART;
    }
    if (testMatch(timeoutsWithArt, name, compilerUnderTest, dexVm, compilationMode)) {
      assert outcome == null;
      outcome = Outcome.TIMEOUTS_WITH_ART;
    }
    if (testMatch(flakyWithArt, name, compilerUnderTest, dexVm, compilationMode)) {
      assert outcome == null;
      outcome = Outcome.FLAKY_WITH_ART;
    }
    return outcome == null ? Outcome.PASSES : outcome;
  }
}
