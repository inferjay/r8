// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.conversion.JarSourceCode;
import com.android.tools.r8.jar.JarRegisterEffectsVisitor;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.IdentityHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class JarCode extends Code {

  public static class ReparseContext {

    // This will hold the content of the whole class. Once all the methods of the class are swapped
    // from this to the actual JarCode, no other references would be left and the content can be
    // GC'd.
    public byte[] classCache;
    public DexProgramClass owner;
    private IdentityHashMap<DexMethod, JarCode> lookupMap = new IdentityHashMap<>();
  }

  private final DexType clazz;
  private MethodNode node;
  private ReparseContext context;

  private final JarApplicationReader application;

  public JarCode(DexMethod method, ReparseContext context, JarApplicationReader application) {
    this.clazz = method.getHolder();
    this.context = context;
    this.application = application;
    context.lookupMap.put(method, this);
  }

  @Override
  public boolean isJarCode() {
    return true;
  }

  @Override
  public JarCode asJarCode() {
    return this;
  }

  @Override
  protected int computeHashCode() {
    triggerDelayedParsingIfNeccessary();
    return node.hashCode();
  }

  @Override
  protected boolean computeEquals(Object other) {
    triggerDelayedParsingIfNeccessary();
    if (this == other) {
      return true;
    }
    if (other instanceof JarCode) {
      JarCode o = (JarCode) other;
      o.triggerDelayedParsingIfNeccessary();
      // TODO(zerny): This amounts to object equality.
      return node.equals(o.node);
    }
    return false;
  }

  @Override
  public IRCode buildIR(DexEncodedMethod encodedMethod, InternalOptions options) {
    triggerDelayedParsingIfNeccessary();
    JarSourceCode source = new JarSourceCode(clazz, node, application);
    IRBuilder builder = new IRBuilder(encodedMethod, source, options);
    return builder.build();
  }

  public IRCode buildIR(DexEncodedMethod encodedMethod, ValueNumberGenerator generator,
      InternalOptions options) {
    triggerDelayedParsingIfNeccessary();
    JarSourceCode source = new JarSourceCode(clazz, node, application);
    IRBuilder builder = new IRBuilder(encodedMethod, source, generator, options);
    return builder.build();
  }


  @Override
  public void registerReachableDefinitions(UseRegistry registry) {
    triggerDelayedParsingIfNeccessary();
    node.instructions.accept(new JarRegisterEffectsVisitor(clazz, registry, application));
  }

  @Override
  public String toString() {
    triggerDelayedParsingIfNeccessary();
    TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
    node.accept(visitor);
    StringWriter writer = new StringWriter();
    visitor.p.print(new PrintWriter(writer));
    return writer.toString();
  }

  @Override
  public String toString(DexEncodedMethod method, ClassNameMapper naming) {
    return toString();
  }

  private void triggerDelayedParsingIfNeccessary() {
    if (context != null) {
      // The SecondVistor is in charge of setting the context to null.
      DexProgramClass owner = context.owner;
      new ClassReader(context.classCache).accept(new SecondVisitor(context, application),
          ClassReader.SKIP_FRAMES);
      assert verifyNoReparseContext(owner);
    }
  }

  /**
   * Fills the MethodNodes of all the methods in the class and removes the ReparseContext.
   */
  private static class SecondVisitor extends ClassVisitor {

    private final ReparseContext context;
    private final JarApplicationReader application;

    public SecondVisitor(ReparseContext context, JarApplicationReader application) {
      super(Opcodes.ASM5);
      this.context = context;
      this.application = application;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
        String[] exceptions) {
      MethodNode node = new JSRInlinerAdapter(null, access, name, desc, signature, exceptions);
      JarCode code = context.lookupMap.get(application.getMethod(context.owner.type, name, desc));
      if (code != null) {
        code.context = null;
        code.node = node;
        return node;
      }
      return null;
    }
  }

  private static boolean verifyNoReparseContext(DexProgramClass owner) {
    for (DexEncodedMethod method : owner.virtualMethods()) {
      Code code = method.getCode();
      if (code != null && code.isJarCode()) {
        if (code.asJarCode().context != null) {
          return false;
        }
      }
    }

    for (DexEncodedMethod method : owner.directMethods()) {
      Code code = method.getCode();
      if (code != null && code.isJarCode()) {
        if (code.asJarCode().context != null) {
          return false;
        }
      }
    }
    return true;
  }
}
