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
package com.igormaznitsa.jjjvm.model;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Structure describes an inner class.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.6}
 */
public class JJJVMInnerClassRecord {

  /**
   * The Declaring class.
   */
  protected final JJJVMClass declaringClass;
  /**
   * The Index in the declaring class constant pool of the inner class info.
   */
  protected final int innerClassInfoIndex;
  /**
   * The Index in the declaring class constant pool of the outer class info. it
   * can be 0.
   */
  protected final int outerClassInfoIndex;
  /**
   * The Inner class name index in the declaring class constant pool.
   */
  protected final int innerNameIndex;
  /**
   * Flags of the inner class.
   */
  protected final int flags;

  public JJJVMInnerClassRecord(final JJJVMClass declaring, final int innerClassInfoIndex, final int outerClassInfoIndex, final int innerNameIndex, final int flags) {
    this.declaringClass = declaring;
    this.innerClassInfoIndex = innerClassInfoIndex;
    this.outerClassInfoIndex = outerClassInfoIndex;
    this.innerNameIndex = innerNameIndex;
    this.flags = flags;
  }

  public JJJVMInnerClassRecord(final JJJVMClass declaring, final DataInputStream inStream) throws IOException {
    this(declaring, inStream.readUnsignedShort(), inStream.readUnsignedShort(), inStream.readUnsignedShort(), inStream.readUnsignedShort());
  }

  public int getFlags() {
    return this.flags;
  }

  public JJJVMConstantPoolItem getInnerClassInfo() {
    return this.declaringClass.getConstantPool().getItemAt(this.innerClassInfoIndex);
  }

  public JJJVMConstantPoolItem getOuterClassInfo() {
    return this.declaringClass.getConstantPool().getItemAt(this.outerClassInfoIndex);
  }

  public String getName() {
    return (String) this.declaringClass.getConstantPool().getItemAt(this.innerNameIndex).asObject();
  }
}
