package com.igormaznitsa.jjjvm.utils;

import org.apache.bcel.generic.*;

public class Branch {
  private final Class<? extends BranchInstruction> klazz;
  private final int jumpIndex;
  private final BranchInstruction instance;
  
  public Branch(final Class<? extends BranchInstruction> klazz, final int jumpCommandIndex) throws Exception{
    this.klazz = klazz;
    this.instance = klazz.getConstructor(InstructionHandle.class).newInstance((Object) null);
    this.jumpIndex = jumpCommandIndex;
  }
  
  public BranchInstruction getInstance(){
    return this.instance;
  }
  
  public void update(final InstructionList list){
    final InstructionHandle handle = list.getInstructionHandles()[this.jumpIndex];
    this.instance.setTarget(handle);
  }
}
