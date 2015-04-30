package com.igormaznitsa.jjjvm;

import com.igormaznitsa.jjjvm.impl.JJJVMClassImpl;
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
  private final JJJVMKlazz declaringClass;
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
  
  public JJJVMInnerClassRecord(final JJJVMKlazz declaring, final int innerClassInfoIndex, final int outerClassInfoIndex, final int innerNameIndex, final int flags){
    this.declaringClass = declaring;
    this.innerClassInfoIndex = innerClassInfoIndex;
    this.outerClassInfoIndex = outerClassInfoIndex;
    this.innerNameIndex = innerNameIndex;
    this.flags = flags;
  }
  
  public JJJVMInnerClassRecord(final JJJVMKlazz declaring, final DataInputStream inStream) throws IOException {
    this(declaring, inStream.readUnsignedShort(), inStream.readUnsignedShort(), inStream.readUnsignedShort(), inStream.readUnsignedShort());
  }
  
  public int getFlags(){
    return this.flags;
  }
  
  public JJJVMCPRecord getInnerClassInfo(){
    return this.declaringClass.getConstantPool().getItem(this.innerClassInfoIndex);
  }
  
  public JJJVMCPRecord getOuterClassInfo(){
    return this.declaringClass.getConstantPool().getItem(this.outerClassInfoIndex);
  }
  
  public String getName(){
    return (String)this.declaringClass.getConstantPool().getItem(this.innerNameIndex).asObject();
  }
}
