package com.igormaznitsa.jjjvm;

import static org.junit.Assert.fail;

public class TestProviderImpl implements JJJVMProvider {

  public Object invoke(final JJJVMKlazz source, final Object obj, final String clazzName, final String methodName, final String methodSignature, final Object[] _arguments) throws Throwable{
    if (!(methodName.equals("<init>") && methodSignature.equals("()V"))) fail("invoke");
    return null;
  }

  public Object allocate(JJJVMKlazz source, String jvmFormattedClassName) throws Throwable{
    fail("allocate");
    return null;
  }

  public Object[] newObjectArray(JJJVMKlazz source, String jvmFormattedClassName, int arrayLength)throws Throwable {
    fail("newObjectArray");
    return null;
  }

  public Object newMultidimensional(JJJVMKlazz source, String jvmFormattedClassName, int[] dimensions) throws Throwable{
    fail("newMultidimensional");
    return null;
  }

  public Object get(JJJVMKlazz source, Object obj, String fieldName, String fieldSignature) throws Throwable{
    fail("get");
    return null;
  }

  public void set(JJJVMKlazz source, Object obj, String fieldName, String fieldSignature, Object value) throws Throwable{
    fail("set");
  }

  public Object getStatic(JJJVMKlazz source, String jvmFormattedClassName,String fieldName, String fieldSignature) throws Throwable{
    fail("getStatic");
    return null;
  }

  public void setStatic(JJJVMKlazz source, String jvmFormattedClassName, String fieldName, String fieldSignature, Object value) throws Throwable{
    fail("setStatic");
  }

  public boolean checkCast(final JJJVMKlazz caller, final String jvmFormattedClassName, final Object value) throws Throwable{
    return true;
  }

  public boolean instanceOf(JJJVMKlazz caller, String jvmFormattedClassName, Object value) throws Throwable{
    fail("instanceOf");
    return false;
  }

  public Object resolveClass(String canonicalClassName) throws Throwable{
    return null;
  }

  public void doThrow(final JJJVMKlazz caller, final Object exception) throws Throwable {
    fail("doThrow");
    throw new Error("Error");
  }

  public void doMonitor(final JJJVMKlazz caller, final Object object, final boolean lock) throws Throwable{
    fail("doMonitor");
  }

  public JJJVMKlazz resolveInnerClass(JJJVMKlazz caller, JJJVMInnerClassRecord innerClassRecord) throws Throwable {
    fail("resolveInnerClass");
    return null;
  }

  public void registerExternalClass(String jvmFormattedClassName, Object clazz) {
  }
  
}
