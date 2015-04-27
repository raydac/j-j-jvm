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
package com.igormaznitsa.jjjvm;

import java.io.DataInputStream;
import java.io.IOException;

public final class JJJVMClassField {
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
  private static final String ATTRIBUTE_CONSTANTVALUE = "ConstantValue";
  //---------------------------
  private final JJJVMClass declaringClass;
  private final int flags;
  private final String name;
  private final String signature;
  private final int constantValue;
  private final int fieldUID;
  private Object staticValue;

  public void setStaticValue(final Object value) {
    this.staticValue = value;
  }

  public Object getStaticValue() {
    return this.staticValue;
  }

  JJJVMClassField(final JJJVMClass declaringClass, final DataInputStream inStream) throws IOException {
    this.declaringClass = declaringClass;
    int theConstantValue = -1;
    this.staticValue = null;
    // flags
    this.flags = inStream.readUnsignedShort();
    // name
    final int nameIndex = inStream.readUnsignedShort();
    this.name = (String) declaringClass.getConstantPool().get(nameIndex).asString();
    // type
    final int typeIndex = inStream.readUnsignedShort();
    this.signature = (String) declaringClass.getConstantPool().get(typeIndex).asString();
    this.fieldUID = (nameIndex << 16) | typeIndex;
    // attributes
    int attributesCounter = inStream.readUnsignedShort();
    while (--attributesCounter >= 0) {
      final String attrName = (String) declaringClass.getConstantPool().get(inStream.readUnsignedShort()).asString();
      if (ATTRIBUTE_CONSTANTVALUE.equals(attrName)) {
        final int attributeSize = inStream.readInt();
        if (attributeSize != 2) {
          throw new IOException("Wrong size for constant value attribute [" + attributeSize + ']');
        }
        theConstantValue = inStream.readUnsignedShort();
      }
      else {
        // ignore all other attributes
        inStream.skipBytes((inStream.readInt()));
      }
    }

    this.constantValue = theConstantValue;
  }

  public int getUID() {
    return fieldUID;
  }

  public Object get(final JJJVMObject instance) {
    if ((flags & ACC_STATIC) == 0) {
      return instance.get(this.name,true);
    }
    else {
      return this.staticValue;
    }
  }

  public void set(final JJJVMObject instance, final Object value) {
    if ((flags & ACC_STATIC) == 0) {
      instance.set(this.name, value, true);
    }
    else {
      this.staticValue = value;
    }
  }

  public Object getConstantValue() {
    if (this.constantValue < 0) {
      return null;
    }
    return this.declaringClass.getConstantPool().get(this.constantValue).asObject();
  }

  public int getFlags() {
    return flags;
  }

  public String getName() {
    return this.name;
  }

  public String getSignature() {
    return this.signature;
  }

}
