#!/bin/bash
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.
#
# Tracks the peak resident memory being used by running the supplied command.
# The output is written to the first argument of the script every second.

function Logger() {
  output="$1"
  while sleep 1
  do
    grep "VmHWM\|Threads" /proc/$pid/status >> $output
  done
}

function Exit {
  kill $lid
  exit 0
}

function Kill {
  kill $lid
  kill -9 $pid
  exit -1
}

if [ $# -lt 2 ]; then
   echo "Takes at least two arguments"
   exit 1
fi
OUTPUT_FILE=$1
shift 1

$* &
pid=$!

Logger $OUTPUT_FILE &
lid=$!

trap "Exit" EXIT
trap "Kill" SIGINT
wait $pid
