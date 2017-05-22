// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {

  public static <T> List<T> awaitFutures(List<? extends Future<? extends T>> futures)
      throws ExecutionException {
    ArrayList<T> result = new ArrayList<>(futures.size());
    for (Future<? extends T> f : futures) {
      try {
        result.add(f.get());
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while waiting for future.", e);
      }
    }
    return result;
  }

  public static ExecutorService getExecutorService(int threads) {
    if (threads == 1) {
      return Executors.newSingleThreadExecutor();
    } else {
      return Executors.newWorkStealingPool(threads);
    }
  }

  public static ExecutorService getExecutorService(InternalOptions options) {
    if (options.numberOfThreads == options.NOT_SPECIFIED) {
      // This heuristic is based on measurements on a 32 core (hyper-threaded) machine.
      int threads = Integer.min(Runtime.getRuntime().availableProcessors(), 16) / 2;
      return Executors.newWorkStealingPool(threads);
    } else if (options.numberOfThreads == 1) {
      return Executors.newSingleThreadExecutor();
    } else {
      return Executors.newWorkStealingPool(options.numberOfThreads);
    }
  }
}
