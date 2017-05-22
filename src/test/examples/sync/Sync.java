// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package sync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Sync {

  public static final int THREADS = 10;
  public static final int ITERATIONS = 10;

  private static final int INITIAL_SHARED_STATE = -1;
  private static final long SLEEP = 10;

  // Shared mutable state that is tested to be consistent
  private static int sharedState = INITIAL_SHARED_STATE;
  private static boolean shouldThrow = false;

  // Copy of interface java.util.function.Consumer to make this test work without a Java 8 runtime
  // library
  public interface Consumer<T> {

    void accept(T t);
  }

  public static void couldThrow(int index) {
    if (shouldThrow) throw new RuntimeException();
    // Copy shared state and trash it (we set our index).
    int local = sharedState;
    sharedState = index;
    try {
      Thread.sleep(SLEEP);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    // Restore the shared state if it is still valid.
    if (sharedState == index) {
      sharedState = local;
    }
  }

  public static synchronized void staticSynchronized(int index) {
    System.out.println("static");
    couldThrow(index);
    System.out.println("end");
  }

  public synchronized void instanceSynchronized(int index) {
    System.out.println("instance");
    couldThrow(index);
    System.out.println("end");
  }

  public void manualSynchronized(int index) {
    System.out.println("manual");
    synchronized (this) {
      couldThrow(index);
    }
    System.out.println("manual");
  }

  public synchronized void tryCatchSynchronized(int index) {
    System.out.println("trycatch");
    try {
      couldThrow(index);
      try {
        couldThrow(index);
      } finally {
        System.out.println("end");
        return;
      }
    } catch (RuntimeException e) {
      System.out.println("caught & end");
      return;
    } catch (Throwable e) {
      System.out.println("caught other");
    }
    System.out.println("end");
  }

  public static synchronized void throwStaticSynchronized() {
    throw new RuntimeException();
  }

  public synchronized void throwInstanceSynchronized() {
    throw new RuntimeException();
  }

  public static void run(ExecutorService service, final Consumer<Integer> fn)
      throws ExecutionException, InterruptedException {
    Future[] results = new Future[ITERATIONS];
    for (int i = 0; i < ITERATIONS; ++i) {
      final int index = i;
      results[i] = service.submit(new Runnable() {
        @Override
        public void run() {
          fn.accept(index);
        }
      });
    }
    for (Future result : results) {
      result.get();
    }
    if (sharedState != INITIAL_SHARED_STATE) {
      throw new RuntimeException("Synchronization error!");
    }
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    shouldThrow = args.length > 100;
    ExecutorService service = Executors.newFixedThreadPool(THREADS);
    run(service, new Consumer<Integer>() {
      @Override
      public void accept(Integer index) {
         Sync.staticSynchronized(index);
      }
    });
    final Sync sync = new Sync();
    run(service, new Consumer<Integer>() {
      @Override
      public void accept(Integer index) {
        sync.instanceSynchronized(index);
      }
    });
    run(service, new Consumer<Integer>() {
      @Override
      public void accept(Integer index) {
        sync.manualSynchronized(index);
      }
    });
    run(service, new Consumer<Integer>() {
      @Override
      public void accept(Integer index) {
        sync.tryCatchSynchronized(index);
      }
    });
    service.shutdown();
    service.awaitTermination(5, TimeUnit.SECONDS);
    try {
      Sync.throwStaticSynchronized();
      throw new Error("expected throw");
    } catch (RuntimeException e) {
      System.out.println("caught throw");
    }
    try {
      sync.throwInstanceSynchronized();
      throw new Error("expected throw");
    } catch (RuntimeException e) {
      System.out.println("caught throw");
    }
  }
}
