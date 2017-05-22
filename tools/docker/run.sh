#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

function follow_links() {
  file="$1"
  while [ -h "$file" ]; do
    # On Mac OS, readlink -f doesn't work.
    file="$(readlink "$file")"
  done
  echo "$file"
}

PROG_NAME="$(follow_links $0)"
PROG_DIR="$(cd "${PROG_NAME%/*}" ; pwd -P)"
R8_ROOT=$PROG_DIR/../..

CONTAINER_NAME=r8
HOST_SHARE=$(cd "$R8_ROOT" ; pwd -P)
CONTAINER_USER=r8
CONTAINER_HOME=/home/$CONTAINER_USER
CONTAINER_SHARE=$CONTAINER_HOME/share

ARGS=$@

docker run \
  --volume $HOST_SHARE:$CONTAINER_SHARE \
  --rm \
  --workdir "$CONTAINER_SHARE" \
  r8 \
  bash -c "$ARGS"

