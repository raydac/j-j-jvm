package com.igormaznitsa.jjjvm;

import com.igormaznitsa.jjjvm.impl.JJJVMClassImpl;
import com.igormaznitsa.jjjvm.impl.jse.JSEProviderImpl;
import com.igormaznitsa.jjjvm.model.JJJVMClass;
import com.igormaznitsa.jjjvm.model.JJJVMMethod;
import com.igormaznitsa.jjjvm.model.JJJVMObject;
import com.igormaznitsa.jjjvm.model.JJJVMProvider;
import com.igormaznitsa.jjjvm.testclasses.TestObject;
import com.igormaznitsa.jjjvm.utils.Branch;
import com.igormaznitsa.jjjvm.utils.TestHelper;
import com.igormaznitsa.jjjvm.utils.TestProviderImpl;
import org.apache.bcel.generic.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class JJJVMInterpreterTest extends TestHelper implements JSEProviderImpl.ClassDataLoader {

  public byte[] loadClassBody(String jvmFormattedClassName) {
    if (!jvmFormattedClassName.startsWith("com/igormaznitsa/jjjvm/testclasses")) {
      return null;
    }
    try {
      return TestHelper.loadClassBodyFromClassPath(jvmFormattedClassName);
    }
    catch (ClassNotFoundException ex) {
      // do nothing
    }
    catch (Throwable thr) {
      fail("Can't load class "+jvmFormattedClassName+", error : "+thr.toString());
    }
    return null;
  }

  @Test
  @Ignore("ignored because JvaSE wrapper doesn't support instantiation of abstract classes and java.lang.Enum is abstract one")
  public void testIntegration_TestEnumeration() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestEnumeration");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMMethod disabledMethod = testKlazz.findMethod("testDISABLED", "()Ljava/lang/String;");
    final JJJVMMethod enabledMethod = testKlazz.findMethod("testENABLED", "()Ljava/lang/String;");

    assertEquals("N",disabledMethod.invoke(obj, new Object[0]));
    assertEquals("Y",enabledMethod.invoke(obj, new Object[0]));
  }

  @Test
  public void test_NOP_NULL_ARETURN_null() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new NOP(), new ACONST_NULL(), new ARETURN());
    assertNull(executeTestMethod(test, Object.class, null, new Object()));
  }

  @Test
  public void test_ICONSTM1_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(-1), new IRETURN());
    assertEquals(-1, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_ICONST5_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(5), new IRETURN());
    assertEquals(5, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_FCONST0_FRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FCONST(0.0f), new FRETURN());
    assertEquals(0.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_FCONST1_FRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FCONST(1.0f), new FRETURN());
    assertEquals(1.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_FCONST2_FRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FCONST(2.0f), new FRETURN());
    assertEquals(2.0f, executeTestMethod(test, Float.class, null, 0.0f), 0f);
  }

  @Test
  public void test_DCONST0_DRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DCONST(0.0d), new DRETURN());
    assertEquals(0.0d, executeTestMethod(test, Double.class, null, 0.0d), 0.0d);
  }

  @Test
  public void test_DCONST1_DRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DRETURN());
    assertEquals(1.0d, executeTestMethod(test, Double.class, null, 0.0d), 0.0d);
  }

  @Test
  public void test_LCONST1_LRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LCONST(1), new LRETURN());
    assertEquals(1L, executeTestMethod(test, Long.class, null, 0L).longValue());
  }

  @Test
  public void test_BIPUSH_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new BIPUSH((byte) -76), new IRETURN());
    assertEquals(-76, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_SIPUSH_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new SIPUSH((short) -763), new IRETURN());
    assertEquals(-763, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_LDC_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LDC(CP_INT), new IRETURN());
    assertEquals(CONST_INT, executeTestMethod(test, Integer.class, null, 0).intValue());

    final JJJVMClassImpl test2 = prepareTestClass(new TestProviderImpl(), Type.INT, new LDC(CP_INT_H), new IRETURN());
    assertEquals(CONST_INT_H, executeTestMethod(test2, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_LDCW_ARETURN_string() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new LDC_W(CP_STRING), new ARETURN());
    assertEquals(CONST_STR, executeTestMethod(test, Object.class, null, 0));

    final JJJVMClassImpl test2 = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new LDC_W(CP_STRING_H), new ARETURN());
    assertEquals(CONST_STR_H, executeTestMethod(test2, Object.class, null, 0));
  }

  @Test
  public void test_LDCW_ARETURN_class() throws Throwable {
    final JJJVMClass some = new JJJVMClassImpl();

    final JJJVMProvider proc = new TestProviderImpl() {
      @Override
      public Object resolveClass(final String className) {
        if (CONST_CLASS.equals(className)) {
          return some;
        }
        return null;
      }
    };

    final JJJVMClassImpl test = prepareTestClass(proc, Type.OBJECT, new LDC_W(CP_CLASS), new ARETURN());
    assertEquals(some, executeTestMethod(test, Object.class, null, 0));

    final JJJVMClassImpl test2 = prepareTestClass(proc, Type.OBJECT, new LDC_W(CP_CLASS_H), new ARETURN());
    assertEquals(some, executeTestMethod(test, Object.class, null, 0));
  }

  @Test
  public void test_LDC2W_LRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LDC2_W(CP_LONG), new LRETURN());
    assertEquals(CONST_LNG, executeTestMethod(test, Long.class, null, 0).longValue());

    final JJJVMClassImpl test2 = prepareTestClass(new TestProviderImpl(), Type.LONG, new LDC2_W(CP_LONG_H), new LRETURN());
    assertEquals(CONST_LNG_H, executeTestMethod(test2, Long.class, null, 0).longValue());
  }

  @Test
  public void test_ALOAD_ASTORE_ALOAD_ARETURN_notNull() throws Throwable {
    final Object testObj = new Object();
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new ASTORE(280), new ALOAD(280), new ARETURN());
    assertSame(testObj, executeTestMethod(test, Object.class, null, testObj));
  }

  @Test
  public void test_ILOAD_ISTORE_ILOAD_IRETURN_Wide() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ISTORE(280), new ILOAD(280), new IRETURN());
    assertEquals(1234, executeTestMethod(test, Integer.class, null, 1234).intValue());
  }

  @Test
  public void test_ILOAD_ISTORE_ILOAD_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ISTORE(2), new ILOAD(2), new IRETURN());
    assertEquals(1234, executeTestMethod(test, Integer.class, null, 1234).intValue());
  }

  @Test
  public void test_LLOAD_LSTORE_LLOAD_LRETURN_Wide() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new LSTORE(280), new LLOAD(280), new LRETURN());
    assertEquals(123456789012345L, executeTestMethod(test, Long.class, null, 123456789012345L).longValue());
  }

  @Test
  public void test_LLOAD_LSTORE_LLOAD_LRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new LSTORE(2), new LLOAD(2), new LRETURN());
    assertEquals(123456789012345L, executeTestMethod(test, Long.class, null, 123456789012345L).longValue());
  }

  @Test
  public void test_FLOAD_FSTORE_FLOAD_FRETURN_Wide() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new FSTORE(280), new FLOAD(280), new FRETURN());
    assertEquals(123456.89f, executeTestMethod(test, Float.class, null, 123456.89f).floatValue(), 0.0f);
  }

  @Test
  public void test_FLOAD_FSTORE_FLOAD_FRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new FSTORE(2), new FLOAD(2), new FRETURN());
    assertEquals(123456.89f, executeTestMethod(test, Float.class, null, 123456.89f).floatValue(), 0.0f);
  }

  @Test
  public void test_DLOAD_DSTORE_DLOAD_DRETURN_Wide() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new DSTORE(280), new DLOAD(280), new DRETURN());
    assertEquals(1234567723442.89d, executeTestMethod(test, Double.class, null, 1234567723442.89d).doubleValue(), 0.0d);
  }

  @Test
  public void test_DLOAD_DSTORE_DLOAD_DRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new DSTORE(2), new DLOAD(2), new DRETURN());
    assertEquals(1234567723442.89d, executeTestMethod(test, Double.class, null, 1234567723442.89d).doubleValue(), 0.0d);
  }

  @Test
  public void test_NEWARRAY_BALOAD_BASTORE_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public byte test(byte a){ byte [] array = new byte[100]; array[34] = a; return array[34];}");
    assertEquals((byte) -117, executeTestMethod(test, Byte.class, null, Integer.valueOf((byte) -117)).byteValue());
  }

  @Test
  public void test_NEWARRAY_ARRAYLENGTH_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new NEWARRAY(Type.INT), new ARRAYLENGTH(), new IRETURN());
    assertEquals(119, executeTestMethod(test, Integer.class, null, Integer.valueOf(119)).intValue());
  }

  @Test
  public void test_NEWARRAY_CALOAD_CASTORE_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public char test(char a){ char [] array = new char[100]; array[34] = a; return array[34];}");
    assertEquals((char) 0xF234, executeTestMethod(test, Character.class, null, Character.valueOf((char) 0xF234)).charValue());
  }

  @Test
  public void test_NEWARRAY_SALOAD_SASTORE_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public short test(short a){ short [] array = new short[100]; array[34] = a; return array[34];}");
    assertEquals(Short.MIN_VALUE, executeTestMethod(test, Short.class, null, Short.valueOf(Short.MIN_VALUE)).shortValue());
  }

  @Test
  public void test_NEWARRAY_BALOAD_BASTORE_IRETURN_Boolean() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public boolean test(boolean a){ boolean [] array = new boolean[100]; array[34] = a; return array[34];}");
    assertTrue(executeTestMethod(test, Boolean.class, null, Boolean.TRUE).booleanValue());
  }

  @Test
  public void test_NEWARRAY_IALOAD_IASTORE_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ int [] array = new int[100]; array[34] = a; return array[34];}");
    assertEquals(-1123445, executeTestMethod(test, Integer.class, null, -1123445).intValue());
  }

  @Test
  public void test_NEWARRAY_LALOAD_LASTORE_LRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ long [] array = new long[100]; array[34] = a; return array[34];}");
    assertEquals(-112344572362L, executeTestMethod(test, Long.class, null, -112344572362L).longValue());
  }

  @Test
  public void test_NEWARRAY_DALOAD_DASTORE_DRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a){ double [] array = new double[100]; array[34] = a; return array[34];}");
    assertEquals(-112344572362.33d, executeTestMethod(test, Double.class, null, -112344572362.33d).doubleValue(), 0.0d);
  }

  @Test
  public void test_NEWARRAY_FALOAD_FASTORE_FRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a){ float [] array = new float[100]; array[34] = a; return array[34];}");
    assertEquals(-11234.345f, executeTestMethod(test, Float.class, null, -11234.345f).floatValue(), 0.0f);
  }

  @Test
  public void test_ANEWARRAY_AALOAD_AASTORE_ARETURN() throws Throwable {
    final Object testObj = new Object();

    final JJJVMProvider proc = new TestProviderImpl() {

      @Override
      public Object[] newObjectArray(final JJJVMClass source, final String jvmFormattedClassName, final int arrayLength) {
        assertEquals("java/lang/Object", jvmFormattedClassName);
        assertEquals(100, arrayLength);
        return new Object[arrayLength];
      }

    };

    final JJJVMClassImpl test = prepareTestClass(proc, "public java.lang.Object test(java.lang.Object a){ java.lang.Object [] array = new java.lang.Object[100]; array[34] = a; return array[34];}");
    assertSame(testObj, executeTestMethod(test, Object.class, null, testObj));
  }

  @Test
  public void testMULTIANEWARRAY() throws Throwable {
    final Object fakeArray = new Object();

    final TestProviderImpl provider = new TestProviderImpl() {
      @Override
      public Object newMultidimensional(final JJJVMClass source, final String jvmFormattedClassName, final int[] dimensions) {
        assertNotNull(source);
        assertEquals(CONST_CLASS_H.replace('.', '/'), jvmFormattedClassName);
        assertArrayEquals(new int[]{1, 2, 3}, dimensions);

        return fakeArray;
      }
    };
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(provider, Type.OBJECT, new ICONST(1), new ICONST(2), new ICONST(3), new MULTIANEWARRAY(CP_CLASS_H, (short) 3), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object[]{null});
    assertStack(new Object[]{fakeArray, null, null, null, null, null}, stack);
  }

  @Test
  public void test_SWAP_POP_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new SWAP(), new POP(), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_SWAP() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new SWAP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), Integer.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(2), null}, stack);
  }

  @Test
  public void test_DUP_POP2_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP(), new POP2(), new IRETURN());
    assertEquals(1, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_POP2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new POP2(), new ICONST(4), new ICONST(5), new IRETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(5), null}, stack);
  }

  @Test
  public void test_DUP_X1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP_X1(), new IRETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUPX1_IRETURN() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new DUP_X1(), new POP2(), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_DUP_X2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUP_X2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(1), new ICONST(2), new DUP_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), null, Long.valueOf(1), Integer.valueOf(2)}, stack);
  }

  @Test
  public void test_DUP2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new DUP2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), null}, stack);
  }

  @Test
  public void test_DUP2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(3), new LCONST(1), new DUP2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), null, Long.valueOf(1), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X1_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new ICONST(4), new DUP2_X1(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), null}, stack);
  }

  @Test
  public void test_DUP2_X1_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(3), new LCONST(1), new DUP2_X1(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), Integer.valueOf(3), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(1), new ICONST(2), new ICONST(3), new ICONST(4), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(3), new ICONST(4), new LCONST(1), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), null, Long.valueOf(1), null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver3() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(1), new ICONST(2), new ICONST(3), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{Integer.valueOf(2), Integer.valueOf(3), null, Long.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null, null}, stack);
  }

  @Test
  public void test_DUP2_X2_ver4() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(0), new LCONST(1), new DUP2_X2(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, Long.valueOf(1), null, Long.valueOf(0), null, Long.valueOf(1), null, null}, stack);
  }

  @Test
  public void test_IADD() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a) { return a+27863;}");
    assertEquals(Integer.valueOf(-65423 + 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LADD() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a) { return a+27863L;}");
    assertEquals(Long.valueOf(-65423L + 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FADD() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a) { return a+27863.34f;}");
    assertEquals(-65423.332f + 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DADD() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a) { return a+27863.34d;}");
    assertEquals(-65423.332d + 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_ISUB() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a) { return a-27863;}");
    assertEquals(Integer.valueOf(-65423 - 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LSUB() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a) { return a-27863L;}");
    assertEquals(Long.valueOf(-65423L - 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FSUB() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a) { return a-27863.34f;}");
    assertEquals(-65423.332f - 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DSUB() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a) { return a-27863.34d;}");
    assertEquals(-65423.332d - 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IMUL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a) { return a*27863;}");
    assertEquals(Integer.valueOf(-65423 * 27863), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LMUL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a) { return a*27863L;}");
    assertEquals(Long.valueOf(-65423L * 27863L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FMUL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a) { return a*27863.34f;}");
    assertEquals(-65423.332f * 27863.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DMUL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a) { return a*27863.34d;}");
    assertEquals(-65423.332d * 27863.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IDIV() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a) { return a/27;}");
    assertEquals(Integer.valueOf(-65423 / 27), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LDIV() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a) { return a/27L;}");
    assertEquals(Long.valueOf(-65423L / 27L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FDIV() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a) { return a/27.34f;}");
    assertEquals(-65423.332f / 27.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DDIV() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a) { return a/27.34d;}");
    assertEquals(-65423.332d / 27.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_IREM() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a) { return a%27;}");
    assertEquals(Integer.valueOf(-65423 % 27), executeTestMethod(test, Integer.class, null, -65423));
  }

  @Test
  public void test_LREM() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a) { return a%27L;}");
    assertEquals(Long.valueOf(-65423L % 27L), executeTestMethod(test, Long.class, null, -65423L));
  }

  @Test
  public void test_FREM() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public float test(float a) { return a%27.34f;}");
    assertEquals(-65423.332f % 27.34f, executeTestMethod(test, Float.class, null, -65423.332f), 0.0f);
  }

  @Test
  public void test_DREM() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public double test(double a) { return a%27.34d;}");
    assertEquals(-65423.332d % 27.34d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_INEG() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new INEG(), new IRETURN());
    assertEquals(65, executeTestMethod(test, Integer.class, null, -65).intValue());
  }

  @Test
  public void test_LNEG() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new LNEG(), new LRETURN());
    assertEquals(65L, executeTestMethod(test, Long.class, null, -65L).longValue());
  }

  @Test
  public void test_FNEG() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new FNEG(), new FRETURN());
    assertEquals(65.332f, executeTestMethod(test, Float.class, null, -65.332f), 0.0f);
  }

  @Test
  public void test_DNEG() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new DNEG(), new DRETURN());
    assertEquals(65423.332d, executeTestMethod(test, Double.class, null, -65423.332d), 0.0d);
  }

  @Test
  public void test_ISHL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a << 4;}");
    assertEquals(65 << 4, executeTestMethod(test, Integer.class, null, 65).intValue());
  }

  @Test
  public void test_LSHL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a << 4;}");
    assertEquals(65L << 4, executeTestMethod(test, Long.class, null, 65L).longValue());
  }

  @Test
  public void test_ISHR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a >> 2;}");
    assertEquals(65 >> 2, executeTestMethod(test, Integer.class, null, 65).intValue());
  }

  @Test
  public void test_LSHR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a >> 2;}");
    assertEquals(65L >> 2, executeTestMethod(test, Long.class, null, 65L).longValue());
  }

  @Test
  public void test_IUSHR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a >>> 2;}");
    assertEquals(-65 >>> 2, executeTestMethod(test, Integer.class, null, -65).intValue());
  }

  @Test
  public void test_LUSHR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a >>> 2;}");
    assertEquals(-65L >>> 2, executeTestMethod(test, Long.class, null, -65L).longValue());
  }

  @Test
  public void test_IAND() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a & 342;}");
    assertEquals(265463 & 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LAND() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a & 342L;}");
    assertEquals(265463L & 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IOR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a | 342;}");
    assertEquals(265463 | 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LOR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a | 342L;}");
    assertEquals(265463L | 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IXOR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ return a ^ 342;}");
    assertEquals(265463 ^ 342, executeTestMethod(test, Integer.class, null, 265463).intValue());
  }

  @Test
  public void test_LXOR() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public long test(long a){ return a ^ 342L;}");
    assertEquals(265463L ^ 342L, executeTestMethod(test, Long.class, null, 265463L).longValue());
  }

  @Test
  public void test_IINC() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new IINC(1, 13), new ILOAD(1), new IRETURN());
    assertEquals(265463 + 13, executeTestMethod(test, Integer.class, null, 265463).longValue());
  }

  @Test
  public void test_IINC_Wide() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ISTORE(280), new IINC(280, 13), new ILOAD(280), new IRETURN());
    assertEquals(265463 + 13, executeTestMethod(test, Integer.class, null, 265463).longValue());
  }

  @Test
  public void test_I2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(3), new I2L(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, 3L, null}, stack);
  }

  @Test
  public void test_I2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ICONST(3), new I2D(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{null, 3.0d, null}, stack);
  }

  @Test
  public void test_I2B() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new I2B(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((byte) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((byte) 123334), null}, stack);
  }

  @Test
  public void test_I2C() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new I2C(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((char) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((char) 123334), null}, stack);
  }

  @Test
  public void test_I2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new I2F(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 234);
    assertStack(new Object[]{234.0f, null}, stack);
  }

  @Test
  public void test_I2S() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new I2S(), new RETURN());
    executeTestMethod(test, Integer.class, stack, -123334);
    assertStack(new Object[]{(int) ((short) -123334), null}, stack);
    executeTestMethod(test, Integer.class, stack, 123334);
    assertStack(new Object[]{(int) ((short) 123334), null}, stack);
  }

  @Test
  public void test_L2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new L2I(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{1234, null}, stack);
  }

  @Test
  public void test_L2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new L2F(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{1234.0f, null}, stack);
  }

  @Test
  public void test_L2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.LONG, new LLOAD(1), new L2D(), new RETURN());
    executeTestMethod(test, Long.class, stack, 1234L);
    assertStack(new Object[]{null, 1234.0d, null}, stack);
  }

  @Test
  public void test_D2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new D2I(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{-1234, null}, stack);
  }

  @Test
  public void test_D2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new D2L(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{null, -1234L, null}, stack);
  }

  @Test
  public void test_D2F() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new LLOAD(1), new D2F(), new RETURN());
    executeTestMethod(test, Double.class, stack, -1234.234d);
    assertStack(new Object[]{Double.valueOf(-1234.234d).floatValue(), null}, stack);
  }

  @Test
  public void test_F2I() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new F2I(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{1234, null}, stack);
  }

  @Test
  public void test_F2L() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new F2L(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{null, 1234L, null}, stack);
  }

  @Test
  public void test_F2D() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new F2D(), new RETURN());
    executeTestMethod(test, Float.class, stack, 1234.123f);
    assertStack(new Object[]{null, Float.valueOf(1234.123f).doubleValue(), null}, stack);
  }

  @Test
  public void test_LCMP_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(1), new LCONST(1), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_LCMP_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(0), new LCONST(1), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_LCMP_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new LCONST(1), new LCONST(0), new LCMP(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new FCONST(1.0f), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new FCONST(1.0f), new FCONST(0.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new FCONST(0.0f), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new FCONST(1.0f), new FCMPL(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPL_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FCONST(1.0f), new FLOAD(1), new FCMPL(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPG_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FLOAD(1), new FCONST(1.0f), new FCMPG(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_FCMPG_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.FLOAT, new FCONST(1.0f), new FLOAD(1), new FCMPG(), new RETURN());
    executeTestMethod(test, Float.class, stack, Float.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_eq() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new DCONST(1.0d), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{0, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_less() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new DCONST(1.0d), new DCONST(0.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_greater() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new DCONST(0.0d), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Integer.class, stack, 0);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new DCONST(1.0d), new DCMPL(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPL_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DLOAD(1), new DCMPL(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{-1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPG_nan1() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DLOAD(1), new DCONST(1.0d), new DCMPG(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_DCMPG_nan2() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.DOUBLE, new DCONST(1.0d), new DLOAD(1), new DCMPG(), new RETURN());
    executeTestMethod(test, Double.class, stack, Double.NaN);
    assertStack(new Object[]{1, null, null, null, null, null}, stack);
  }

  @Test
  public void test_IFEQ() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFEQ.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 34).intValue());
  }

  @Test
  public void test_IFNE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFNE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 0).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 34).intValue());
  }

  @Test
  public void test_IFLT() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFLT.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, -5).intValue());
  }

  @Test
  public void test_IFLE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFLE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFGT() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFGT.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFGE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new Branch(IFGE.class, 4), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, -5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 0).intValue());
  }

  @Test
  public void test_IFICMPEQ() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPEQ.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPNE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPNE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPLT() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPLT.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPLE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPLE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPGT() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPGT.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFICMPGE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.INT, new ILOAD(1), new ICONST(5), new Branch(IF_ICMPGE.class, 5), new ICONST(3), new IRETURN(), new ICONST(2), new IRETURN());
    assertEquals(3, executeTestMethod(test, Integer.class, null, 3).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 5).intValue());
    assertEquals(2, executeTestMethod(test, Integer.class, null, 8).intValue());
  }

  @Test
  public void test_IFACMPEQ() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new ACONST_NULL(), new Branch(IF_ACMPEQ.class, 5), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{3, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_IFACMPNE() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new ACONST_NULL(), new Branch(IF_ACMPNE.class, 5), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{3, null, null, null}, stack);
  }

  @Test
  public void test_GOTO() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new Branch(GOTO.class, 3), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_GOTO_W() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new Branch(GOTO_W.class, 3), new ICONST(3), new RETURN(), new ICONST(2), new RETURN());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_JSR_RET() throws Throwable {
    final Object[] stack = new Object[100];
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new Branch(JSR.class, 2), new RETURN(), new ASTORE(85), new ICONST(2), new RET(85));
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_WIDE_JSR_RET() throws Throwable {
    final Object[] stack = new Object[100];
    final List<Object> instr = new ArrayList<Object>();
    instr.add(new Branch(JSR.class, 2 + 65535));
    instr.add(new RETURN());
    for (int i = 0; i < 65535; i++) {
      instr.add(new NOP());
    }
    instr.add(new ASTORE(288));
    instr.add(new ICONST(2));
    instr.add(new RET(288));

    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, instr.toArray());
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test
  public void test_LOOKUPSWITCH() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){"
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
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ sfield=a; return sfield;}");
    assertEquals(9942343, executeTestMethod(test, Integer.class, null, 9942343).intValue());
  }

  @Test
  public void test_GETSTATIC_PUTSTATIC_otherClass() throws Throwable {
    final AtomicBoolean calledSet = new AtomicBoolean();
    final AtomicBoolean calledGet = new AtomicBoolean();

    final JJJVMProvider processor = new TestProviderImpl() {

      @Override
      public void setStatic(final JJJVMClass source, final String className, final String fieldName, final String fieldSignature, final Object value) {
        assertEquals("com/igormaznitsa/jjjvm/testclasses/TestObject", className);
        assertEquals("sfield", fieldName);
        assertEquals("I", fieldSignature);

        assertFalse(calledSet.get());
        calledSet.set(true);

        assertEquals(Integer.valueOf(9942343), value);
      }

      @Override
      public Object getStatic(final JJJVMClass source, final String className, final String fieldName, final String fieldSignature) {
        assertEquals("com/igormaznitsa/jjjvm/testclasses/TestObject", className);
        assertEquals("sfield", fieldName);
        assertEquals("I", fieldSignature);

        assertFalse(calledGet.get());
        calledGet.set(true);

        return 23334;
      }

    };

    final JJJVMClassImpl test = prepareTestClass(processor, "public int test(int a){ com.igormaznitsa.jjjvm.testclasses.TestObject.sfield=a; return com.igormaznitsa.jjjvm.testclasses.TestObject.sfield;}");
    assertEquals(23334, executeTestMethod(test, Integer.class, null, 9942343).intValue());

    assertTrue(calledGet.get());
    assertTrue(calledSet.get());
  }

  @Test
  public void test_GETFIRLD_PUTFIELD_thisClass() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), "public int test(int a){ field=a; return field;}");
    assertEquals(9942343, executeTestMethod(test, Integer.class, null, 9942343).intValue());
  }

  @Test
  public void test_GETFIELD_PUTFIELD_otherClass() throws Throwable {
    final AtomicBoolean calledSet = new AtomicBoolean();
    final AtomicBoolean calledGet = new AtomicBoolean();

    final JJJVMProvider processor = new TestProviderImpl() {
      private TestObject obj;

      @Override
      public Object allocate(JJJVMClass source, String jvmFormattedClassName) {
        assertEquals("com/igormaznitsa/jjjvm/testclasses/TestObject", jvmFormattedClassName);
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

    final JJJVMClassImpl test = prepareTestClass(processor, "public int test(int a){ com.igormaznitsa.jjjvm.testclasses.TestObject obj = new com.igormaznitsa.jjjvm.testclasses.TestObject(); obj.field=a; return obj.field;}");
    assertEquals(23334, executeTestMethod(test, Integer.class, null, 9942343).intValue());

    assertTrue(calledGet.get());
    assertTrue(calledSet.get());
  }

  @Test
  public void testIFNULL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new Branch(IFNULL.class, 4), new ICONST(1), new RETURN(), new ICONST(2), new RETURN());
    final Object[] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{2, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{1, null, null, null}, stack);
  }

  @Test
  public void testIFNONNULL() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new Branch(IFNONNULL.class, 4), new ICONST(1), new RETURN(), new ICONST(2), new RETURN());
    final Object[] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{1, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, new Object());
    assertStack(new Object[]{2, null, null, null}, stack);
  }

  @Test(expected = NullPointerException.class)
  public void testATHROW_null() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new ATHROW());
    executeTestMethod(test, Object.class, null, (Object) null);
  }

  @Test(expected = RuntimeException.class)
  public void testATHROW_RTE() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new ATHROW());
    executeTestMethod(test, Object.class, null, new RuntimeException("TEST"));
  }

  @Test
  public void testATHROW_NonThrowableObject() throws Throwable {
    final Object obj = new Object();

    final JJJVMProvider provider = new TestProviderImpl() {

      @Override
      public void doThrow(JJJVMClass caller, Object exception) throws Throwable {
        assertNotNull(caller);
        assertSame(obj, exception);
        throw new IOException("HEHEHE");
      }

    };

    final JJJVMClassImpl test = prepareTestClass(provider, Type.OBJECT, new ALOAD(1), new ATHROW());
    try {
      executeTestMethod(test, Object.class, null, obj);
      fail("Must throw IOE");
    }
    catch (IOException ex) {
    }
  }

  @Test
  public void testCHECKCAST() throws Throwable {
    final Object fake = new Object();

    final AtomicBoolean flag = new AtomicBoolean();

    final JJJVMProvider proc = new TestProviderImpl() {
      @Override
      public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) {
        assertEquals(CONST_CLASS, jvmFormattedClassName);
        assertEquals(fake, value);
        return flag.get();
      }
    };

    final JJJVMClassImpl test = prepareTestClass(proc, Type.OBJECT, new ALOAD(1), new CHECKCAST(CP_CLASS), new ARETURN());
    flag.set(true);
    assertEquals(null, executeTestMethod(test, Object.class, null, (Object) null));
    assertEquals(fake, executeTestMethod(test, Object.class, null, fake));
    try {
      flag.set(false);
      executeTestMethod(test, Object.class, null, fake);
      fail("Must throw CCE");
    }
    catch (ClassCastException ex) {

    }
  }

  @Test
  public void testINSTANCEOF() throws Throwable {
    final Object fake = new Object();

    final JJJVMProvider proc = new TestProviderImpl() {

      @Override
      public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) {
        assertEquals(CONST_CLASS, jvmFormattedClassName);
        assertEquals(fake, value);
        return true;
      }
    };

    final JJJVMClassImpl test = prepareTestClass(proc, Type.OBJECT, new ALOAD(1), new INSTANCEOF(CP_CLASS), new RETURN());
    final Object[] stack = new Object[100];
    executeTestMethod(test, Object.class, stack, (Object) null);
    assertStack(new Object[]{0, null, null, null}, stack);
    executeTestMethod(test, Object.class, stack, fake);
    assertStack(new Object[]{1, null, null, null}, stack);
  }

  @Test
  public void testMONITORENTER_MONITOREXIT_jjjvmobject() throws Throwable {
    final JJJVMObject obj = new JJJVMObject(null, null);
    final JJJVMClassImpl test1 = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new MONITORENTER(), new ALOAD(1), new ARETURN());
    final JJJVMClassImpl test2 = prepareTestClass(new TestProviderImpl(), Type.OBJECT, new ALOAD(1), new MONITOREXIT(), new ALOAD(1), new ARETURN());

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

    final JJJVMProvider prov = new TestProviderImpl() {
      @Override
      public void doMonitor(final JJJVMClass caller, final Object object, final boolean lock) {
        counter.incrementAndGet();
        assertNotNull(caller);
        assertSame(obj, object);
        locked.set(lock);
      }
    };

    final JJJVMClassImpl test1 = prepareTestClass(prov, Type.OBJECT, new ALOAD(1), new MONITORENTER(), new ALOAD(1), new ARETURN());
    final JJJVMClassImpl test2 = prepareTestClass(prov, Type.OBJECT, new ALOAD(1), new MONITOREXIT(), new ALOAD(1), new ARETURN());

    assertFalse(locked.get());
    executeTestMethod(test1, Object.class, null, obj);
    assertTrue(locked.get());
    executeTestMethod(test2, Object.class, null, obj);
    assertFalse(locked.get());

    assertEquals(2, counter.get());
  }

  @Test
  public void testIntegration_TestTableswitch() throws Throwable {
    final JSEProviderImpl provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestTableswitch");

    assertTrue(JSEProviderImpl.tryCastTo(testKlazz, "java/lang/Object"));
    assertFalse(JSEProviderImpl.tryCastTo(testKlazz, "java/util/Map"));
    assertTrue(JSEProviderImpl.tryCastTo(testKlazz, "java/io/Serializable"));

    assertEquals(Integer.valueOf(1234), testKlazz.findField("sfield1").getStaticValue());
    assertEquals(Long.valueOf(56787L), testKlazz.findField("sfield2").getStaticValue());
    assertEquals("Hello world", testKlazz.findField("sfield3").getStaticValue());

    final Double arg = new Double(11233.0932d);

    final JJJVMObject obj = testKlazz.newInstance("(D)V", new Object[]{arg}, null, null);
    assertNotNull(obj);
    assertSame(testKlazz, obj.getDeclaringClass());
    assertSame(arg, testKlazz.findField("field4").get(obj));
    assertEquals(Integer.valueOf(9876), testKlazz.findField("field1").get(obj));
    assertEquals(Long.valueOf(6666L), testKlazz.findField("field2").get(obj));
    assertEquals("Ugums", testKlazz.findField("field3").get(obj));

    final JJJVMMethod doCalcMethod = testKlazz.findMethod("doCalc", "(D)D");

    assertEquals(new Double(11233.0932d + -652374.23d / 34 - (23 * -652374.23d)), doCalcMethod.invoke(obj, new Object[]{new Double(-652374.23d)}));

    final JJJVMMethod doTableSwitchMethod = testKlazz.findMethod("doTableSwitch", "(J)D");
    assertEquals(new Double(1.0d), doTableSwitchMethod.invoke(obj, new Object[]{-1L}));
    assertEquals(new Double(1.3d), doTableSwitchMethod.invoke(obj, new Object[]{0L}));
    assertEquals(new Double(2.0d), doTableSwitchMethod.invoke(obj, new Object[]{1L}));
    assertEquals(new Double(8.0d), doTableSwitchMethod.invoke(obj, new Object[]{2L}));
    assertEquals(new Double(81.0d), doTableSwitchMethod.invoke(obj, new Object[]{9L}));
    assertEquals(new Double(93 * 93), doTableSwitchMethod.invoke(obj, new Object[]{93L}));
  }

  @Test
  public void testIntegration_TestInvoke() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestInvoke");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMMethod calc = testKlazz.findMethod("calc", "(I)I");
    final JJJVMMethod calcLong = testKlazz.findMethod("calc", "(J)J");
    assertEquals((125 * 125) / 2 + 10, calc.invoke(obj, new Object[]{new Integer(125)}));
    assertEquals((125L * 125L) / 2L + 10L, calcLong.invoke(obj, new Object[]{new Long(125L)}));
  }

  @Test
  public void testIntegration_TestVector() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestVector");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMMethod fillVector = testKlazz.findMethod("fillVector", "(Ljava/util/Vector;)Ljava/util/Vector;");
    final JJJVMMethod makeVector = testKlazz.findMethod("makeVector", "()Ljava/util/Vector;");

    final Vector theVector = new Vector();
    theVector.add("1");
    theVector.add("2");
    theVector.add("3");

    final Vector result = (Vector) fillVector.invoke(obj, new Object[]{theVector});
    assertSame(theVector, result);
    assertArrayEquals(new Object[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}, result.toArray());
    assertArrayEquals(new Object[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, ((Vector) makeVector.invoke(obj, null)).toArray());
  }

  @Test
  public void testIntegration_TestIterable() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestIterable");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMMethod iterableMethod = testKlazz.findMethod("iterate", "(Ljava/util/List;)I");

    assertEquals(0,iterableMethod.invoke(obj, new Object[]{Collections.EMPTY_LIST}));
    List list = new ArrayList();
    list.add(1);
    assertEquals(1,iterableMethod.invoke(obj, new Object[]{list}));
    list.add(2);
    assertEquals(2,iterableMethod.invoke(obj, new Object[]{list}));
    list.clear();
    assertEquals(0,iterableMethod.invoke(obj, new Object[]{list}));
  }

  @Test
  public void testClassNameAndVersion() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestInnerClasses$NonStaticClass");

    assertEquals("must be Java 1.5", 0x0031, testKlazz.getClassFormatVersion());
    assertEquals("com/igormaznitsa/jjjvm/testclasses/TestInnerClasses$NonStaticClass", testKlazz.getClassName());
    assertEquals("com.igormaznitsa.jjjvm.testclasses.TestInnerClasses$NonStaticClass", testKlazz.getName());
    assertEquals("com.igormaznitsa.jjjvm.testclasses.TestInnerClasses.NonStaticClass", testKlazz.getCanonicalName());
  }

  @Test
  public void testIntegration_TestInnerClasses() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestInnerClasses");
    assertEquals(0, JJJVMClassImpl.getNumberOfLoadingClasses());

    assertEquals("TestInnerClasses.java", testKlazz.getSourceFileName());
    final JJJVMObject instance = testKlazz.newInstance(true);

    final JJJVMMethod test = testKlazz.findMethod("test", "(II)I");
    assertEquals(96, test.invoke(instance, new Object[]{123, 456}));
  }

  @Test
  public void testIntegration_TestThrow() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestThrow");

    final JJJVMObject obj = testKlazz.newInstance(true);

    final JJJVMMethod throwIOE = testKlazz.findMethod("throwIOE", "()V");
    final JJJVMMethod throwDIFF = testKlazz.findMethod("throwDIFF", "(I)V");

    try {
      throwIOE.invoke(obj, null);
      fail("Must throw IOE");
    }
    catch (IOException ex) {
      assertEquals("IOE", ex.getMessage());
      assertEquals(8, ex.getStackTrace()[0].getLineNumber());
    }

    try {
      throwDIFF.invoke(obj, new Object[]{0});
      fail("Must throw AE");
    }
    catch (ArithmeticException ex) {
      assertEquals("arith", ex.getMessage());
      assertEquals(15, ex.getStackTrace()[0].getLineNumber());
    }

    try {
      throwDIFF.invoke(obj, new Object[]{1});
      fail("Must throw UOE");
    }
    catch (UnsupportedOperationException ex) {
      assertEquals("uns", ex.getMessage());
      assertEquals(17, ex.getStackTrace()[0].getLineNumber());
    }

    try {
      throwDIFF.invoke(obj, new Object[]{9993});
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
      assertEquals("ise", ex.getMessage());
      assertEquals(23, ex.getStackTrace()[0].getLineNumber());
      assertEquals("npe", ex.getCause().getMessage());
    }
  }

  @Test
  public void testIntegration_TestClassInheritance() throws Throwable {
    final JJJVMProvider provider = new JSEProviderImpl(this);
    final JJJVMClass testKlazz = loadClassFromClassPath(provider, "com/igormaznitsa/jjjvm/testclasses/TestClassInheritance$Klazz3");
    final JJJVMObject obj = testKlazz.newInstance(true);
    obj.setFieldValue("field1", 123, true);
    obj.setFieldValue("field2", 345, true);
    obj.setFieldValue("field3", 678, true);

    assertEquals(123.456d, ((Double) testKlazz.readStaticField("dblStatField")), 0.0d);

    assertEquals(Integer.valueOf(123), obj.getFieldValue("field1", true));
    assertEquals(Integer.valueOf(345), obj.getFieldValue("field2", true));
    assertEquals(Integer.valueOf(678), obj.getFieldValue("field3", true));

    assertEquals((123 * 999) / 345 + 678, testKlazz.findMethod("calc", "(I)I").invoke(obj, new Object[]{999}));
  }

  @Test
  public void testIntegration_FillMultidimensionalArray_Int() throws Throwable {
    final JJJVMClassImpl test = prepareTestClass(new JSEProviderImpl(this),
            "public java.lang.Object test(java.lang.Object a){"
            + " int [][] array = new int[2][10];"
            + " int index = 0;"
            + " for(int x=0; x<array.length; x++){"
            + "  int [] fill = array[x];"
            + "  for(int y=0; y< fill.length; y++){"
            + "    fill[y] = index++;"
            + "  }"
            + " }"
            + " return array;"
            + "}"
    );

    final int[][] result = (int[][]) executeTestMethod(test, Object.class, null, null);
    assertEquals(2, result.length);
    assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, result[0]);
    assertArrayEquals(new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, result[1]);
  }

  @Test
  public void testSynchronization_staticMethods() throws Throwable {

    final AtomicLong callCounter = new AtomicLong();

    final JJJVMProvider provider = new JSEProviderImpl(this) {
      final AtomicInteger counter = new AtomicInteger();

      @Override
      public Object invoke(JJJVMClass caller, Object instance, String jvmFormattedClassName, String methodName, String methodSignature, Object[] arguments) throws Throwable {
        if (jvmFormattedClassName.equals("java/io/PrintStream") && methodName.equals("println") && methodSignature.equals("(I)V")) {
          synchronized (counter) {
            callCounter.incrementAndGet();
            final int value = (Integer) arguments[0];
            
            if (counter.get() == 0) {
              counter.set(value == 1 ? 1 + 2 + 3 + 4 + 5 : 6 + 7 + 8 + 9 + 10);
              counter.addAndGet(-value);
            }
            else {
              assertTrue("Counter must be every time zero or greater", counter.addAndGet(-value) >= 0);
            }
          }
          return null;
        }
        return super.invoke(caller, instance, jvmFormattedClassName, methodName, methodSignature, arguments);
      }
    };

    final JJJVMClass klazz = loadClassFromClassPath(provider, "com.igormaznitsa.jjjvm.testclasses.TestSynchronization");

    final JJJVMMethod smethod1 = klazz.findMethod("smethod1", "()V");
    final JJJVMMethod smethod2 = klazz.findMethod("smethod2", "()V");

    assertNotNull(smethod1);
    assertNotNull(smethod2);

    final JJJVMObject obj = klazz.newInstance(true);

    final int NUM = 100000;

    final Thread thr1 = new Thread(new Runnable() {

      public void run() {
        try {
          Thread.sleep(200L);
          for (int i = 0; i < NUM; i++) {
            smethod1.invoke(null, null);
          }
        }
        catch (Throwable ex) {
          ex.printStackTrace();
          fail("Error in thread 1");
        }
      }
    });

    final Thread thr2 = new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(200L);
          for (int i = 0; i < NUM; i++) {
            smethod2.invoke(null, null);
          }
        }
        catch (Throwable ex) {
          ex.printStackTrace();
          fail("Error in thread 2");
        }
      }
    });

    thr1.start();
    thr2.start();

    thr1.join();
    thr2.join();

    assertTrue(callCounter.get() != 0);
  }

  @Test
  public void testSynchronization_nonstaticMethods() throws Throwable {

    final AtomicLong callCounter = new AtomicLong();

    final JJJVMProvider provider = new JSEProviderImpl(this) {
      final AtomicInteger counter = new AtomicInteger();

      @Override
      public Object invoke(JJJVMClass caller, Object instance, String jvmFormattedClassName, String methodName, String methodSignature, Object[] arguments) throws Throwable {
        if (jvmFormattedClassName.equals("java/io/PrintStream") && methodName.equals("println") && methodSignature.equals("(I)V")) {
          synchronized (counter) {
            callCounter.incrementAndGet();
            final int value = (Integer) arguments[0];
                        
            if (counter.get() == 0) {
              counter.set(value == 1 ? 1 + 2 + 3 + 4 + 5 : 6 + 7 + 8 + 9 + 10);
              counter.addAndGet(-value);
            }
            else {
              assertTrue("Counter must be every time zero or greater", counter.addAndGet(-value) >= 0);
            }
          }
          return null;
        }
        return super.invoke(caller, instance, jvmFormattedClassName, methodName, methodSignature, arguments);
      }
    };

    final JJJVMClass klazz = loadClassFromClassPath(provider, "com.igormaznitsa.jjjvm.testclasses.TestSynchronization");

    final JJJVMMethod smethod1 = klazz.findMethod("method1", "()V");
    final JJJVMMethod smethod2 = klazz.findMethod("method2", "()V");

    assertNotNull(smethod1);
    assertNotNull(smethod2);

    final JJJVMObject obj = klazz.newInstance(true);

    final int NUM = 100000;

    final Thread thr1 = new Thread(new Runnable() {

      public void run() {
        try {
          Thread.sleep(200L);
          for (int i = 0; i < NUM; i++) {
            smethod1.invoke(obj, null);
          }
        }
        catch (Throwable ex) {
          ex.printStackTrace();
          fail("Error in thread 1");
        }
      }
    });

    final Thread thr2 = new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(200L);
          for (int i = 0; i < NUM; i++) {
            smethod2.invoke(obj, null);
          }
        }
        catch (Throwable ex) {
          ex.printStackTrace();
          fail("Error in thread 2");
        }
      }
    });

    thr1.start();
    thr2.start();

    thr1.join();
    thr2.join();

    assertTrue(callCounter.get() != 0);
  }

}
