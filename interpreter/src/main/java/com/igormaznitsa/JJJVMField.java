package com.igormaznitsa;

import com.igormaznitsa.jjjvm.JJJVMKlazz;
import com.igormaznitsa.jjjvm.JJJVMObject;

public interface JJJVMField {
  
  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_VOLATILE = 0x0040;
  public static final int ACC_TRANSIENT = 0x0080;
  public static final int ACC_SYNTHETIC = 0x1000;
  public static final int ACC_ENUM = 0x4000;

  //---------------------------
  public static final String ATTRIBUTE_CONSTANTVALUE = "ConstantValue";
  //---------------------------
  JJJVMKlazz getDeclaringClass();
  int getFlags();
  String getName();
  String getSignature();

  public Object getConstantValue();

  public Object getStaticValue();

  public void setStaticValue(Object localMethodStack);

  public void set(JJJVMObject jjjobj, Object fieldValue);

  public Object get(JJJVMObject jjjobj);
  
}
