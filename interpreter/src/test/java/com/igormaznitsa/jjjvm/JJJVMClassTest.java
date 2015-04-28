package com.igormaznitsa.jjjvm;

import static com.igormaznitsa.jjjvm.TestHelper.CONST_CLASS;
import com.igormaznitsa.jjjvm.impl.JSEProviderImpl;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.bcel.generic.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JJJVMClassTest extends TestHelper implements JSEProviderImpl.ClassLoader {
  public byte[] loadClass(String className) throws IOException, ClassNotFoundException {
    try {
      return TestHelper.loadClassBodyFromClassPath(className);
    }
    catch (ClassNotFoundException ex) {
      throw ex;
    }
    catch (Throwable thr) {
      throw new IOException("Error", thr);
    }
  }

  @Test
  public void test_NOP_NULL_ARETURN_null() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new NOP(), new ACONST_NULL(), new ARETURN());
    assertNull(executeTestMethod(test, Object.class, null, new Object()));
  }

  @Test
  public void test_ICONSTM1_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(-1), new IRETURN());
    assertEquals(-1, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_ICONST5_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(5), new IRETURN());
    assertEquals(5, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_FCONST0_FRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FCONST(0.0f), new FRETURN());
    assertEquals(0.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_FCONST1_FRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FCONST(1.0f), new FRETURN());
    assertEquals(1.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_FCONST2_FRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FCONST(2.0f), new FRETURN());
    assertEquals(2.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_DCONST0_DRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DCONST(0.0d), new DRETURN());
    assertEquals(0.0d, executeTestMethod(test, Double.class, null, 0.0d), 0.0d);
  }

  @Test
  public void test_DCONST1_DRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DRETURN());
    assertEquals(1.0d, executeTestMethod(test, Double.class, null, 0.0d), 0.0d);
  }

  @Test
  public void test_LCONST1_LRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LCONST(1), new LRETURN());
    assertEquals(1L, executeTestMethod(test, Long.class, null, 0L).longValue());
  }

  @Test
  public void test_BIPUSH_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new BIPUSH((byte) -76), new IRETURN());
    assertEquals(-76, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_SIPUSH_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new SIPUSH((short) -763), new IRETURN());
    assertEquals(-763, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_LDC_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LDC(CP_INT), new IRETURN());
    assertEquals(CONST_INT, executeTestMethod(test, Integer.class, null, 0).intValue());

    final JJJVMClass test2 = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LDC(CP_INT_H), new IRETURN());
    assertEquals(CONST_INT_H, executeTestMethod(test2, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_LDCW_ARETURN_string() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new LDC_W(CP_STRING), new ARETURN());
    assertEquals(CONST_STR, executeTestMethod(test, Object.class, null, 0));

    final JJJVMClass test2 = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new LDC_W(CP_STRING_H), new ARETURN());
    assertEquals(CONST_STR_H, executeTestMethod(test2, Object.class, null, 0));
  }

  @Test
  public void test_LDCW_ARETURN_class() throws Throwable {
    final JJJVMClass some = new JJJVMClass();

    final JJJVMProvider proc = new DefaulttestproviderImpl() {
      @Override
      public Object resolveClass(final String className) {
        if (CONST_CLASS.equals(className)) return some;
        return null;
      }
    };

    final JJJVMClass test = prepareTestClass(proc, Type.OBJECT, new LDC_W(CP_CLASS), new ARETURN());
    assertEquals(some, executeTestMethod(test, Object.class, null, 0));

    final JJJVMClass test2 = prepareTestClass(proc, Type.OBJECT, new LDC_W(CP_CLASS_H), new ARETURN());
    assertEquals(some, executeTestMethod(test, Object.class, null, 0));
  }

  @Test
  public void test_LDC2W_LRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LDC2_W(CP_LONG), new LRETURN());
    assertEquals(CONST_LNG, executeTestMethod(test, Long.class, null, 0).longValue());

    final JJJVMClass test2 = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LDC2_W(CP_LONG_H), new LRETURN());
    assertEquals(CONST_LNG_H, executeTestMethod(test2, Long.class, null, 0).longValue());
  }

  @Test
  public void test_ALOAD_ASTORE_ALOAD_ARETURN_notNull() throws Throwable {
    final Object testObj = new Object();
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new ASTORE(280), new ALOAD(280), new ARETURN());
    assertSame(testObj, executeTestMethod(test, Object.class, null, testObj));
  }

  @Test
  public void test_ILOAD_ISTORE_ILOAD_IRETURN_Wide() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ISTORE(280), new ILOAD(280), new IRETURN());
    assertEquals(1234, executeTestMethod(test, Integer.class, null, 1234).intValue());
  }

  @Test
  public void test_ILOAD_ISTORE_ILOAD_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ISTORE(2), new ILOAD(2), new IRETURN());
    assertEquals(1234, executeTestMethod(test, Integer.class, null, 1234).intValue());
  }

  @Test
  public void test_LLOAD_LSTORE_LLOAD_LRETURN_Wide() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new LSTORE(280), new LLOAD(280), new LRETURN());
    assertEquals(123456789012345L, executeTestMethod(test, Long.class, null, 123456789012345L).longValue());
  }

  @Test
  public void test_LLOAD_LSTORE_LLOAD_LRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new LSTORE(2), new LLOAD(2), new LRETURN());
    assertEquals(123456789012345L, executeTestMethod(test, Long.class, null, 123456789012345L).longValue());
  }

  @Test
  public void test_FLOAD_FSTORE_FLOAD_FRETURN_Wide() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new FSTORE(280), new FLOAD(280), new FRETURN());
    assertEquals(123456.89f, executeTestMethod(test, Float.class, null, 123456.89f).floatValue(), 0.0f);
  }

  @Test
  public void test_FLOAD_FSTORE_FLOAD_FRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new FSTORE(2), new FLOAD(2), new FRETURN());
    assertEquals(123456.89f, executeTestMethod(test, Float.class, null, 123456.89f).floatValue(), 0.0f);
  }

  @Test
  public void test_DLOAD_DSTORE_DLOAD_DRETURN_Wide() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new DSTORE(280), new DLOAD(280), new DRETURN());
    assertEquals(1234567723442.89d, executeTestMethod(test, Double.class, null, 1234567723442.89d).doubleValue(), 0.0d);
  }

  @Test
  public void test_DLOAD_DSTORE_DLOAD_DRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new DSTORE(2), new DLOAD(2), new DRETURN());
    assertEquals(1234567723442.89d, executeTestMethod(test, Double.class, null, 1234567723442.89d).doubleValue(), 0.0d);
  }

  @Test
  public void test_NEWARRAY_BALOAD_BASTORE_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public byte test(byte a){ byte [] array = new byte[100]; array[34] = a; return array[34];}");
    assertEquals((byte) -117, executeTestMethod(test, Byte.class, null, Integer.valueOf((byte) -117)).byteValue());
  }

  @Test
  public void test_NEWARRAY_ARRAYLENGTH_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new NEWARRAY(Type.INT),new ARRAYLENGTH(), new IRETURN());
    assertEquals(119, executeTestMethod(test, Integer.class, null, Integer.valueOf(119)).intValue());
  }

  @Test
  public void test_NEWARRAY_CALOAD_CASTORE_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public char test(char a){ char [] array = new char[100]; array[34] = a; return array[34];}");
    assertEquals((char) 0xF234, executeTestMethod(test, Character.class, null, Character.valueOf((char) 0xF234)).charValue());
  }

  @Test
  public void test_NEWARRAY_SALOAD_SASTORE_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public short test(short a){ short [] array = new short[100]; array[34] = a; return array[34];}");
    assertEquals(Short.MIN_VALUE, executeTestMethod(test, Short.class, null, Short.valueOf(Short.MIN_VALUE)).shortValue());
  }

  @Test
  public void test_NEWARRAY_BALOAD_BASTORE_IRETURN_Boolean() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public boolean test(boolean a){ boolean [] array = new boolean[100]; array[34] = a; return array[34];}");
    assertTrue(executeTestMethod(test, Boolean.class, null, Boolean.TRUE).booleanValue());
  }

  @Test
  public void test_NEWARRAY_IALOAD_IASTORE_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ int [] array = new int[100]; array[34] = a; return array[34];}");
    assertEquals(-1123445, executeTestMethod(test, Integer.class, null, -1123445).intValue());
  }

  @Test
  public void test_NEWARRAY_LALOAD_LASTORE_LRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ long [] array = new long[100]; array[34] = a; return array[34];}");
    assertEquals(-112344572362L, executeTestMethod(test, Long.class, null, -112344572362L).longValue());
  }

  @Test
  public void test_NEWARRAY_DALOAD_DASTORE_DRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a){ double [] array = new double[100]; array[34] = a; return array[34];}");
    assertEquals(-112344572362.33d, executeTestMethod(test, Double.class, null, -112344572362.33d).doubleValue(), 0.0d);
  }

  @Test
  public void test_NEWARRAY_FALOAD_FASTORE_FRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a){ float [] array = new float[100]; array[34] = a; return array[34];}");
    assertEquals(-11234.345f, executeTestMethod(test, Float.class, null, -11234.345f).floatValue(), 0.0f);
  }

  @Test
  public void test_ANEWARRAY_AALOAD_AASTORE_ARETURN() throws Throwable {
    final Object testObj = new Object();

    final JJJVMProvider proc = new DefaulttestproviderImpl() {

      @Override
      public Object[] newObjectArray(final JJJVMClass source, final String jvmFormattedClassName, final int arrayLength) {
        assertEquals("java/lang/Object", jvmFormattedClassName);
        assertEquals(100, arrayLength);
        return new Object[arrayLength];
      }

    };

    final JJJVMClass test = prepareTestClass(proc, "public java.lang.Object test(java.lang.Object a){ java.lang.Object [] array = new java.lang.Object[100]; array[34] = a; return array[34];}");
    assertSame(testObj, executeTestMethod(test, Object.class, null, testObj));
  }

  @Test
  public void testMULTIANEWARRAY() throws Throwable {
    final Object fakeArray = new Object();
    
    final DefaulttestproviderImpl provider = new DefaulttestproviderImpl(){
      @Override
      public Object newMultidimensional(final JJJVMClass source, final String jvmFormattedClassName, final int[] dimensions) {
        assertNotNull(source);
        assertEquals(CONST_CLASS_H.replace('.', '/'), jvmFormattedClassName);
        assertArrayEquals(new int[]{1,2,3}, dimensions);
        
        return fakeArray;
      }
    };
    final Object [] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(provider, Type.OBJECT, new ICONST(1), new ICONST(2), new ICONST(3), new MULTIANEWARRAY(CP_CLASS_H, (short)3), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object[]{null});
    assertStack(new Object[]{fakeArray, null, null, null, null, null}, stack);
  }
  
  @Test
  public void test_SWAP_POP_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new SWAP(), new POP(), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_SWAP() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new SWAP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), Integer.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(2), null}, stack);
  }

  @Test
  public void test_DUP_POP2_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP(), new POP2(), new IRETURN());
    assertEquals(1, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_POP2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new POP2(), new ICONST(4), new ICONST(5), new IRETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(5), null}, stack);
  }

  @Test
  public void test_DUP_X1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP_X1(), new IRETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUPX1_IRETURN() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP_X1(), new POP2(), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_DUP_X2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUP_X2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(1), new ICONST(2), new DUP_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), null, Long.valueOf(1), Integer.valueOf(2)}, stack);
  }

  @Test
  public void test_DUP2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUP2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(3), new LCONST(1), new DUP2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), null, Long.valueOf(1), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X1_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new ICONST(4), new DUP2_X1(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), null}, stack);
  }

  @Test
  public void test_DUP2_X1_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(3), new LCONST(1), new DUP2_X1(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), Integer.valueOf(3), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new ICONST(4), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(3), new ICONST(4), new LCONST(1), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver3() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(1), new ICONST(2), new ICONST(3), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), Integer.valueOf(3), null, Long.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null, null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver4() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(0), new LCONST(1), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), null, Long.valueOf(0), null, Long.valueOf(1), null, null}, stack);
  }

  @Test
  public void test_IADD() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a) { return a+27863;}");
    assertEquals(Integer.valueOf(-65423 + 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LADD() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a) { return a+27863L;}");
    assertEquals(Long.valueOf(-65423L + 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FADD() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a) { return a+27863.34f;}");
    assertEquals(-65423.332f + 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DADD() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a) { return a+27863.34d;}");
    assertEquals(-65423.332d + 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_ISUB() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a) { return a-27863;}");
    assertEquals(Integer.valueOf(-65423 - 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LSUB() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a) { return a-27863L;}");
    assertEquals(Long.valueOf(-65423L - 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FSUB() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a) { return a-27863.34f;}");
    assertEquals(-65423.332f - 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DSUB() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a) { return a-27863.34d;}");
    assertEquals(-65423.332d - 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IMUL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a) { return a*27863;}");
    assertEquals(Integer.valueOf(-65423 * 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LMUL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a) { return a*27863L;}");
    assertEquals(Long.valueOf(-65423L * 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FMUL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a) { return a*27863.34f;}");
    assertEquals(-65423.332f * 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DMUL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a) { return a*27863.34d;}");
    assertEquals(-65423.332d * 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IDIV() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a) { return a/27;}");
    assertEquals(Integer.valueOf(-65423 / 27), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LDIV() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a) { return a/27L;}");
    assertEquals(Long.valueOf(-65423L / 27L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FDIV() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a) { return a/27.34f;}");
    assertEquals(-65423.332f / 27.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DDIV() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a) { return a/27.34d;}");
    assertEquals(-65423.332d / 27.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IREM() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a) { return a%27;}");
    assertEquals(Integer.valueOf(-65423 % 27), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LREM() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a) { return a%27L;}");
    assertEquals(Long.valueOf(-65423L % 27L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FREM() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public float test(float a) { return a%27.34f;}");
    assertEquals(-65423.332f % 27.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DREM() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public double test(double a) { return a%27.34d;}");
    assertEquals(-65423.332d % 27.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_INEG() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new INEG(), new IRETURN());
    assertEquals(65, executeTestMethod(test, Integer.class, null, -65).intValue());
  }

  @Test
  public void test_LNEG() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new LNEG(), new LRETURN());
    assertEquals(65L, executeTestMethod(test, Long.class, null, -65L).longValue());
  }

  @Test
  public void test_FNEG() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new FNEG(), new FRETURN());
    assertEquals(65.332f, executeTestMethod(test, Float.class, null, -65.332f), 0.0f);
  }

  @Test
  public void test_DNEG() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new DNEG(), new DRETURN());
    assertEquals(65423.332d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_ISHL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a << 4;}");
    assertEquals(65 << 4, executeTestMethod(test, Integer.class, null, 65).intValue());
  }

  @Test
  public void test_LSHL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a << 4;}");
    assertEquals(65L << 4, executeTestMethod(test, Long.class, null, 65L).longValue());
  }

  @Test
  public void test_ISHR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a >> 2;}");
    assertEquals(65 >> 2, executeTestMethod(test, Integer.class, null, 65).intValue());
  }

  @Test
  public void test_LSHR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a >> 2;}");
    assertEquals(65L >> 2, executeTestMethod(test, Long.class, null, 65L).longValue());
  }

  @Test
  public void test_IUSHR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a >>> 2;}");
    assertEquals(-65 >>> 2, executeTestMethod(test, Integer.class, null, -65).intValue());
  }

  @Test
  public void test_LUSHR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a >>> 2;}");
    assertEquals(-65L >>> 2, executeTestMethod(test, Long.class, null, -65L).longValue());
  }

  @Test
  public void test_IAND() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a & 342;}");
    assertEquals(265463 & 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LAND() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a & 342L;}");
    assertEquals(265463L & 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IOR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a | 342;}");
    assertEquals(265463 | 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LOR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a | 342L;}");
    assertEquals(265463L | 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IXOR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ return a ^ 342;}");
    assertEquals(265463 ^ 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LXOR() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public long test(long a){ return a ^ 342L;}");
    assertEquals(265463L ^ 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IINC() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new IINC(1, 13), new ILOAD(1), new IRETURN());
    assertEquals(265463 + 13, executeTestMethod(test, Integer.class, null, 265463).longValue());
  }

  @Test
  public void test_IINC_Wide() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ISTORE(280), new IINC(280, 13), new ILOAD(280), new IRETURN());
    assertEquals(265463 + 13, executeTestMethod(test, Integer.class, null, 265463).longValue());
  }

  @Test
  public void test_I2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(3), new I2L(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, 3L, null}, stack);
  }

  @Test
  public void test_I2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ICONST(3), new I2D(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, 3.0d, null}, stack);
  }

  @Test
  public void test_I2B() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new I2B(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((byte) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((byte) 123334), null}, stack);
  }

  @Test
  public void test_I2C() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new I2C(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((char) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((char) 123334), null}, stack);
  }

  @Test
  public void test_I2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new I2F(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 234);
    assertStack(new Object[]{234.0f, null}, stack);
  }

  @Test
  public void test_I2S() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new I2S(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((short) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((short) 123334), null}, stack);
  }

  @Test
  public void test_L2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new L2I(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{1234, null}, stack);
  }

  @Test
  public void test_L2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new L2F(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{1234.0f, null}, stack);
  }

  @Test
  public void test_L2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.LONG, new LLOAD(1), new L2D(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{null, 1234.0d, null}, stack);
  }

  @Test
  public void test_D2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new D2I(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{-1234, null}, stack);
  }

  @Test
  public void test_D2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new D2L(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{null, -1234L, null}, stack);
  }

  @Test
  public void test_D2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new LLOAD(1), new D2F(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{Double.valueOf(-1234.234d).floatValue(), null}, stack);
  }

  @Test
  public void test_F2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new F2I(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{1234, null}, stack);
  }

  @Test
  public void test_F2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new F2L(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{null, 1234L, null}, stack);
  }

  @Test
  public void test_F2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new F2D(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{null, Float.valueOf(1234.123f).doubleValue(), null}, stack);
  }

  @Test
  public void test_LCMP_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(1), new LCONST(1), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_LCMP_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(0), new LCONST(1), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_LCMP_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new LCONST(1), new LCONST(0), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new FCONST(1.0f), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new FCONST(1.0f), new FCONST(0.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new FCONST(0.0f), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FCONST(1.0f), new FLOAD(1), new FCMPL(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPG_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FLOAD(1), new FCONST(1.0f), new FCMPG(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPG_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.FLOAT, new FCONST(1.0f), new FLOAD(1), new FCMPG(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new DCONST(1.0d), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new DCONST(1.0d), new DCONST(0.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new DCONST(0.0d), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DLOAD(1), new DCMPL(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPG_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DLOAD(1), new DCONST(1.0d), new DCMPG(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPG_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DLOAD(1), new DCMPG(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_IFEQ() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFEQ.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 34).intValue());
  }

  @Test
  public void test_IFNE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFNE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 0).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 34).intValue());
  }

  @Test
  public void test_IFLT() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFLT.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, -5).intValue());
  }

  @Test
  public void test_IFLE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFLE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFGT() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFGT.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFGE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new Branch(IFGE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFICMPEQ() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPEQ.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPNE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPNE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPLT() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPLT.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPLE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPLE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPGT() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPGT.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPGE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPGE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFACMPEQ() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new ACONST_NULL(), new Branch(IF_ACMPEQ.class, 5), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{3, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_IFACMPNE() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new ACONST_NULL(), new Branch(IF_ACMPNE.class, 5), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{3, null, null, null}, stack);
  }

  @Test
  public void test_GOTO() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new Branch(GOTO.class, 3), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_GOTO_W() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new Branch(GOTO_W.class, 3), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_JSR_RET() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new Branch(JSR.class, 2), new RETURN(), new ASTORE(85), new ICONST(2), new RET(85));
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_WIDE_JSR_RET() throws Throwable {
    final Object[] stack = new Object[100];
    final List<Object> instr = new ArrayList<Object>();
    instr.add(new Branch(JSR.class, 2+65535));
    instr.add(new RETURN());
    for(int i=0; i< 65535; i++){
      instr.add(new NOP());
    }
    instr.add(new ASTORE(288));
    instr.add(new ICONST(2));
    instr.add(new RET(288));
    
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, instr.toArray());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_LOOKUPSWITCH() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){"
            + "switch(a){"
            + "case 1: return 5;"
            + "case 2: return 6;"
            + "case 3: return 7;"
            + "case 4: return 8;"
            + "case 1000234: return 9;"
            + "case -1000234: return 10;"
            + "default: return -1;"
            + "}}");
    assertEquals(5, executeTestMethod(test, Integer.class, null, 1).intValue());
    assertEquals(6, executeTestMethod(test, Integer.class, null, 2).intValue());
    assertEquals(7, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(8, executeTestMethod(test, Integer.class, null, 4).intValue());
    assertEquals(9, executeTestMethod(test, Integer.class, null, 1000234).intValue());
    assertEquals(10, executeTestMethod(test, Integer.class, null, -1000234).intValue());
    assertEquals(-1, executeTestMethod(test, Integer.class, null, 342).intValue());
  }

  @Test
  public void test_GETSTATIC_PUTSTATIC_sameClass() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ sfield=a; return sfield;}");
    assertEquals(9942343, executeTestMethod(test, Integer.class, null, 9942343).intValue());
  }

  @Test
  public void test_GETSTATIC_PUTSTATIC_otherClass() throws Throwable {
    final AtomicBoolean calledSet = new AtomicBoolean();
    final AtomicBoolean calledGet = new AtomicBoolean();
    
    final JJJVMProvider processor = new DefaulttestproviderImpl(){
      
      @Override
      public void setStatic(final JJJVMClass source, final String className, final String fieldName, final String fieldSignature, final Object value) {
        assertEquals("com/igormaznitsa/jjjvm/TestObject",className);
        assertEquals("sfield",fieldName);
        assertEquals("I",fieldSignature);
        
        assertFalse(calledSet.get());
        calledSet.set(true);
        
        assertEquals(Integer.valueOf(9942343),value);
      }

      @Override
      public Object getStatic(final JJJVMClass source, final String className, final String fieldName, final String fieldSignature) {
        assertEquals("com/igormaznitsa/jjjvm/TestObject", className);
        assertEquals("sfield", fieldName);
        assertEquals("I", fieldSignature);

        assertFalse(calledGet.get());
        calledGet.set(true);
        
        return 23334;
      }
        
    };

    final JJJVMClass test = prepareTestClass(processor, "public int test(int a){ com.igormaznitsa.jjjvm.TestObject.sfield=a; return com.igormaznitsa.jjjvm.TestObject.sfield;}");
    assertEquals(23334, executeTestMethod(test, Integer.class, null, 9942343).intValue());

    assertTrue(calledGet.get());
    assertTrue(calledSet.get());
  }
  
  @Test
  public void test_GETFIRLD_PUTFIELD_thisClass() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), "public int test(int a){ field=a; return field;}");
    assertEquals(9942343, executeTestMethod(test, Integer.class, null, 9942343).intValue());
  }

  @Test
  public void test_GETFIELD_PUTFIELD_otherClass() throws Throwable {
    final AtomicBoolean calledSet = new AtomicBoolean();
    final AtomicBoolean calledGet = new AtomicBoolean();

    final JJJVMProvider processor = new DefaulttestproviderImpl() {
      private TestObject obj;
      
      @Override
      public Object allocate(JJJVMClass source, String jvmFormattedClassName) {
        assertEquals("com/igormaznitsa/jjjvm/TestObject",jvmFormattedClassName);
        obj = new TestObject();
        return obj;
      }

      @Override
      public void set(final JJJVMClass source, final Object obj, final String fieldName, final String fieldSignature, final Object value) {
        assertSame(this.obj, obj);
        assertEquals("field", fieldName);
        assertEquals("I", fieldSignature);

        assertFalse(calledSet.get());
        calledSet.set(true);

        assertEquals(Integer.valueOf(9942343), value);
      }

      @Override
      public Object get(final JJJVMClass source, final Object obj, final String fieldName, final String fieldSignature) {
        assertSame(this.obj, obj);
        assertEquals("field", fieldName);
        assertEquals("I", fieldSignature);

        assertFalse(calledGet.get());
        calledGet.set(true);

        return 23334;
      }
    };
    
    final JJJVMClass test = prepareTestClass(processor, "public int test(int a){ com.igormaznitsa.jjjvm.TestObject obj = new com.igormaznitsa.jjjvm.TestObject(); obj.field=a; return obj.field;}");
    assertEquals(23334, executeTestMethod(test, Integer.class, null, 9942343).intValue());

    assertTrue(calledGet.get());
    assertTrue(calledSet.get());
  }

  @Test
  public void testIFNULL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new Branch(IFNULL.class, 4), new ICONST(1), new RETURN(), new ICONST(2), new RETURN());
    final Object [] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{2,null,null,null}, stack);
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{1,null,null,null}, stack);
  }

  @Test
  public void testIFNONNULL() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new Branch(IFNONNULL.class, 4), new ICONST(1), new RETURN(), new ICONST(2), new RETURN());
    final Object [] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{1,null,null,null}, stack);
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2,null,null,null}, stack);
  }

  @Test(expected = NullPointerException.class)
  public void testATHROW_null() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new ATHROW());
    executeTestMethod(test, Object.class, null, (Object) null);
  }

  @Test(expected = RuntimeException.class)
  public void testATHROW_RTE() throws Throwable {
    final JJJVMClass test = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new ATHROW());
    executeTestMethod(test, Object.class, null, new RuntimeException("TEST"));
  }

  @Test
  public void testATHROW_NonThrowableObject() throws Throwable {
    final Object obj = new Object();
    
    final JJJVMProvider provider = new DefaulttestproviderImpl(){

      @Override
      public void doThrow(JJJVMClass caller, Object exception) throws Throwable {
        assertNotNull(caller);
        assertSame(obj, exception);
        throw new IOException("HEHEHE");
      }
      
    };
    
    final JJJVMClass test = prepareTestClass(provider, Type.OBJECT, new ALOAD(1), new ATHROW());
    try{
      executeTestMethod(test, Object.class, null, obj);
      fail("Must throw IOE");
    }catch(IOException ex){
    }
  }

  @Test
  public void testCHECKCAST() throws Throwable {
    final Object fake = new Object();
    
    final AtomicBoolean flag = new AtomicBoolean();
    
    final JJJVMProvider proc = new DefaulttestproviderImpl(){
      @Override
      public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) {
        assertEquals(CONST_CLASS,jvmFormattedClassName);
        assertEquals(fake,value);
        return flag.get();
      }
    };
    
    final JJJVMClass test = prepareTestClass(proc, Type.OBJECT, new ALOAD(1), new CHECKCAST(CP_CLASS), new ARETURN());
    flag.set(true);
    assertEquals(null,executeTestMethod(test, Object.class, null, (Object) null));
    assertEquals(fake,executeTestMethod(test, Object.class, null, fake));
    try{
      flag.set(false);
      executeTestMethod(test, Object.class, null, fake);
      fail("Must throw CCE");
    }catch(ClassCastException ex){
      
    }
  }

  @Test
  public void testINSTANCEOF() throws Throwable {
    final Object fake = new Object();
    
    final JJJVMProvider proc = new DefaulttestproviderImpl(){

      @Override
      public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) {
        assertEquals(CONST_CLASS, jvmFormattedClassName);
        assertEquals(fake, value);
        return true;
      }
    };
    
    final JJJVMClass test = prepareTestClass(proc, Type.OBJECT, new ALOAD(1), new INSTANCEOF(CP_CLASS), new RETURN());
    final Object [] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{0, null, null, null},stack);
    executeTestMethod(test, Object.class, stack, fake);
    assertStack(new Object[]{1,null,null,null},stack);
  }
  
  @Test
  public void testMONITORENTER_MONITOREXIT_jjjvmobject() throws Throwable {
    final JJJVMObject obj = new JJJVMObject();
    final JJJVMClass test1 = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new MONITORENTER(), new ALOAD(1), new ARETURN());
    final JJJVMClass test2 = prepareTestClass(new DefaulttestproviderImpl(), Type.OBJECT, new ALOAD(1), new MONITOREXIT(), new ALOAD(1), new ARETURN());
    
    assertFalse(obj.isLocked());
    executeTestMethod(test1, Object.class, null, obj);
    assertTrue(obj.isLocked());
    executeTestMethod(test2, Object.class, null, obj);
    assertFalse(obj.isLocked());
  }

  @Test
  public void testMONITORENTER_MONITOREXIT_notjjjvmobject() throws Throwable {
    final Object obj = new Object();
    final AtomicBoolean locked = new AtomicBoolean();
    
    final AtomicInteger counter = new AtomicInteger();
    
    final JJJVMProvider prov = new DefaulttestproviderImpl(){
      @Override
      public void doMonitor(final JJJVMClass caller, final Object object, final boolean lock) {
        counter.incrementAndGet();
        assertNotNull(caller);
        assertSame(obj, object);
        locked.set(lock);
      }
    };
    
    final JJJVMClass test1 = prepareTestClass(prov, Type.OBJECT, new ALOAD(1), new MONITORENTER(), new ALOAD(1), new ARETURN());
    final JJJVMClass test2 = prepareTestClass(prov, Type.OBJECT, new ALOAD(1), new MONITOREXIT(), new ALOAD(1), new ARETURN());
    
    assertFalse(locked.get());
    executeTestMethod(test1, Object.class, null, obj);
    assertTrue(locked.get());
    executeTestMethod(test2, Object.class, null, obj);
    assertFalse(locked.get());
    
    assertEquals(2, counter.get());
  }
  
  @Test
  public void testIntegration_TestTableswitch() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestTableswitch");
    
    assertTrue(testKlazz.canBeCastTo("java/lang/Object"));
    assertFalse(testKlazz.canBeCastTo("java/util/Map"));
    assertTrue(testKlazz.canBeCastTo("java/io/Serializable"));
    
    assertEquals(Integer.valueOf(1234), testKlazz.findDeclaredField("sfield1").getStaticValue());
    assertEquals(Long.valueOf(56787L), testKlazz.findDeclaredField("sfield2").getStaticValue());
    assertEquals("Hello world", testKlazz.findDeclaredField("sfield3").getStaticValue());
  
    final Double arg = new Double(11233.0932d);
    
    final JJJVMObject obj = testKlazz.newInstance("(D)V", new Object[]{arg}, null, null);
    assertNotNull(obj);
    assertSame(testKlazz, obj.getKlazz());
    assertSame(arg, testKlazz.findField("field4").get(obj));
    assertEquals(Integer.valueOf(9876), testKlazz.findField("field1").get(obj));
    assertEquals(Long.valueOf(6666L), testKlazz.findField("field2").get(obj));
    assertEquals("Ugums", testKlazz.findField("field3").get(obj));
  
    final JJJVMClassMethod doCalcMethod = testKlazz.findMethod("doCalc", "(D)D");
    
    assertEquals(new Double(11233.0932d + -652374.23d / 34 - (23 * -652374.23d)), testKlazz.invoke(obj, doCalcMethod, new Object[]{new Double(-652374.23d)}, null, null));
  
    final JJJVMClassMethod doTableSwitchMethod = testKlazz.findDeclaredMethod("doTableSwitch", "(J)D");
    assertEquals(new Double(1.0d),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{-1L}, null, null));
    assertEquals(new Double(1.3d),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{0L}, null, null));
    assertEquals(new Double(2.0d),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{1L}, null, null));
    assertEquals(new Double(8.0d),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{2L}, null, null));
    assertEquals(new Double(81.0d),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{9L}, null, null));
    assertEquals(new Double(93*93),testKlazz.invoke(obj, doTableSwitchMethod, new Object[]{93L}, null, null));
  }

  @Test
  public void testIntegration_TestInvoke() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestInvoke");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMClassMethod calc = testKlazz.findMethod("calc", "(I)I");
    final JJJVMClassMethod calcLong = testKlazz.findMethod("calc", "(J)J");
    assertEquals((125*125)/2+10,testKlazz.invoke(obj, calc, new Object[]{new Integer(125)}, null, null));
    assertEquals((125L*125L)/2L+10L,testKlazz.invoke(obj, calcLong, new Object[]{new Long(125L)}, null, null));
  }

  @Test
  public void testIntegration_TestVector() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestVector");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMClassMethod fillVector = testKlazz.findMethod("fillVector", "(Ljava/util/Vector;)Ljava/util/Vector;");
    final JJJVMClassMethod makeVector = testKlazz.findMethod("makeVector", "()Ljava/util/Vector;");
    
    final Vector theVector = new Vector();
    theVector.add("1");
    theVector.add("2");
    theVector.add("3");
    
    final Vector result = (Vector)testKlazz.invoke(obj, fillVector, new Object[]{theVector}, null, null);
    assertSame(theVector, result);
    assertArrayEquals(new Object[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}, result.toArray());
    assertArrayEquals(new Object[]{0,1,2,3,4,5,6,7,8,9}, ((Vector) testKlazz.invoke(obj, makeVector, null, null, null)).toArray());
  }

  @Test
  public void testIntegration_TestInnerClasses() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestInnerClasses");
    
    final JJJVMObject instance = testKlazz.newInstance(true);
    
    final JJJVMClassMethod test = testKlazz.findMethod("test", "(II)I");
    assertEquals(96, testKlazz.invoke(instance, test, new Object[]{123,456}, null, null));
  }
  
  @Test
  public void testIntegration_TestThrow() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestThrow");

    final JJJVMObject obj = testKlazz.newInstance(true);
    
    final JJJVMClassMethod throwIOE = testKlazz.findMethod("throwIOE", "()V");
    final JJJVMClassMethod throwDIFF = testKlazz.findMethod("throwDIFF", "(I)V");
    
    try{
      testKlazz.invoke(obj, throwIOE, null, null, null);
      fail("Must throw IOE");
    }catch(IOException ex){
      assertEquals("IOE",ex.getMessage());
    }
    
    try{
      testKlazz.invoke(obj, throwDIFF, new Object[]{0}, null, null);
      fail("Must throw AE");
    }catch(ArithmeticException ex){
      assertEquals("arith",ex.getMessage());
    }
    
    try{
      testKlazz.invoke(obj, throwDIFF, new Object[]{1}, null, null);
      fail("Must throw UOE");
    }catch(UnsupportedOperationException ex){
      assertEquals("uns",ex.getMessage());
    }
    
    try{
      testKlazz.invoke(obj, throwDIFF, new Object[]{9993}, null, null);
      fail("Must throw ISE");
    }catch(IllegalStateException ex){
      assertEquals("ise",ex.getMessage());
      assertEquals("npe",ex.getCause().getMessage());
    }
  }
  
  @Test
  public void testIntegration_FillMultidimensionalArray_Int() throws Throwable {
    final JJJVMClass test = prepareTestClass(new JSEProviderImpl(this), 
            "public java.lang.Object test(java.lang.Object a){"
            + " int [][] array = new int[2][10];"
            + " for(int x=0; x<array.length; x++){"
            + "  int [] fill = array[x];"
            + "  for(int y=0; y< fill.length; y++){"
            + "    fill[y] = y;"
            + "  }"
            + " }"
            + " return array;"
            + "}"
    );
  
    final int[][] result = (int[][]) executeTestMethod(test, Object.class, null, null);
    assertEquals(2, result.length);
    assertArrayEquals(new int[]{0,1,2,3,4,5,6,7,8,9}, result[0]);
    assertArrayEquals(new int[]{0,1,2,3,4,5,6,7,8,9}, result[1]);
  }

}
