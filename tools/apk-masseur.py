#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import glob
import optparse
import os
import shutil
import subprocess
import sys
import utils

USAGE = 'usage: %prog [options] <apk>'

def parse_options():
  parser = optparse.OptionParser(usage=USAGE)
  parser.add_option('--dex',
                    help='directory with dex files to use instead of those in the apk',
                    default=None)
  parser.add_option('--out',
                    help='output file (default ./$(basename <apk>))',
                    default=None)
  parser.add_option('--keystore',
                    help='keystore file (default ~/.android/app.keystore)',
                    default=None)
  parser.add_option('--install',
                    help='install the generated apk with adb options -t -r -d',
                    default=False,
                    action='store_true')
  parser.add_option('--adb-options',
                    help='additional adb options when running adb',
                    default=None)
  (options, args) = parser.parse_args()
  if len(args) != 1:
    parser.error('Expected <apk> argument, got: ' + ' '.join(args))
  apk = args[0]
  if not options.out:
    options.out = os.path.basename(apk)
  if not options.keystore:
    options.keystore = findKeystore()
  return (options, apk)

def findKeystore():
  return os.path.join(os.getenv('HOME'), '.android', 'app.keystore')

def repack(processed_out, original_apk, temp):
  processed_apk = os.path.join(temp, 'processed.apk')
  shutil.copyfile(original_apk, processed_apk)
  if not processed_out:
    print 'Using original APK as is'
    return processed_apk
  print 'Repacking APK with dex files from', processed_apk
  with utils.ChangedWorkingDirectory(temp):
    cmd = ['zip', '-d', 'processed.apk', '*.dex']
    utils.PrintCmd(cmd)
    subprocess.check_call(cmd)
  if processed_out.endswith('.zip') or processed_out.endswith('.jar'):
    cmd = ['unzip', processed_out, '-d', temp]
    utils.PrintCmd(cmd)
    subprocess.check_call(cmd)
    processed_out = temp
  with utils.ChangedWorkingDirectory(processed_out):
    dex = glob.glob('*.dex')
    cmd = ['zip', '-u', '-9', processed_apk] + dex
    utils.PrintCmd(cmd)
    subprocess.check_call(cmd)
  return processed_apk

def sign(unsigned_apk, keystore, temp):
  print 'Signing (ignore the warnings)'
  cmd = ['zip', '-d', unsigned_apk, 'META-INF/*']
  utils.PrintCmd(cmd)
  subprocess.call(cmd)
  signed_apk = os.path.join(temp, 'unaligned.apk')
  cmd = [
    'jarsigner',
    '-sigalg', 'SHA1withRSA',
    '-digestalg', 'SHA1',
    '-keystore', keystore,
    '-storepass', 'android',
    '-signedjar', signed_apk,
    unsigned_apk,
    'androiddebugkey'
  ]
  utils.PrintCmd(cmd)
  subprocess.check_call(cmd)
  return signed_apk

def align(signed_apk, temp):
  print 'Aligning'
  aligned_apk = os.path.join(temp, 'aligned.apk')
  cmd = ['zipalign', '-f', '4', signed_apk, aligned_apk]
  print ' '.join(cmd)
  subprocess.check_call(cmd)
  return signed_apk

def main():
  (options, apk) = parse_options()
  with utils.TempDir() as temp:
    processed_apk = None
    if options.dex:
      processed_apk = repack(options.dex, apk, temp)
    else:
      print 'Signing original APK without modifying dex files'
      processed_apk = os.path.join(temp, 'processed.apk')
      shutil.copyfile(apk, processed_apk)
    signed_apk = sign(processed_apk, options.keystore, temp)
    aligned_apk = align(signed_apk, temp)
    print 'Writing result to', options.out
    shutil.copyfile(aligned_apk, options.out)
    adb_cmd = ['adb']
    if options.adb_options:
      adb_cmd.extend(
          [option for option in options.adb_options.split(' ') if option])
    if options.install:
      adb_cmd.extend(['install', '-t', '-r', '-d', options.out]);
      utils.PrintCmd(adb_cmd)
      subprocess.check_call(adb_cmd)
  return 0

if __name__ == '__main__':
  sys.exit(main())
