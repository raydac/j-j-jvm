package com.igormaznitsa.jjjvm;

import com.igormaznitsa.JJJVMField;
import java.util.Map;

public interface JJJVMKlazz {

  public JJJVMMethod findDeclaredMethod(String methodName, String methodSignature);
  public JJJVMField findDeclaredField(String fieldName);
  public JJJVMMethod findMethod(String methodName, String methodSignature) throws Throwable;
  public JJJVMConstantPool getConstantPool();
  public JJJVMField findField(String fieldName) throws Throwable;
  public JJJVMProvider getProvider();
  public Map<String, JJJVMField> getDeclaredFields();
  public Map<String, JJJVMMethod> getDeclaredMethods();

  public String [] getInterfaces();

  public Object resolveSuperclass() throws Throwable;


  public int getClassFormatVersion();

  public String getName();
  public String getClassName();
  public String getCanonicalName();

  public String getSourceFile();

  public Object readStaticField(String fieldName) throws Throwable;
  public void writeStaticField(String fieldName, Object value) throws Throwable;
  
  public JJJVMObject newInstance(boolean callDefaultConstructor) throws Throwable;
  public JJJVMObject newInstance(String constructorSignature, Object[] args, Object [] stack, Object [] localVariables) throws Throwable;
  
}
