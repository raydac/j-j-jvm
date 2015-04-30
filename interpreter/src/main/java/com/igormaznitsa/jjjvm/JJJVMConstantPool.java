package com.igormaznitsa.jjjvm;

public interface JJJVMConstantPool {

  JJJVMCPRecord getItem(int asInt);
  JJJVMKlazz getDeclaringClass();
  int size();
}
