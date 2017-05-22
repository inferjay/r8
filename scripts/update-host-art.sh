#! /bin/bash
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# This script will update the host art VM in tools/linux/art

# Before running this script make sure that you have a full android build
# and that the host Art version required is build in ~/android/master:
#
#  m -j24
#  m -j24 build-art
#
# Maybe also run the Art host tests:
#
#  m -j24 test-art-host

set -e

ANDROID_CHECKOUT=~/android/master
ANDROID_PRODUCT=angler
ART_DIR=art
DEST_ROOT=tools/linux

function usage {
  echo "Usage: $(basename $0)"
  echo "  [--android-checkout <android repo root>]"
  echo "  [--art-dir <destination directory>]"
  echo "  [--android-product <product name>]"
  echo "  [--destination-dir <destination directory>]"
  echo ""
  echo "  --android-checkout specifies the Android repo root"
  echo "  Defaults to: $ANDROID_CHECKOUT"
  echo ""
  echo "  --art-dir specifies the directory name inside the root destination"
  echo "  directory for the art bundle"
  echo "  Defaults to: $ART_DIR"
  echo ""
  echo "  --android-product specifies the Android product for the framework to include"
  echo "  Defaults to: $ANDROID_PRODUCT"
  echo ""
  echo "  --destination-dir specifies the root destination directory for the art bundle"
  echo "  Defaults to: $DEST_ROOT"
  echo ""
  echo "Update the master version of art from ~/android/master:"
  echo "  "
  echo "  $(basename $0)"
  echo "  "
  echo "Update a specific version of art:"
  echo "  "
  echo "  $(basename $0) --android-checkout ~/android/5.1.1_r19 --art-dir 5.1.1"
  echo "  "
  echo "Test the Art bundle in a temporary directory:"
  echo "  "
  echo "  $(basename $0) --android-checkout ~/android/5.1.1_r19 --art-dir art-5.1.1 --android-product mako --destination-dir /tmp/art"
  echo "  "
  exit 1
}

# Process options.
while [ $# -gt 0 ]; do
  case $1 in
    --android-checkout)
      ANDROID_CHECKOUT="$2"
      shift 2
      ;;
    --art-dir)
      ART_DIR="$2"
      shift 2
      ;;
    --android-product)
      ANDROID_PRODUCT="$2"
      shift 2
      ;;
    --destination-dir)
      DEST_ROOT="$2"
      shift 2
      ;;
    --help|-h|-H|-\?)
      usage
      ;;
    *)
      echo "Unkonwn option $1"
      echo ""
      usage
      ;;
  esac
done

ANDROID_HOST_BUILD=$ANDROID_CHECKOUT/out/host/linux-x86
ANDROID_TARGET_BUILD=$ANDROID_CHECKOUT/out/target
DEST=$DEST_ROOT/$ART_DIR

# Clean out the previous version of Art
rm -rf $DEST

# Required binaries and scripts.
mkdir -p $DEST/bin
if [ -f $ANDROID_HOST_BUILD/bin/art ]; then
  cp $ANDROID_HOST_BUILD/bin/art $DEST/bin
else
  cp $ANDROID_CHECKOUT/art/tools/art $DEST/bin
fi

cp $ANDROID_HOST_BUILD/bin/dalvikvm64 $DEST/bin
cp $ANDROID_HOST_BUILD/bin/dalvikvm32 $DEST/bin
# Copy the dalvikvm link creating a regular file instead, as download_from_google_stroage.py does
# not allow tar files with symbolic links (depending on Android/Art version dalvikvm links to
# dalvikvm32 or dalvikvm64).
cp $ANDROID_HOST_BUILD/bin/dalvikvm $DEST/bin
cp $ANDROID_HOST_BUILD/bin/dex2oat $DEST/bin
cp $ANDROID_HOST_BUILD/bin/patchoat $DEST/bin

# Required framework files.
mkdir -p $DEST/framework
cp -R $ANDROID_HOST_BUILD/framework/* $DEST/framework

# Required library files.
mkdir -p $DEST/lib64
cp -r $ANDROID_HOST_BUILD/lib64/* $DEST/lib64
mkdir -p $DEST/lib
cp -r $ANDROID_HOST_BUILD/lib/* $DEST/lib

# Image files required for dex2oat of actual android apps. We need an actual android product
# image containing framework classes to verify the code against.
mkdir -p $DEST/product/$ANDROID_PRODUCT/system/framework
cp -r $ANDROID_TARGET_BUILD/product/$ANDROID_PRODUCT/system/framework/* $DEST/product/$ANDROID_PRODUCT/system/framework

# Required auxillary files.
mkdir -p $DEST/usr/icu
cp -r $ANDROID_HOST_BUILD/usr/icu/* $DEST/usr/icu

# Allow failure for strip commands below.
set +e

strip $DEST/bin/* 2> /dev/null
strip $DEST/lib/*
strip $DEST/lib64/*
strip $DEST/framework/x86/* 2> /dev/null
strip $DEST/framework/x86_64/* 2> /dev/null

echo "Now run"
echo "(cd $DEST_ROOT; upload_to_google_storage.py -a --bucket r8-deps $ART_DIR)"
