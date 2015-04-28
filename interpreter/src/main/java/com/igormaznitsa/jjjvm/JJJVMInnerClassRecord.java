package com.igormaznitsa.jjjvm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Structure describes an inner class.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.6}
 */
public final class JJJVMInnerClassRecord {
  /**
   * The Declaring class.
   */
  private final JJJVMClass declaringClass;
  /**
   * The Index in the declaring class constant pool of the inner class info.
   */
  private final int innerClassInfoIndex;
  /**
   * The Index in the declaring class constant pool of the outer class info. it can be 0.
   */
  private final int outerClassInfoIndex;
  /**
   * The Inner class name index in the declaring class constant pool.
   */
  private final int innerNameIndex;
  /**
   * Flags of the inner class.
   */
  private final int flags;
  
  JJJVMInnerClassRecord(final JJJVMClass declaring, final DataInputStream inStream) throws IOException {
    this.declaringClass = declaring;
    this.innerClassInfoIndex = inStream.readUnsignedShort();
    this.outerClassInfoIndex = inStream.readUnsignedShort();
    this.innerNameIndex = inStream.readUnsignedShort();
    this.flags = inStream.readUnsignedShort();
  }
  
  public int getFlags(){
    return this.flags;
  }
  
  public JJJVMConstantPool.Record getInnerClassInfo(){
    return this.declaringClass.getConstantPool().get(this.innerClassInfoIndex);
  }
  
  public JJJVMConstantPool.Record getOuterClassInfo(){
    return this.declaringClass.getConstantPool().get(this.outerClassInfoIndex);
  }
  
  public String getName(){
    return (String)this.declaringClass.getConstantPool().get(this.innerNameIndex).asObject();
  }
}
