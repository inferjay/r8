#! /bin/bash
#
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

set -e

if [ -z "$R8_HOME" ]; then
  R8_HOME="$(realpath $(dirname ${BASH_SOURCE[0]})/..)"
fi

TOOLSDIR=$R8_HOME/tools/linux

function usage {
  echo "Usage: $(basename $0) <dex files>"
  exit 1
}

# Process options.
while [ $# -gt 0 ]; do
  case $1 in
    -h)
      usage
      ;;
    *)
      break
      ;;
  esac
done

if [ $# -eq 0 ]; then
  usage
fi

TMPDIR=$(mktemp -d "${TMP:-/tmp/}$(basename $0).XXXXXXXXXXXX")
OATFILE=$TMPDIR/all.oat

if [ $# -gt 1 ]; then
  JARFILE="$TMPDIR/all.jar"
  for f in "$@"; do
    IR=$(dirname "$f")
    BASE=$(basename "$f")
    EXT=$(echo "$BASE" | cut -d '.' -f 2)
    if [ "$EXT" = "dex" ]; then
      (cd "$DIR" && zip "$JARFILE" "$BASE")
    else
      echo "Warning: ignoring non-dex file argument when dex2oat'ing multiple files."
    fi
  done
else
  JARFILE="$1"
fi

LD_LIBRARY_PATH=$TOOLSDIR/art/lib $TOOLSDIR/art/bin/dex2oat \
  --android-root=$TOOLSDIR/art/product/angler \
  --runtime-arg -Xnorelocate \
  --boot-image=$TOOLSDIR/art/product/angler/system/framework/boot.art \
  --dex-file=$JARFILE \
  --oat-file=$OATFILE \
  --instruction-set=arm64 \
  --compiler-filter=interpret-only

rm -rf $TMPDIR
