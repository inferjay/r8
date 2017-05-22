#! /bin/bash
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# This script will update the host dx and dexmerger tools in tools/linux/dx.

# Before running this script make sure the dx and dexmerger versions required are built
# in ~/android/master (There are probably simpler ways to just build dx and dexmerger):
#
#  m -j24 build-art
#
# Maybe also run the Art host tests:
#
#  mm -j24 art-test-host
#

ANDROID_CHECKOUT=~/android/master
ANDROID_HOST_BUILD=$ANDROID_CHECKOUT/out/host/linux-x86
LINUX_TOOLS=tools/linux
DX_DIR=dx
DEST=$LINUX_TOOLS/$DX_DIR

# Clean out the previous version of Art
rm -rf $DEST

# Required binaries and scripts.
mkdir -p $DEST/bin
cp $ANDROID_HOST_BUILD/bin/dx $DEST/bin
cp $ANDROID_HOST_BUILD/bin/dexmerger $DEST/bin

# Required framework files.
mkdir -p $DEST/framework
cp $ANDROID_HOST_BUILD/framework/dx.jar $DEST/framework

# Build the tar to upload to Google Cloud Storage.
DX_ARCHIVE=$DX_DIR.tar.gz
pushd $LINUX_TOOLS > /dev/null
rm -f $DX_ARCHIVE
rm -f $DX_ARCHIVE.sha1
tar caf $DX_ARCHIVE $DX_DIR
popd  > /dev/null

echo "New $LINUX_TOOLS/$DX_ARCHIVE archive created."
echo ""
echo "Now run:"
echo ""
echo "  cd $LINUX_TOOLS"
echo "  upload_to_google_storage.py --bucket r8-deps $DX_DIR.tar.gz"
echo ""
echo "to upload to Google Cloud Storage"
