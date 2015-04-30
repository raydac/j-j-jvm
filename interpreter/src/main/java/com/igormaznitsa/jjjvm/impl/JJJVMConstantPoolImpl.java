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
import com.igormaznitsa.jjjvm.model.JJJVMConstantPool;
import com.igormaznitsa.jjjvm.model.JJJVMConstantPoolItem;
import java.io.DataInputStream;
import java.io.IOException;

public class JJJVMConstantPoolImpl implements JJJVMConstantPool {

  private final JJJVMConstantPoolItem[] records;
  private final JJJVMClass klazz;

  public JJJVMConstantPoolImpl(final JJJVMClass klazz, final DataInputStream inStream) throws IOException {
    int index = 0;

    this.klazz = klazz;

    int itemsNumber = inStream.readUnsignedShort();
    this.records = new JJJVMConstantPoolItem[itemsNumber];
    this.records[index++] = null;
    itemsNumber--;

    final StringBuilder strBuffer = new StringBuilder(128);

    while (itemsNumber > 0) {
      boolean doubleRecordItem = false;
      final int recordType = inStream.readUnsignedByte();
      final Object recordValue;
      switch (recordType) {
        case JJJVMConstantPoolItem.CONSTANT_UTF8: {
          recordValue = inStream.readUTF();
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_UNICODE: {
          final int len = inStream.readUnsignedShort();
          for (int i = 0; i < len; i++) {
            char ch_char = (char) inStream.readUnsignedShort();
            strBuffer.append(ch_char);
          }
          recordValue = strBuffer.toString();
          strBuffer.setLength(0);
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_INTEGER: {
          recordValue = inStream.readInt();
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_FLOAT: {
          recordValue = inStream.readFloat();
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_LONG: {
          recordValue = inStream.readLong();
          doubleRecordItem = true;
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_DOUBLE: {
          recordValue = inStream.readDouble();
          doubleRecordItem = true;
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_CLASSREF:
        case JJJVMConstantPoolItem.CONSTANT_STRING: {
          recordValue = inStream.readUnsignedShort();
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_FIELDREF:
        case JJJVMConstantPoolItem.CONSTANT_METHODREF:
        case JJJVMConstantPoolItem.CONSTANT_INTERFACEMETHOD:
        case JJJVMConstantPoolItem.CONSTANT_NAMETYPEREF:
        case JJJVMConstantPoolItem.CONSTANT_METHODHANDLE:
        case JJJVMConstantPoolItem.CONSTANT_INVOKEDYNAMIC: {
          final int high = inStream.readUnsignedShort();
          final int low = inStream.readUnsignedShort();
          recordValue = (high << 16) | low;
        }
        break;
        case JJJVMConstantPoolItem.CONSTANT_METHODTYPE: {
          final int descIndex = inStream.readUnsignedShort();
          recordValue = descIndex;
        }
        break;
        default: {
          throw new IOException("Unsupported constant pool item [" + recordType + ']');
        }
      }

      this.records[index++] = new JJJVMConstantPoolItem(this, recordType, recordValue);
      if (doubleRecordItem) {
        itemsNumber--;
        index++;
      }
      itemsNumber--;
    }
  }

  public JJJVMClass getDeclaringClass() {
    return this.klazz;
  }

  public JJJVMConstantPoolItem getItemAt(final int index) {
    return this.records[index];
  }

  public int size() {
    return this.records.length;
  }

}
