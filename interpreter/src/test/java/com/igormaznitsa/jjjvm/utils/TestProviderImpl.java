package com.igormaznitsa.jjjvm.utils;

import com.igormaznitsa.jjjvm.model.JJJVMInnerClassRecord;
import com.igormaznitsa.jjjvm.model.JJJVMClass;
import com.igormaznitsa.jjjvm.model.JJJVMProvider;
import static org.junit.Assert.fail;

public class TestProviderImpl implements JJJVMProvider {

  public Object invoke(final JJJVMClass source, final Object obj, final String clazzName, final String methodName, final String methodSignature, final Object[] _arguments) throws Throwable{
    if (!(methodName.equals("<init>") && methodSignature.equals("()V"))) fail("invoke");
    return null;
  }

  public Object allocate(JJJVMClass source, String jvmFormattedClassName) throws Throwable{
    fail("allocate");
    return null;
  }

  public Object[] newObjectArray(JJJVMClass source, String jvmFormattedClassName, int arrayLength)throws Throwable {
    fail("newObjectArray");
    return null;
  }

  public Object newMultidimensional(JJJVMClass source, String jvmFormattedClassName, int[] dimensions) throws Throwable{
    fail("newMultidimensional");
    return null;
  }

  public Object get(JJJVMClass source, Object obj, String fieldName, String fieldSignature) throws Throwable{
    fail("get");
    return null;
  }

  public void set(JJJVMClass source, Object obj, String fieldName, String fieldSignature, Object value) throws Throwable{
    fail("set");
  }

  public Object getStatic(JJJVMClass source, String jvmFormattedClassName,String fieldName, String fieldSignature) throws Throwable{
    fail("getStatic");
    return null;
  }

  public void setStatic(JJJVMClass source, String jvmFormattedClassName, String fieldName, String fieldSignature, Object value) throws Throwable{
    fail("setStatic");
  }

  public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) throws Throwable{
    return true;
  }

  public boolean instanceOf(JJJVMClass caller, String jvmFormattedClassName, Object value) throws Throwable{
    fail("instanceOf");
    return false;
  }

  public Object resolveClass(String canonicalClassName) throws Throwable{
    return null;
  }

  public void doThrow(final JJJVMClass caller, final Object exception) throws Throwable {
    fail("doThrow");
    throw new Error("Error");
  }

  public void doMonitor(final JJJVMClass caller, final Object object, final boolean lock) throws Throwable{
    fail("doMonitor");
  }

  public JJJVMClass resolveInnerClass(JJJVMClass caller, JJJVMInnerClassRecord innerClassRecord) throws Throwable {
    fail("resolveInnerClass");
    return null;
  }

  public void registerExternalClass(String jvmFormattedClassName, Object clazz) {
  }
  
}
