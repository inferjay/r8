# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Rudmentary script for generating smali files from a Java file
# compiled with both javac/dx and jack.

# This requires a Android checkout in $HOME/android/master with the
# art host test tools build:
#
#    source build/envsetup.sh
#    lunch <some configuration, e.g. aosp_bullhead-userdebug>
#    m -j30 test-art-host
#
# It also requires a checkout of https://github.com/JesusFreke/smali
# in $HOME/smali build by running "gradle build" in that directory.
#
# The output from javac/dx is placed in classes_dx, and the output from
# Jack is placed in classes_jack.

set -e

JAVA_FILE=Test.java

ANDROID_HOME="$HOME/android/master"
SMALI_HOME="$HOME/smali"

# Build with javac/dx and decompile dex file.
mkdir -p classes_dx
javac -d classes_dx -target 1.7 -source 1.7 $JAVA_FILE
tools/linux/dx/bin/dx --dex --output classes_dx/classes.dex classes_dx
java -jar "$SMALI_HOME/baksmali/build/libs/baksmali.jar" --output classes_dx classes_dx/classes.dex

# Build with Jack and decompile dex file.
mkdir -p classes_jack
JACK_JAVA_LIBRARIES="$ANDROID_HOME/out/host/common/obj/JAVA_LIBRARIES"
JACK="$ANDROID_HOME/out/host/linux-x86/bin/jack -cp $JACK_JAVA_LIBRARIES/core-libart-hostdex_intermediates/classes.jack:$JACK_JAVA_LIBRARIES/core-oj-hostdex_intermediates/classes.jack"
$JACK $JAVA_FILE --output-dex classes_jack
java -jar $SMALI_HOME/baksmali/build/libs/baksmali.jar --output classes_jack classes_jack/classes.dex
