#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.
#

# Take a file where each line is a tab-separated list of arguments for D8
# and create a self-contained directory with all the input files and a script
# which replays the same D8 invocations as the original list.
#
# Usage:
#
#     create_d8_replay <d8-args-script> <output-dir>
#
# The <d8-args-script> is a text file where each line contains tab-separated
# arguments for a D8 call.
# The script 'scripts/test_android_cts.sh' can log D8 invocations during an AOSP
# build to such a file.
set -e

# This function will be called with the out_dir, input_counter and
# arguments for the original D8 invocation. It copies the inputs
# of the D8 call into the output directory and appends the replay
# script with a new line which invokes D8 on the local inputs.
function process_line {
  local out_dir="$1"
  shift
  local input_counter=$1
  shift

  args=()
  inputs=()
  while (( "$#" )); do
    if [[ "$1" =~ ^--output=(.*) ]]; then
      :
    elif [[ "$1" =~ ^(--.*) ]]; then
      args+=("$1")
    else
      # copy $1 to local dir with unique_name
      if [[ -f "$1" ]]; then
        :
      elif [[ -d "$1" ]]; then
        echo "Adding directories ('$1') to the replay script is not implemented."
      else
        echo "The input to D8 does not exist: '$1'."
      fi

      input_file="in/${input_counter}_$(basename $1)"
      cp "$1" "$out_dir/$input_file"
      inputs+=("\$SCRIPT_DIR/$input_file")
    fi
    shift
  done
  echo "mkdir -p \"\$SCRIPT_DIR/out/$input_counter\"" \
    >> "$out_dir/replay.sh"
  echo "\$D8_COMMAND ${args[@]} \"--output=\$SCRIPT_DIR/out/$input_counter\" \"${inputs[@]}\"" \
    >> "$out_dir/replay.sh"
}


### MAIN ###

if (( "$#" != 2 )); then
  echo "Usage: $0 <d8-args-script> <output-dir>" >&2
  echo "See docs in source for details." >&2
  exit 1
fi

input_script="$1"
out_dir="$2"

if [[ -d "$out_dir" ]]; then
  rmdir "$out_dir" # make sure to write only to empty out dir
fi
mkdir -p "$out_dir/in"

# write first lines of the replay script
cat > "$out_dir/replay.sh" << EOF
#!/bin/bash
set -e
readonly SCRIPT_DIR=\$(cd "\$(dirname \${BASH_SOURCE[0]})"; pwd)
if [[ -z "\$D8_COMMAND" ]]; then
  echo "Set D8_COMMAND to, e.g. 'java -jar d8|compatdx.jar'" >&2
  exit 1
fi
rm -rf out
EOF

chmod +x "$out_dir/replay.sh"


# process input file

input_counter=1
while IFS=$'\t' read -r -a args; do
  process_line "$out_dir" $input_counter "${args[@]}"
  input_counter=$((input_counter+1))
done <"$input_script"

