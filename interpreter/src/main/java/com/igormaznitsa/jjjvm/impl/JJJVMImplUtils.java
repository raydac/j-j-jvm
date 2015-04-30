package com.igormaznitsa.jjjvm.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class JJJVMImplUtils {
  private JJJVMImplUtils(){}
  
  public static void makeAccessible(final AccessibleObject obj){
    if (obj!=null && obj.isAccessible()){
      AccessController.doPrivileged(new PrivilegedAction(){
        public Object run() {
          obj.setAccessible(true);
          return null;
        }
      });
    }
  }
  
  public static void skip(final DataInputStream stream, final int bytesToSkip) throws IOException {
    if (stream.skipBytes(bytesToSkip)!=bytesToSkip) throw new IOException("Can't skip "+bytesToSkip+" byte(s)");
  }
}
