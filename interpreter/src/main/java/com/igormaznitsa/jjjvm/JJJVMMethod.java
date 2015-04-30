package com.igormaznitsa.jjjvm;

public interface JJJVMMethod {
  // flags of a method
  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SYNCHRONIZED = 0x0020;
  public static final int ACC_BRIDGE = 0x0040;
  public static final int ACC_VARARGS = 0x0080;
  public static final int ACC_NATIVE = 0x0100;
  public static final int ACC_ABSTRACT = 0x0400;
  public static final int ACC_STRICT = 0x0800;
  public static final int ACC_SYNTHETIC = 0x1000;
  //-----------------------------------------
  // types of data
  public static final char TYPE_BYTE = 'B';
  public static final char TYPE_CHAR = 'C';
  public static final char TYPE_DOUBLE = 'D';
  public static final char TYPE_FLOAT = 'F';
  public static final char TYPE_INT = 'I';
  public static final char TYPE_LONG = 'J';
  public static final char TYPE_SHORT = 'S';
  public static final char TYPE_BOOLEAN = 'Z';
  public static final char TYPE_VOID = 'V';
  public static final char TYPE_CLASS = 'L';
  public static final char TYPE_ARRAY = '[';
  //-----------------------------------------
  public static final String ATTRIBUTE_EXCEPTIONS = "Exceptions";
  public static final String ATTRIBUTE_CODE = "Code";
  //-----------------------------------------
  JJJVMKlazz getDeclaringClass();
  int getFlags();
  String getName();
  String getSignature();

  public int getMaxLocals();

  public int getMaxStackDepth();

  public byte[] getBytecode();

  public JJJVMCatchBlockDescriptor[] getCatchBlockDescriptors();
  public Object invoke(JJJVMObject instance, Object[] arguments) throws Throwable;

}
