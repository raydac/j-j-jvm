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

package com.igormaznitsa.jjjvm.impl;

import com.igormaznitsa.jjjvm.model.JJJVMClass;
import com.igormaznitsa.jjjvm.model.JJJVMField;
import com.igormaznitsa.jjjvm.model.JJJVMObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a class field.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.5}
 */
public final class JJJVMClassFieldImpl implements JJJVMField {

  private final JJJVMClassImpl declaringClass;
  private final int flags;
  private final String name;
  private final String signature;
  private final int constantIndexInPool;
  private final int fieldUID;
  private volatile Object staticValue;

  private static final Map<String, Object> DEFAULT_VALUES = new HashMap<String, Object>();

  static {
    DEFAULT_VALUES.put("B", (byte) 0);
    DEFAULT_VALUES.put("C", '\u0000');
    DEFAULT_VALUES.put("D", 0.0d);
    DEFAULT_VALUES.put("F", 0.0f);
    DEFAULT_VALUES.put("I", 0);
    DEFAULT_VALUES.put("J", 0L);
    DEFAULT_VALUES.put("S", (short) 0);
    DEFAULT_VALUES.put("Z", false);
  }

  /**
   * Write static value in the field.
   *
   * @param value object to be saved
   * @param force if true then set value even for final field
   * @throws IllegalStateException if the field is either non static or is final
   */
  public void setStaticValue(final Object value, final boolean force) {
    if ((this.flags & ACC_STATIC) == 0) {
      throw new IllegalStateException("Field '" + this.name + "' is not static");
    } else {
      if ((this.flags & ACC_FINAL) == 0 || force) {
        this.staticValue = value;
      } else {
        throw new IllegalStateException("Field '" + this.name + "' is final");
      }
    }
  }

  /**
   * Read static value from the field
   *
   * @return object from the field
   * @throws IllegalStateException if the field is non static
   */
  public Object getStaticValue() {
    if ((this.flags & ACC_STATIC) == 0) {
      throw new IllegalStateException("Field '" + this.name + "' is not static");
    } else {
      return this.staticValue;
    }
  }

  JJJVMClassFieldImpl(final JJJVMClassImpl declaringClass, final DataInputStream inStream) throws IOException {
    this.declaringClass = declaringClass;
    int theConstantValueIndex = -1;
    this.staticValue = null;

    // flags
    this.flags = inStream.readUnsignedShort();

    // name
    final int nameIndex = inStream.readUnsignedShort();
    this.name = (String) declaringClass.getConstantPool().getItemAt(nameIndex).asString();

    // type
    final int typeIndex = inStream.readUnsignedShort();
    this.signature = (String) declaringClass.getConstantPool().getItemAt(typeIndex).asString();
    this.fieldUID = (nameIndex << 16) | typeIndex;

    // attributes
    int attributesCounter = inStream.readUnsignedShort();

    while (--attributesCounter >= 0) {
      final String attrName = (String) declaringClass.getConstantPool().getItemAt(inStream.readUnsignedShort()).asString();
      if (ATRNAME_CONSTANTVALUE.equals(attrName)) {
        final int attributeSize = inStream.readInt();
        if (attributeSize != 2) {
          throw new IOException("Wrong size for constant value attribute [" + attributeSize + ']');
        }
        theConstantValueIndex = inStream.readUnsignedShort();
      } else {
        // ignore all other attributes
        JJJVMImplUtils.skip(inStream, inStream.readInt());
      }
    }

    this.constantIndexInPool = theConstantValueIndex;
    if ((this.flags & ACC_STATIC) != 0) {
      if (theConstantValueIndex >= 0) {
        this.staticValue = this.getConstantValue();
      } else {
        this.staticValue = DEFAULT_VALUES.get(this.signature);
      }
    }
  }

  public int getUID() {
    return fieldUID;
  }

  public Object get(final JJJVMObject instance) {
    if ((flags & ACC_STATIC) == 0) {
      return instance.getFieldValue(this.name, true);
    } else {
      return this.staticValue;
    }
  }

  public void set(final JJJVMObject instance, final Object value) {
    if ((flags & ACC_STATIC) == 0) {
      instance.setFieldValue(this.name, value, true);
    } else {
      this.staticValue = value;
    }
  }

  public Object getConstantValue() {
    if (this.constantIndexInPool <= 0) {
      return null;
    }
    return this.declaringClass.getConstantPool().getItemAt(this.constantIndexInPool).asObject();
  }

  public JJJVMClass getDeclaringClass() {
    return this.declaringClass;
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

  @Override
  public String toString() {
    return this.getClass().getCanonicalName() + '[' + this.declaringClass.getName() + '#' + this.getName() + ' ' + this.getSignature() + ']';
  }
}
