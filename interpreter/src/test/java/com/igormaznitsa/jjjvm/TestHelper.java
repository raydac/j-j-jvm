package com.igormaznitsa.jjjvm;

import java.io.*;
import javassist.*;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.generic.*;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

public abstract class TestHelper {

  protected static final int CONST_INT = 12345;
  protected static final String CONST_CLASS = "com/test/klass/Some";
  protected static final String CONST_STR = "hello world";
  protected static final double CONST_DBL = 1234.56789d;
  protected static final float CONST_FLT = 1234.56f;
  protected static final long CONST_LNG = 1234567890L;

  protected static final int CP_INT = 15;
  protected static final int CP_CLASS = 17;
  protected static final int CP_STRING = 19;
  protected static final int CP_DOUBLE = 20;
  protected static final int CP_FLOAT = 22;
  protected static final int CP_LONG = 23;

  protected static final int CP_INT_H = 325;
  protected static final int CP_CLASS_H = 327;
  protected static final int CP_STRING_H = 329;
  protected static final int CP_DOUBLE_H = 330;
  protected static final int CP_FLOAT_H = 332;
  protected static final int CP_LONG_H = 333;

  protected static final int CONST_INT_H = 123456;
  protected static final String CONST_CLASS_H = "com.test.klass.Some2";
  protected static final String CONST_STR_H = "hello world11";
  protected static final double CONST_DBL_H = 1238.56789d;
  protected static final float CONST_FLT_H = 1214.56f;
  protected static final long CONST_LNG_H = 1200567890L;

  protected static JJJVMClass prepareTestClass(final JJJVMProvider processor, final String method) throws Throwable {
    final ClassPool classPool = ClassPool.getDefault();
    final CtClass clazz = classPool.makeClass("com.igormaznitsa.SyntheticMJVMTest");
    clazz.addField(CtField.make("public static int sfield;", clazz));
    clazz.addField(CtField.make("public int field = 123;", clazz));
    clazz.addMethod(CtNewMethod.make(method, clazz));
    final JJJVMClass result = new JJJVMClass(new ByteArrayInputStream(clazz.toBytecode()), processor);
    clazz.detach();
    return result;
  }

  protected static JJJVMClass loadClassFromClassPath(final JJJVMProvider provider, final String className) throws Throwable {
    final ClassPool classPool = ClassPool.getDefault();
    final CtClass klazz = classPool.get(className);
    return new JJJVMClass(new ByteArrayInputStream(klazz.toBytecode()), provider);
  }
  
  protected static JJJVMClass prepareTestClass(final JJJVMProvider processor, final Type type, final Object... instructions) throws Throwable {
    final ClassGen clazzGen = new ClassGen("com.igormaznitsa.SyntheticMJVMTest", "java.lang.Object", "SyntheticMJVMTest.java", Constants.ACC_PUBLIC, null);
    clazzGen.addEmptyConstructor(Constants.ACC_PUBLIC);

    final ConstantPoolGen cpg = clazzGen.getConstantPool();
    Assert.assertEquals(CP_INT, cpg.addInteger(CONST_INT));
    Assert.assertEquals(CP_CLASS, cpg.addClass(CONST_CLASS));
    Assert.assertEquals(CP_STRING, cpg.addString(CONST_STR));
    Assert.assertEquals(CP_DOUBLE, cpg.addDouble(CONST_DBL));
    Assert.assertEquals(CP_FLOAT, cpg.addFloat(CONST_FLT));
    Assert.assertEquals(CP_LONG, cpg.addLong(CONST_LNG));

    for (int i = 0; i < 300; i++) {
      cpg.addInteger(i + 11);
    }

    Assert.assertEquals(CP_INT_H, cpg.addInteger(CONST_INT_H));
    Assert.assertEquals(CP_CLASS_H, cpg.addClass(CONST_CLASS_H));
    Assert.assertEquals(CP_STRING_H, cpg.addString(CONST_STR_H));
    Assert.assertEquals(CP_DOUBLE_H, cpg.addDouble(CONST_DBL_H));
    Assert.assertEquals(CP_FLOAT_H, cpg.addFloat(CONST_FLT_H));
    Assert.assertEquals(CP_LONG_H, cpg.addLong(CONST_LNG_H));

    final InstructionList instructionList = new InstructionList();
    for (final Object i : instructions) {
      if (i instanceof  Instruction){
        instructionList.append((Instruction)i);
      }
      else if (i instanceof Branch){
        instructionList.append(((Branch)i).getInstance());
      }
    }
    
    instructionList.update();
    for (final Object i : instructions) {
      if (i instanceof Branch) {
        ((Branch)i).update(instructionList);
      }
    }
    
    final MethodGen testMethod = new MethodGen(Constants.ACC_PUBLIC, type, new Type[]{type}, new String[]{"arg"}, "test", "com.igormaznitsa.SyntheticMJVMTest", instructionList, clazzGen.getConstantPool());
    testMethod.setMaxLocals(300);
    testMethod.setMaxStack(64);

    testMethod.update();

    final FieldGen sfgen = new FieldGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.INT, "sfield", cpg);
    clazzGen.addField(sfgen.getField());
    
    clazzGen.addField(new FieldGen(Constants.ACC_PUBLIC, Type.INT, "field", cpg).getField());
    
    clazzGen.addMethod(testMethod.getMethod());
    clazzGen.update();

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    clazzGen.getJavaClass().dump(buffer);
    buffer.close();

    final byte[] bbb = buffer.toByteArray();
    FileUtils.writeByteArrayToFile(new File("/home/igorm/1/SyntheticMJVMTest.class"), bbb);

    return new JJJVMClass(new ByteArrayInputStream(buffer.toByteArray()), processor);
  }

  protected <K> K executeTestMethod(final JJJVMClass clazz, final Class<K> type, final Object[] stack, final Object... args) throws Throwable {
    final JJJVMObject obj = clazz.newInstance(true);

    final Type bceltype;

    if (type == Integer.class) {
      bceltype = Type.INT;
    }
    else if (type == Byte.class) {
      bceltype = Type.BYTE;
    }
    else if (type == Short.class) {
      bceltype = Type.SHORT;
    }
    else if (type == Character.class) {
      bceltype = Type.CHAR;
    }
    else if (type == Long.class) {
      bceltype = Type.LONG;
    }
    else if (type == Float.class) {
      bceltype = Type.FLOAT;
    }
    else if (type == Double.class) {
      bceltype = Type.DOUBLE;
    }
    else if (type == Boolean.class) {
      bceltype = Type.BOOLEAN;
    }
    else {
      bceltype = Type.getType(type);
    }

    final JJJVMClassMethod method = clazz.findDeclaredMethod("test", "(" + bceltype.getSignature() + ")" + bceltype.getSignature());
    Assert.assertNotNull("Method not found", method);

    Object result = clazz.invoke(obj, method, args, stack, null);

    if (type == Byte.class) {
      result = ((Integer)result).byteValue();
    }else
    if (type == Short.class) {
      result = ((Integer)result).shortValue();
    }else
    if (type == Character.class) {
      result = (char)((Integer)result).shortValue();
    }else
    if (type == Boolean.class) {
      result = ((Integer)result).byteValue()!=0;
    }
    return type.cast(result);
  }

  protected void assertStack(final Object [] etalon, final Object [] stack){
    for(int i=0;i<etalon.length;i++){
      assertEquals(etalon[i], stack[i]);
    }
  }
  
}
