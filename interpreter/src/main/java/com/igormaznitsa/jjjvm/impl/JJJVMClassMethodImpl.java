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
package com.igormaznitsa.jjjvm.impl;

import com.igormaznitsa.jjjvm.model.JJJVMClass;
import com.igormaznitsa.jjjvm.model.JJJVMObject;
import com.igormaznitsa.jjjvm.model.JJJVMMethod;
import com.igormaznitsa.jjjvm.model.JJJVMTryCatchRecord;
import com.igormaznitsa.jjjvm.*;
import java.io.DataInputStream;
import java.io.IOException;

public final class JJJVMClassMethodImpl implements JJJVMMethod {

  private final JJJVMClass declaringClass;
  private final int flags;
  private final String name;
  private final String signature;
  private final String[] declaredExceptions;
  private final JJJVMTryCatchRecord[] catchBlocks;
  private final int maxStackDepth;
  private final int maxLocals;
  private final byte[] bytecode;
  private final int[][] lineNumberTable;
  private final boolean clinit;
  
  JJJVMClassMethodImpl(final JJJVMClassImpl declaringClass, final DataInputStream inStream) throws IOException {
    final JJJVMConstantPoolImpl cpool = declaringClass.getConstantPool();

    this.declaringClass = declaringClass;
    this.flags = inStream.readUnsignedShort();
    final int nameIndex = inStream.readUnsignedShort();
    final int descriptorIndex = inStream.readUnsignedShort();
    this.name = cpool.getItemAt(nameIndex).asString();
    this.signature = cpool.getItemAt(descriptorIndex).asString();

    this.clinit = (this.flags & ACC_STATIC)==ACC_STATIC && this.name.equals("<clinit>") && this.signature.equals("()V");
    
    int numberOfAttrs = inStream.readUnsignedShort();

    String[] declExceptions = null;
    int lmaxStackDepth = -1;
    int lmaxLocalVars = -1;
    byte[] lbytecode = null;
    int[][] lineNumbers = null;
    JJJVMTryCatchRecord[] lcatchBlocks = null;

    while (--numberOfAttrs >= 0) {
      final String attrName = cpool.getItemAt(inStream.readUnsignedShort()).asString();
      // read the size of the attribute data
      final int attributeDataLen = inStream.readInt();
      if (ATTRNAME_EXCEPTIONS.equals(attrName)) {
        // read exceptions table for the method i.e. the tail contains exceptions which can be thrown by the method
        final int numberOfExceptions = inStream.readUnsignedShort();
        declExceptions = numberOfExceptions == 0 ? EMPTY_STRING_ARRAY : new String[numberOfExceptions];
        for (int li = 0; li < numberOfExceptions; li++) {
          declExceptions[li] = cpool.getItemAt(inStream.readUnsignedShort()).asString();
        }
      } else {
        if (ATTRNAME_CODE.equals(attrName)) {
          // read the method bytecode and its attributes
          lmaxStackDepth = inStream.readUnsignedShort();
          lmaxLocalVars = inStream.readUnsignedShort();
          lbytecode = new byte[inStream.readInt()];
          inStream.readFully(lbytecode);
          // read the table of exception processors for the bytecode
          final int catchBlockNumber = inStream.readUnsignedShort();
          lcatchBlocks = catchBlockNumber == 0 ? EMPTY_CATCBLOCK_ARRAY : new JJJVMTryCatchRecord[catchBlockNumber];
          for (int li = 0; li < lcatchBlocks.length; li++) {
            lcatchBlocks[li] = new JJJVMTryCatchRecord(cpool, inStream);
          }

          int numberOfAttributes = inStream.readUnsignedShort();
          while (--numberOfAttributes >= 0) {
            final int attrNameIndex = inStream.readUnsignedShort();
            final int attrDataSize = inStream.readInt();
            final String codeAttrName = cpool.getItemAt(attrNameIndex).asString();
            if (ATTRNAME_LINENUMBERTABLE.equals(codeAttrName)) {
              lineNumbers = readLineNumberTable(inStream);
            } else {
              // skip all other attributes in the code attribute
              JJJVMImplUtils.skip(inStream, attrDataSize);
            }
          }
        } else {
          // skip other attribute data
          JJJVMImplUtils.skip(inStream, attributeDataLen);
        }
      }
    }
    // it would be good for us to keep arrays as objects but null
    if (declExceptions == null) {
      declExceptions = new String[0];
    }
    if (lcatchBlocks == null) {
      lcatchBlocks = new JJJVMTryCatchRecord[0];
    }
    this.lineNumberTable = lineNumbers;
    this.declaredExceptions = declExceptions;
    this.catchBlocks = lcatchBlocks;
    this.maxStackDepth = lmaxStackDepth;
    this.maxLocals = lmaxLocalVars;
    this.bytecode = lbytecode;
  }

  @Override
  public boolean isClinit() {
    return this.clinit;
  }

  /**
   * Read table with source line numbers
   * @see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.12
   * @param inStream
   * @return array of pairs code offset : line number
   * @throws Throwable
   */
  private int[][] readLineNumberTable(final DataInputStream inStream) throws IOException {
    final int numberOfRecordsInTable = inStream.readUnsignedShort();
    int[][] result = new int[numberOfRecordsInTable][2];
    for (int i = 0; i < numberOfRecordsInTable; i++) {
      result[i][0] = inStream.readUnsignedShort();
      result[i][1] = inStream.readUnsignedShort();
    }
    return result;
  }

  public int[][] getLineNumberTable() {
      return lineNumberTable;
  }

    public String[] getDeclaredExceptions() {
    return this.declaredExceptions;
  }

  public Object invoke(final JJJVMObject instance, final Object[] arguments) throws Throwable {
    if ((this.flags & ACC_STATIC) == 0 && instance == null) {
      throw new NullPointerException("'this' can't be null for non-static method");
    }
    return JJJVMInterpreter.invoke(this.declaringClass, instance, this, arguments, null, null);
  }

  public JJJVMClass getDeclaringClass() {
    return this.declaringClass;
  }

  public JJJVMTryCatchRecord[] getTryCatchRecords() {
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

  @Override
  public String toString() {
    return this.getClass().getCanonicalName() + '[' + this.declaringClass.getName() + '#' + this.getName() + ' ' + this.getSignature() + ']';
  }
}
