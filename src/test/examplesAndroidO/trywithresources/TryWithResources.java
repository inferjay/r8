// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package trywithresources;

import java.io.Closeable;
import java.io.IOException;

public abstract class TryWithResources {
  // --- TEST SUPPORT ---

  interface Test {
    void test() throws Throwable;
  }

  private void test(Test test) {
    try {
      test.test();
    } catch (Throwable e) {
      dumpException(e);
    }
  }

  private void dumpException(Throwable e) {
    dumpException(e, "Exception: ");
  }

  private void dumpException(Throwable e, String indent) {
    assert e != null;
    System.out.println(indent + e.getMessage());

    indent = indent.replaceAll("[^:]", " ");

    Throwable cause = e.getCause();
    if (cause != null) {
      dumpException(cause, indent + "  cause: ");
    }

    // Dump suppressed UNLESS it is a desugared code running
    // on JVM, in which case we avoid dumping suppressed, since
    // the output will be used for comparison with desugared code
    // running on device.
    if (!desugaredCodeRunningOnJvm()) {
      Throwable[] suppressed = e.getSuppressed();
      for (int i = 0; i < suppressed.length; i++) {
        dumpException(suppressed[i], indent + "supp[" + i + "]: ");
      }
    }
  }

  abstract boolean desugaredCodeRunningOnJvm();

  // --- TEST SYMBOLS ---

  static class Resource implements Closeable {
    final String tag;

    Resource(String tag) {
      this.tag = tag;
    }

    @Override
    public void close() throws IOException {
      Class<? extends Resource> cls = this.getClass();
      System.out.println("Closing " + tag + " (" +
          cls.getName().substring(TryWithResources.class.getName().length() + 1) + ")");
    }
  }

  // --- TEST ---

  class RegularTryWithResources {
    class RegularResource extends Resource {
      RegularResource(String tag) {
        super(tag);
      }
    }

    private void test() throws Throwable {
      test(2);
    }

    private void test(int level) throws Throwable {
      try (RegularResource a = new RegularResource("a" + level);
           RegularResource b = new RegularResource("b" + level)) {
        if (level > 0) {
          try {
            test(level - 1);
          } catch (Throwable e) {
            throw new RuntimeException("e" + level, e);
          }
        }
        throw new RuntimeException("primary cause");
      }
    }
  }

  // --- TEST ---

  class FailingTryWithResources {
    class FailingResource extends Resource {
      FailingResource(String tag) {
        super(tag);
      }

      @Override
      public void close() throws IOException {
        super.close();
        throw new RuntimeException("failed to close '" + tag + "'");
      }
    }

    private void test() throws Throwable {
      test(2);
    }

    private void test(int level) throws Throwable {
      try (FailingResource a = new FailingResource("a" + level);
           FailingResource b = new FailingResource("b" + level)) {
        if (level > 0) {
          try {
            test(level - 1);
          } catch (Throwable e) {
            throw new RuntimeException("e" + level, e);
          }
        }
        throw new RuntimeException("primary cause");
      }
    }
  }

  // --- TEST ---

  class ExplicitAddGetSuppressed {
    class RegularResource extends Resource {
      RegularResource(String tag) {
        super(tag);
      }

      @Override
      public void close() throws IOException {
        super.close();
        throw new RuntimeException("failed to close '" + tag + "'");
      }
    }

    private void test() throws Throwable {
      test(2);
    }

    private void test(int level) throws RuntimeException {
      try (RegularResource a = new RegularResource("a" + level);
           RegularResource b = new RegularResource("b" + level)) {
        if (level > 0) {
          try {
            test(level - 1);
          } catch (RuntimeException e) {
            // Just collect suppressed, but throw away the exception.
            RuntimeException re = new RuntimeException("e" + level);
            for (Throwable suppressed : e.getSuppressed()) {
              re.addSuppressed(suppressed);
            }
            throw re;
          }
        }
        throw new RuntimeException("primary cause");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // --- TEST ---

  interface Consumer {
    void act(RuntimeException re);
  }

  interface Supplier {
    Throwable[] get();
  }

  class AddGetSuppressedRoundTrip {
    private void test() throws Throwable {
      RuntimeException carrier = new RuntimeException("carrier");
      Consumer packer = carrier::addSuppressed;
      Supplier unpacker = carrier::getSuppressed;

      packer.act(new RuntimeException("original exception A"));
      packer.act(new RuntimeException("original exception Z"));

      for (Throwable unpacked : unpacker.get()) {
        if (!desugaredCodeRunningOnJvm()) {
          dumpException(unpacked);
        }
      }
    }
  }

  // --- TEST ---

  class UnreachableCatchAfterCallsRemoved {
    private void test() throws Throwable {
      RuntimeException main = new RuntimeException("main");
      RuntimeException origA = new RuntimeException("original exception A");
      RuntimeException origB = new RuntimeException("original exception Z");

      try {
        // After both calls below are removed, the whole catch
        // handler should be removed.
        main.addSuppressed(origA);
        main.addSuppressed(origB);
      } catch (Throwable t) {
        throw new RuntimeException("UNREACHABLE");
      }

      // Return value not used.
      main.getSuppressed();
    }
  }

  // --- MAIN TEST ---

  void test() throws Exception {
    System.out.println("----- TEST 1 -----");
    test(new RegularTryWithResources()::test);
    System.out.println("----- TEST 2 -----");
    test(new FailingTryWithResources()::test);
    System.out.println("----- TEST 3 -----");
    test(new ExplicitAddGetSuppressed()::test);
    System.out.println("----- TEST 4 -----");
    test(new AddGetSuppressedRoundTrip()::test);
    System.out.println("----- TEST 5 -----");
    test(new UnreachableCatchAfterCallsRemoved()::test);
    System.out.println("------------------");
  }
}
