#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.
#
# Replacement for 'dx' script invoked by the AOSP makefiles.
# This script invokes 'd8' instead while providing the same API.
#
# Based on this file:
#
#     repo: https://android.googlesource.com/platform/prebuilts/sdk
#     file: tools/dx
#     SHA1: 6f6b5641b531f18c8e8d314b4b0560370ffbf1ab
#
# or this (identical)
#
#     repo: https://android.googlesource.com/platform/dalvik
#     file: dx/etc/dx
#     SHA1: 9fa280c2171b3a016d63e80cda7be031519b7ef7

readonly ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

defaultMx="-Xmx1024M"

# The following will extract any initial parameters of the form
# "-J<stuff>" from the command line and pass them to the Java
# invocation (instead of to dx). This makes it possible for you to add
# a command-line parameter such as "-JXmx256M" in your scripts, for
# example. "java" (with no args) and "java -X" give a summary of
# available options.

javaOpts=""

while expr "x$1" : 'x-J' >/dev/null; do
    opt=`expr "x$1" : 'x-J\(.*\)'`
    javaOpts="${javaOpts} -${opt}"
    if expr "x${opt}" : "xXmx[0-9]" >/dev/null; then
        defaultMx="no"
    fi
    shift
done

if [ "${defaultMx}" != "no" ]; then
    javaOpts="${javaOpts} ${defaultMx}"
fi

jarpath="$ROOT/build/libs/d8.jar"

exec java $javaOpts -jar "$jarpath" --dex "$@"

