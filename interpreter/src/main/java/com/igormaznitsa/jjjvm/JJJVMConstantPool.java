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

public final class JJJVMConstantPool {

  public static final class Record {

    /**
     * Constant pool UTF8 string item.
     */
    public static final int CONSTANT_UTF8 = 1;
    /**
     * Constant pool UNICODE string item.
     */
    public static final int CONSTANT_UNICODE = 2;
    /**
     * Constant pool INTEGER item.
     */
    public static final int CONSTANT_INTEGER = 3;
    /**
     * Constant pool FLOAT item.
     */
    public static final int CONSTANT_FLOAT = 4;
    /**
     * Constant pool LONG item.
     */
    public static final int CONSTANT_LONG = 5;
    /**
     * Constant pool DOUBLE item.
     */
    public static final int CONSTANT_DOUBLE = 6;
    /**
     * Constant pool Class Reference item.
     */
    public static final int CONSTANT_CLASSREF = 7;
    /**
     * Constant pool String Reference item.
     */
    public static final int CONSTANT_STRING = 8;
    /**
     * Constant pool Field Reference item.
     */
    public static final int CONSTANT_FIELDREF = 9;
    /**
     * Constant pool Method Reference item.
     */
    public static final int CONSTANT_METHODREF = 10;
    /**
     * Constant pool INTERFACE METHOD instance.
     */
    public static final int CONSTANT_INTERFACEMETHOD = 11;
    /**
     * Constant pool NAME+TYPE Reference item.
     */
    public static final int CONSTANT_NAMETYPEREF = 12;
    public static final int CONSTANT_METHODHANDLE = 15;
    public static final int CONSTANT_METHODTYPE = 16;
    public static final int CONSTANT_INVOKEDYNAMIC = 18;

    private final int type;
    private final Object value;
    private final JJJVMConstantPool cpool;

    private Record(final JJJVMConstantPool cp, final int type, final Object value) {
      this.cpool = cp;
      this.type = type;
      this.value = value;
    }

    public int getType() {
      return this.type;
    }

    public Object getValue() {
      return this.value;
    }

    public int asInt() {
      return (Integer) this.value;
    }

    public Integer asInteger() {
      return (Integer) this.value;
    }

    public Double asDouble() {
      return (Double) this.value;
    }

    public Float asFloat() {
      return (Float) this.value;
    }

    public Long asLong() {
      return (Long) this.value;
    }

    public String asString() {
      switch (this.type) {
        case CONSTANT_UTF8:
        case CONSTANT_UNICODE:
          return (String) this.value;
        case CONSTANT_CLASSREF:
        case CONSTANT_STRING:
          return this.cpool.records[this.asInt()].asString();
        default:
            throw new IllegalArgumentException("Can't be presented as String [" + this.type + ']');
      }
    }

    private int extractHighUShort() {
      return this.asInt() >>> 16;
    }

    private int extractLowUShort() {
      return this.asInt() & 0xFFFF;
    }

    public String getClassName() {
      final String result;
      switch (this.type) {
        case CONSTANT_CLASSREF: {
          result = this.cpool.records[this.asInt()].asString();
        }
        break;
        case CONSTANT_METHODREF:
        case CONSTANT_INTERFACEMETHOD:
        case CONSTANT_FIELDREF: {
          result = this.cpool.get(extractHighUShort()).asString();
        }
        break;
        default: {
          throw new IllegalArgumentException("Illegal constant pool item");
        }
      }
      return result;
    }

    public String getSignature() {
      final String result;
      switch (this.type) {
        case CONSTANT_NAMETYPEREF: {
          result = this.cpool.get(this.extractLowUShort()).asString();
        }
        break;
        case CONSTANT_METHODREF:
        case CONSTANT_INTERFACEMETHOD:
        case CONSTANT_FIELDREF: {
          result = this.cpool.get(this.cpool.get(extractLowUShort()).extractLowUShort()).asString();
        }
        break;
        default: {
          throw new IllegalArgumentException("Illegal constant pool item");
        }
      }
      return result;
    }

    public String getName() {
      final String result;
      switch (this.type) {
        case CONSTANT_NAMETYPEREF: {
          result = this.cpool.get(this.extractHighUShort()).asString();
        }
        break;
        case CONSTANT_METHODREF:
        case CONSTANT_INTERFACEMETHOD:
        case CONSTANT_FIELDREF: {
          result = this.cpool.get(this.cpool.get(this.extractLowUShort()).extractHighUShort()).asString();
        }
        break;
        default: {
          throw new IllegalArgumentException("Illegal constant pool item");
        }
      }
      return result;
    }

    public Object asObject() {
      return this.value;
    }
  }

  private final Record[] records;
  private final JJJVMClass klazz;

  public JJJVMConstantPool(final JJJVMClass klazz, final DataInputStream inStream) throws IOException {
    int index = 0;
    
    this.klazz = klazz;

    int itemsNumber = inStream.readUnsignedShort();
    this.records = new Record[itemsNumber];
    this.records[index++] = null;
    itemsNumber--;

    final StringBuilder strBuffer = new StringBuilder(128);

    while(itemsNumber > 0) {
      boolean doubleRecordItem = false;
      final int recordType = inStream.readUnsignedByte();
      final Object recordValue;
      switch (recordType) {
        case Record.CONSTANT_UTF8: {
          recordValue = inStream.readUTF();
        }
        break;
        case Record.CONSTANT_UNICODE: {
          final int len = inStream.readUnsignedShort();
          for (int i = 0; i < len; i++) {
            char ch_char = (char) inStream.readUnsignedShort();
            strBuffer.append(ch_char);
          }
          recordValue = strBuffer.toString();
          strBuffer.setLength(0);
        }
        break;
        case Record.CONSTANT_INTEGER: {
          recordValue = inStream.readInt();
        }
        break;
        case Record.CONSTANT_FLOAT: {
          recordValue = inStream.readFloat();
        }
        break;
        case Record.CONSTANT_LONG: {
          recordValue = inStream.readLong();
          doubleRecordItem = true;
        }
        break;
        case Record.CONSTANT_DOUBLE: {
          recordValue = inStream.readDouble();
          doubleRecordItem = true;
        }
        break;
        case Record.CONSTANT_CLASSREF: 
        case Record.CONSTANT_STRING: {
          recordValue = inStream.readUnsignedShort();
        }
        break;
        case Record.CONSTANT_FIELDREF:
        case Record.CONSTANT_METHODREF:
        case Record.CONSTANT_INTERFACEMETHOD: 
        case Record.CONSTANT_NAMETYPEREF: 
        case Record.CONSTANT_METHODHANDLE: 
        case Record.CONSTANT_INVOKEDYNAMIC: {
          final int high = inStream.readUnsignedShort();
          final int low = inStream.readUnsignedShort();
          recordValue = (high << 16) | low;
        }
        break;
        case Record.CONSTANT_METHODTYPE: {
          final int descIndex = inStream.readUnsignedShort();
          recordValue = descIndex;
        }
        break;
        default: {
          throw new IOException("Unsupported constant pool item [" + recordType + ']');
        }
      }

      this.records[index++] = new Record(this, recordType, recordValue);
      if (doubleRecordItem) {
        itemsNumber--;
        index++;
      }
      itemsNumber--;
    }
  }

  public JJJVMClass getKlazz() {
    return this.klazz;
  }

  public Record get(final int index) {
    return this.records[index];
  }

  public int size() {
    return this.records.length;
  }

}
