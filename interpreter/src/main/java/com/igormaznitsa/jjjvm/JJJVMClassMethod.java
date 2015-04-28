/* 
 * Copyright 2015 Igor Maznitsa (http://www.igormaznitsa.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.jjjvm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Class contains class method info.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6}
 */
public final class JJJVMClassMethod {
  // flags of a method
  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SYNCHRONIZED = 0x0020;
  public static final int ACC_BRIDGE = 0x0040;
  public static final int ACC_VARARGS = 0x0080;
  public static final int ACC_NATIVE = 0x0100;
  public static final int ACC_ABSTRACT = 0x0400;
  public static final int ACC_STRICT = 0x0800;
  public static final int ACC_SYNTHETIC = 0x1000;
  //-----------------------------------------
  // types of data
  public static final char TYPE_BYTE = 'B';
  public static final char TYPE_CHAR = 'C';
  public static final char TYPE_DOUBLE = 'D';
  public static final char TYPE_FLOAT = 'F';
  public static final char TYPE_INT = 'I';
  public static final char TYPE_LONG = 'J';
  public static final char TYPE_SHORT = 'S';
  public static final char TYPE_BOOLEAN = 'Z';
  public static final char TYPE_VOID = 'V';
  public static final char TYPE_CLASS = 'L';
  public static final char TYPE_ARRAY = '[';
  //-----------------------------------------
  private static final String ATTRIBUTE_EXCEPTIONS = "Exceptions";
  private static final String ATTRIBUTE_CODE = "Code";
  //-----------------------------------------
  private  final JJJVMClass declaringClass;
  private final int flags;
  private final String name;
  private final String signature;
  private final String[] declaredExceptions;
  private final JJJVMCatchBlockDescriptor[] catchBlocks;
  private final int maxStackDepth;
  private final int maxLocals;
  private final byte[] bytecode;
  
  JJJVMClassMethod(final JJJVMClass declaringClass, final DataInputStream inStream) throws IOException {
    final JJJVMConstantPool cpool = declaringClass.getConstantPool();
    
    this.declaringClass = declaringClass;
    this.flags = inStream.readUnsignedShort();
    final int nameIndex = inStream.readUnsignedShort();
    final int descriptorIndex = inStream.readUnsignedShort();
    this.name = cpool.get(nameIndex).asString();
    this.signature = cpool.get(descriptorIndex).asString();
    
    int numberOfAttrs = inStream.readUnsignedShort();
    
    String [] declExceptions = null;
    int lmaxStackDepth = -1;
    int lmaxLocalVars = -1;
    byte [] lbytecode = null;
    JJJVMCatchBlockDescriptor [] lcatchBlocks = null;
    
    while (--numberOfAttrs >= 0) {
      final String attrName = cpool.get(inStream.readUnsignedShort()).asString();
      // read the size of the attribute data
      final int attributeDataLen = inStream.readInt();
      if (ATTRIBUTE_EXCEPTIONS.equals(attrName)) {
        // read exceptions table for the method i.e. the tail contains exceptions which can be thrown by the method
        final int numberOfExceptions = inStream.readUnsignedShort();
        declExceptions = new String[numberOfExceptions];
        for (int li = 0; li < numberOfExceptions; li++) {
          declExceptions[li] = cpool.get(inStream.readUnsignedShort()).asString();
        }
      }
      else {
        if (ATTRIBUTE_CODE.equals(attrName)) {
          // read the method bytecode and its attributes
          lmaxStackDepth= inStream.readUnsignedShort();
          lmaxLocalVars = inStream.readUnsignedShort();
          lbytecode = new byte[inStream.readInt()];
          inStream.readFully(lbytecode);
          // read the table of exception processors for the bytecode
          lcatchBlocks = new JJJVMCatchBlockDescriptor[inStream.readUnsignedShort()];
          for (int li = 0; li < lcatchBlocks.length; li++) {
            lcatchBlocks[li] = new JJJVMCatchBlockDescriptor(cpool, inStream);
          }
          // skip all other attributes in the code attribute
          JJJVMClass.skipAllAttributes(inStream);
        }
        else {
          // skip other attribute data
          inStream.skipBytes(attributeDataLen);
        }
      }
    }
    // it would be good for us to keep arrays as objects but null
    if (declExceptions == null) {
      declExceptions = new String[0];
    }
    if (lcatchBlocks == null) {
      lcatchBlocks = new JJJVMCatchBlockDescriptor[0];
    }
    
    this.declaredExceptions = declExceptions;
    this.catchBlocks = lcatchBlocks;
    this.maxStackDepth = lmaxStackDepth;
    this.maxLocals = lmaxLocalVars;
    this.bytecode = lbytecode;
  } 

  public Object invoke(final JJJVMObject instance, final Object[] arguments) throws Throwable {
    return this.declaringClass.invoke(instance, this, arguments, null, null);
  }
  
  public JJJVMClass getDeclaringClass(){
    return this.declaringClass;
  }
  
  public JJJVMCatchBlockDescriptor [] getCatchBlockDescriptors() {
    return this.catchBlocks;
  }

  public String getName() {
    return this.name;
  }

  public String getSignature() {
    return this.signature;
  }

  public int getFlags() {
    return this.flags;
  }

  public int getMaxStackDepth() {
    return this.maxStackDepth;
  }

  public int getMaxLocals() {
    return this.maxLocals;
  }

  public byte[] getBytecode() {
    return this.bytecode;
  }
}
