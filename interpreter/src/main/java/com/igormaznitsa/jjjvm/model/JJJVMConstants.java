/* 
 * Copyright 2015 Igor Maznitsa (http://www.igormaznitsa.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.jjjvm.model;

public interface JJJVMConstants {

  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SYNCHRONIZED = 0x0020;
  public static final int ACC_METHOD_BRIDGE = 0x0040;
  public static final int ACC_VOLATILE = 0x0040;
  public static final int ACC_METHOD_VARARGS = 0x0080;
  public static final int ACC_TRANSIENT = 0x0080;
  public static final int ACC_NATIVE = 0x0100;
  public static final int ACC_INTERFACE = 0x0200;
  public static final int ACC_ABSTRACT = 0x0400;
  public static final int ACC_STRICT = 0x0800;
  public static final int ACC_SYNTHETIC = 0x1000;
  public static final int ACC_ENUM = 0x4000;

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

  public static final String ATTRNAME_EXCEPTIONS = "Exceptions";
  public static final String ATTRNAME_CODE = "Code";
  public static final String ATRNAME_CONSTANTVALUE = "ConstantValue";
  public static final String ATTRNAME_INNERCLASSES = "InnerClasses";
  public static final String ATTRNAME_SOURCEFILE = "SourceFile";
  public static final String ATTRNAME_LINENUMBERTABLE = "LineNumberTable";

  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final JJJVMInnerClassRecord[] EMPTY_INNERCLASS_ARRAY = new JJJVMInnerClassRecord[0];
  public static final JJJVMTryCatchRecord[] EMPTY_CATCBLOCK_ARRAY = new JJJVMTryCatchRecord[0];

}
