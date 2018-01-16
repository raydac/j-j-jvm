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

    int ACC_PUBLIC = 0x0001;
    int ACC_PRIVATE = 0x0002;
    int ACC_PROTECTED = 0x0004;
    int ACC_STATIC = 0x0008;
    int ACC_FINAL = 0x0010;
    int ACC_SYNCHRONIZED = 0x0020;
    int ACC_METHOD_BRIDGE = 0x0040;
    int ACC_VOLATILE = 0x0040;
    int ACC_METHOD_VARARGS = 0x0080;
    int ACC_TRANSIENT = 0x0080;
    int ACC_NATIVE = 0x0100;
    int ACC_INTERFACE = 0x0200;
    int ACC_ABSTRACT = 0x0400;
    int ACC_STRICT = 0x0800;
    int ACC_SYNTHETIC = 0x1000;
    int ACC_ENUM = 0x4000;

    char TYPE_BYTE = 'B';
    char TYPE_CHAR = 'C';
    char TYPE_DOUBLE = 'D';
    char TYPE_FLOAT = 'F';
    char TYPE_INT = 'I';
    char TYPE_LONG = 'J';
    char TYPE_SHORT = 'S';
    char TYPE_BOOLEAN = 'Z';
    char TYPE_VOID = 'V';
    char TYPE_CLASS = 'L';
    char TYPE_ARRAY = '[';

    String ATTRNAME_EXCEPTIONS = "Exceptions";
    String ATTRNAME_CODE = "Code";
    String ATRNAME_CONSTANTVALUE = "ConstantValue";
    String ATTRNAME_INNERCLASSES = "InnerClasses";
    String ATTRNAME_SOURCEFILE = "SourceFile";

    String[] EMPTY_STRING_ARRAY = new String[0];
    JJJVMInnerClassRecord[] EMPTY_INNERCLASS_ARRAY = new JJJVMInnerClassRecord[0];
    JJJVMTryCatchRecord[] EMPTY_CATCBLOCK_ARRAY = new JJJVMTryCatchRecord[0];

}
