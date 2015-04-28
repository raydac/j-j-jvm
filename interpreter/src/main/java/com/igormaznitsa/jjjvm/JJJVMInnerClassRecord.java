package com.igormaznitsa.jjjvm;

import java.io.DataInputStream;
import java.io.IOException;

public final class JJJVMInnerClassRecord {
  private final JJJVMClass declaring;
  private final int innerClassInfoIndex;
  private final int outerClassInfoIndex;
  private final int innerNameIndex;
  private final int flags;
  
  public JJJVMInnerClassRecord(final JJJVMClass declaring, final DataInputStream inStream) throws IOException {
    this.declaring = declaring;
    this.innerClassInfoIndex = inStream.readUnsignedShort();
    this.outerClassInfoIndex = inStream.readUnsignedShort();
    this.innerNameIndex = inStream.readUnsignedShort();
    this.flags = inStream.readUnsignedShort();
  }
  
  public int getFlags(){
    return this.flags;
  }
  
  public JJJVMConstantPool.Record getInnerClassInfo(){
    return this.declaring.getConstantPool().get(this.innerClassInfoIndex);
  }
  
  public JJJVMConstantPool.Record getOuterClassInfo(){
    return this.declaring.getConstantPool().get(this.outerClassInfoIndex);
  }
  
  public String getName(){
    return (String)this.declaring.getConstantPool().get(this.innerNameIndex).asObject();
  }
}
