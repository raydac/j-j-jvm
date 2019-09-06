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

import com.igormaznitsa.jjjvm.model.*;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public abstract class JJJVMInterpreter implements JJJVMConstants {

  protected static final Map<String, Integer> CACHED_NUMBER_OF_ARGS = new HashMap<String, Integer>();

  /**
   * Invoke a method.
   *
   * @param caller
   * @param instance the 'this' object or null for static methods
   * @param methodToInvoke the method to invoke, must not be null
   * @param args array contains arguments for method, can be null for method
   * without arguments
   * @param stack predefined stack for the method, will be used only if the
   * provided stack is enough for the method else recreated version will be
   * used, it can be null
   * @param vars predefined local variable area, it will be recreated if
   * provided array is null or has not enough size
   * @return result of invocation, null for void method
   * @throws Throwable it will be thrown for errors
   */
  public static Object invoke(final JJJVMClass caller, final JJJVMObject instance, final JJJVMMethod methodToInvoke, final Object[] args, final Object[] stack, final Object[] vars) throws Throwable {
    final int methodFlags = methodToInvoke.getFlags();
    if ((methodFlags & ACC_NATIVE) != 0) {
      throw new IllegalArgumentException("Method must not be native [" + methodToInvoke + ']');
    }

    // implementation of synchronization mechanism
    if ((methodFlags & ACC_SYNCHRONIZED) != 0) {
      // it's a synchronized method
      final Object syncObject;

      if ((methodFlags & ACC_STATIC) != 0) {
        // it's a static method
        // we need to use class as the synchro object
        syncObject = caller;
      } else {
        // it's a nonstatic method
        // we need to use the instance as the synchro object
        syncObject = instance;
      }

      synchronized (syncObject) {
        return _invoke(caller, instance, methodToInvoke, args, 0, stack, vars);
      }
    } else {
      // it's not a synchronized method and we just call inside invoke function
      return _invoke(caller, instance, methodToInvoke, args, 0, stack, vars);
    }
  }

  // the Heart of the interpreter, it processes byte-code of method {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.3}
  protected static Object _invoke(final JJJVMClass caller, final JJJVMObject instance, final JJJVMMethod method, final Object[] args, final int initialStackOffset, final Object[] stack, final Object[] vars) throws Throwable {
    final Object[] localVars = vars == null || vars.length < method.getMaxLocals() ? new Object[method.getMaxLocals()] : vars;

    final Object[] localMethodStack;
    int regPC = 0;
    int regSP;

    if (stack == null) {
      localMethodStack = new Object[method.getMaxStackDepth()];
      regSP = 0;
    } else {
      if (stack.length - initialStackOffset >= method.getMaxStackDepth()) {
        localMethodStack = stack;
        regSP = initialStackOffset;
      } else {
        localMethodStack = new Object[method.getMaxStackDepth()];
        regSP = 0;
      }
    }

    // the variable contains the first local variable index contains the first method argument
    int firstArgument = 0;

    final int flags = method.getFlags();

    // check the method flags
    if ((flags & (ACC_ABSTRACT | ACC_STRICT)) != 0) {
      // decoding
      if ((flags & ACC_ABSTRACT) != 0) {
        throw new IllegalStateException("It's an abstract method");
      }
      if ((flags & ACC_STRICT) != 0) {
        throw new IllegalStateException("Strict methods not supported");
      }
    }

    final JJJVMConstantPool cpool = caller.getConstantPool();
    final JJJVMProvider provider = caller.getProvider();

    // if the method is not static, we will need to place "this" in the zero-indexed local variable
    if ((flags & ACC_STATIC) == 0) {
      // place "this"
      localVars[0] = instance;
      // the first argument will be at the index 1
      firstArgument = 1;
    }

    // fill the method stack with arguments
    if (args != null) {
      for (final Object arg : args) {
        localVars[firstArgument++] = arg;
      }
    }

    // the string below to increase the speed
    final byte[] methodBytecodes = method.getBytecode();

    // the flag will be used by the WIDE command
    boolean nextInstructionWide = false;

    while (true) {
      final int lastPC = regPC;
      try {
        final int instruction = methodBytecodes[regPC++] & 0xFF;

        switch (instruction) {
          case 0: // NOP
          {
          }
          break;
          case 1: // ACONST_NULL
          {
            localMethodStack[regSP++] = null;
          }
          break;
          case 2: // ICONST_M1 
          {
            localMethodStack[regSP++] = -1;
          }
          break;
          case 3: // ICONST_0
          case 4: // ICONST_1
          case 5: // ICONST_2
          case 6: // ICONST_3
          case 7: // ICONST_4
          case 8: // ICONST_5
          {
            localMethodStack[regSP++] = instruction - 3;
          }
          break;
          case 9:  // LCONST_0
          case 10: // LCONST_1
          {
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = (long) instruction - 9;
          }
          break;
          case 11: // FCONST_0 
          {
            localMethodStack[regSP++] = 0.0f;
          }
          break;
          case 12: // FCONST_1
          {
            localMethodStack[regSP++] = 1.0f;
          }
          break;
          case 13: // FCONST_2
          {
            localMethodStack[regSP++] = 2.0f;
          }
          break;
          case 14: // DCONST_0
          {
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = 0.0d;
          }
          break;
          case 15: // DCONST_1
          {
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = 1.0d;
          }
          break;
          case 16: // BIPUSH
          {
            localMethodStack[regSP++] = (int) methodBytecodes[regPC++];
          }
          break;
          case 17: // SIPUSH
          {
            final int val = readShortValueFromArray(methodBytecodes, regPC);
            regPC += 2;
            localMethodStack[regSP++] = val;
          }
          break;
          case 18: // LDC
          case 19: // LDC_W
          {
            int index = methodBytecodes[regPC++] & 0xFF;
            if (instruction == 19) {
              // wide index
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
            }

            final JJJVMConstantPoolItem record = cpool.getItemAt(index);
            switch (record.getType()) {
              case JJJVMConstantPoolItem.CONSTANT_INTEGER:
              case JJJVMConstantPoolItem.CONSTANT_FLOAT: {
                localMethodStack[regSP++] = record.getValue();
              }
              break;
              case JJJVMConstantPoolItem.CONSTANT_STRING: {
                localMethodStack[regSP++] = record.asString();
              }
              break;
              case JJJVMConstantPoolItem.CONSTANT_CLASSREF: {
                final String jvmFormattedClassName = record.getClassName();
                final Object clazz = provider.resolveClass(jvmFormattedClassName);
                if (clazz == null) {
                  throw new IllegalArgumentException("Can't resolve class [" + jvmFormattedClassName + ']');
                }
                localMethodStack[regSP++] = clazz;
              }
              break;
              case JJJVMConstantPoolItem.CONSTANT_METHODTYPE:
              case JJJVMConstantPoolItem.CONSTANT_METHODHANDLE:
                throw new UnsupportedOperationException("Method type and Method handle is not supported");
              default:
                throw new Error("Unsupported constant type for LDC [" + record.getType() + ']');
            }
          }
          break;
          case 20: // LDC2W
          {
            final int index = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;

            final JJJVMConstantPoolItem record = cpool.getItemAt(index);
            switch (record.getType()) {
              case JJJVMConstantPoolItem.CONSTANT_DOUBLE:
              case JJJVMConstantPoolItem.CONSTANT_LONG: {
                localMethodStack[regSP++] = null;
              }
              break;
              default:
                throw new Error("Unsupported constant type for LDC2W [" + record.getType() + ']');
            }
            localMethodStack[regSP++] = record.getValue();
          }
          break;
          case 22: // LLOAD
          {
            int index = methodBytecodes[regPC++] & 0xFF;
            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = localVars[index];
          }
          break;
          case 24: // DLOAD
          {
            int index = methodBytecodes[regPC++] & 0xFF;
            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = localVars[index];
          }
          break;
          case 21: // ILOAD 
          case 23: // FLOAD
          case 25: // ALOAD
          {
            int index = methodBytecodes[regPC++] & 0xFF;
            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }
            localMethodStack[regSP++] = localVars[index];
          }
          break;
          case 26: // ILOAD_0
          case 27: // ILOAD_1
          case 28: // ILOAD_2
          case 29: // ILOAD_3
          {
            localMethodStack[regSP++] = localVars[instruction - 26];
          }
          break;
          case 30: // LLOAD_0
          case 31: // LLOAD_1
          case 32: // LLOAD_2
          case 33: // LLOAD_3
          {
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = localVars[instruction - 30];
          }
          break;
          case 34: // FALOAD_0
          case 35: // FALOAD_1
          case 36: // FALOAD_2
          case 37: // FALOAD_3
          {
            localMethodStack[regSP++] = localVars[instruction - 34];
          }
          break;
          case 38: // DLOAD_0
          case 39: // DLOAD_1
          case 40: // DLOAD_2
          case 41: // DLOAD_3
          {
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = localVars[instruction - 38];
          }
          break;
          case 42: // ALOAD_0
          case 43: // ALOAD_1
          case 44: // ALOAD_2
          case 45: // ALOAD_3
          {
            localMethodStack[regSP++] = localVars[instruction - 42];
          }
          break;
          case 46: // IALOAD
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final int[] array = (int[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = array[index];
          }
          break;
          case 47: // LALOAD
          {
            int index = toNumber(localMethodStack[--regSP]).intValue();
            final long[] array = (long[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = array[index];
          }
          break;
          case 48: // FALOAD
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final float[] array = (float[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = array[index];
          }
          break;
          case 49: // DALOAD
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final double[] array = (double[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = null;
            localMethodStack[regSP++] = array[index];
          }
          break;
          case 50: // AALOAD
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final Object[] array = (Object[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = array[index];
          }
          break;
          case 51: // BALOAD
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final Object arrayObj = localMethodStack[--regSP];
            if (arrayObj instanceof boolean[]) {
              final boolean[] boolArray = (boolean[]) arrayObj;
              localMethodStack[regSP++] = boolArray[index] ? 1 : 0;
            } else {
              // byte
              final byte[] byteArray = (byte[]) arrayObj;
              localMethodStack[regSP++] = (int) byteArray[index];
            }
          }
          break;
          case 52: // CALOAD 
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final char[] charArray = (char[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = (int) charArray[index];
          }
          break;
          case 53: // SALOAD 
          {
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            final short[] shortArray = (short[]) localMethodStack[--regSP];
            localMethodStack[regSP++] = (int) shortArray[index];
          }
          break;
          case 55: // LSTORE
          case 57: // DSTORE
          {
            int index = methodBytecodes[regPC++] & 0xFF;

            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }

            localVars[index] = localMethodStack[--regSP];
            --regSP;
          }
          break;
          case 54: // ISTORE
          case 56: // FSTORE
          case 58: // ASTORE
          {
            int index = methodBytecodes[regPC++] & 0xFF;

            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }

            localVars[index] = localMethodStack[--regSP];
          }
          break;
          case 59: // ISTORE_0
          case 60: // ISTORE_1
          case 61: // ISTORE_2
          case 62: // ISTORE_3
          {
            final Integer intValue = toNumber(localMethodStack[--regSP]).intValue();
            localVars[instruction - 59] = intValue;
          }
          break;
          case 63: // LSTORE_0
          case 64: // LSTORE_1
          case 65: // LSTORE_2
          case 66: // LSTORE_3
          {
            final Long longValue = toNumber(localMethodStack[--regSP]).longValue();
            localVars[instruction - 63] = longValue;
          }
          break;
          case 67: // FSTORE_0
          case 68: // FSTORE_1
          case 69: // FSTORE_2
          case 70: // FSTORE_3
          {
            final Float floatValue = toNumber(localMethodStack[--regSP]).floatValue();
            localVars[instruction - 67] = floatValue;
          }
          break;
          case 71: // DSTORE_0
          case 72: // DSTORE_1
          case 73: // DSTORE_2
          case 74: // DSTORE_3
          {
            final Double dblValue = toNumber(localMethodStack[--regSP]).doubleValue();
            localVars[instruction - 71] = dblValue;
          }
          break;
          case 75: // ASTORE_0
          case 76: // ASTORE_1
          case 77: // ASTORE_2
          case 78: // ASTORE_3
          {
            final Object value = localMethodStack[--regSP];
            localVars[instruction - 75] = value;
          }
          break;
          case 79: // IASTORE
          {
            final Integer value = toNumber(localMethodStack[--regSP]).intValue();
            final Integer index = toNumber(localMethodStack[--regSP]).intValue();
            ((int[]) localMethodStack[--regSP])[index] = value;
          }
          break;
          case 80: // LASTORE
          {
            final long value = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            ((long[]) localMethodStack[--regSP])[index] = value;
          }
          break;
          // FASTORE
          case 81: // FASTORE
          {
            final float value = toNumber(localMethodStack[--regSP]).floatValue();
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            ((float[]) localMethodStack[--regSP])[index] = value;
          }
          break;
          case 82: // DASTORE
          {
            final double value = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            ((double[]) localMethodStack[--regSP])[index] = value;
          }
          break;
          case 83: // AASTORE
          {
            final Object value = localMethodStack[--regSP];
            final int index = toNumber(localMethodStack[--regSP]).intValue();
            ((Object[]) localMethodStack[--regSP])[index] = value;
          }
          break;
          case 84: // BASTORE
          {
            final Object value = localMethodStack[--regSP];
            final int index = toNumber(localMethodStack[--regSP]).intValue();

            Object array = localMethodStack[--regSP];
            if (array instanceof boolean[]) {
              ((boolean[]) array)[index] = (Boolean) value;
            } else {
              ((byte[]) array)[index] = toNumber(value).byteValue();
            }
          }
          break;
          case 85: // CASTORE
          case 86: // SASTORE
          {
            final Object value = localMethodStack[--regSP];
            final int index = toNumber(localMethodStack[--regSP]).intValue();

            if (instruction == 85) {
              final char[] arr = (char[]) localMethodStack[--regSP];
              arr[index] = (Character) value;
            } else {
              final short[] arr = (short[]) localMethodStack[--regSP];
              arr[index] = (Short) value;
            }
          }
          break;
          case 87: // POP
          {
            localMethodStack[--regSP] = null;
          }
          break;
          case 88: // POP2
          {
            localMethodStack[--regSP] = null;
            localMethodStack[--regSP] = null;
          }
          break;
          case 89: // DUP
          {
            final Object obj = localMethodStack[regSP - 1];
            localMethodStack[regSP++] = obj;
          }
          break;
          case 90: // DUP_X1
          {
            final Object top = localMethodStack[regSP - 1];
            final Object sec = localMethodStack[regSP - 2];
            localMethodStack[regSP++] = top;
            localMethodStack[regSP - 3] = top;
            localMethodStack[regSP - 2] = sec;
          }
          break;
          case 91: // DUP_X2
          {
            final Object top = localMethodStack[regSP - 1];
            final Object sec = localMethodStack[regSP - 2];
            final Object thr = localMethodStack[regSP - 3];

            int index = regSP++;

            localMethodStack[index--] = top;
            localMethodStack[index--] = sec;
            localMethodStack[index--] = thr;
            localMethodStack[index] = top;
          }
          break;
          case 92: // DUP2
          {
            final Object top = localMethodStack[regSP - 1];
            final Object sec = localMethodStack[regSP - 2];
            localMethodStack[regSP++] = sec;
            localMethodStack[regSP++] = top;
          }
          break;
          case 93: // DUP2_X1
          {
            final Object top = localMethodStack[--regSP];
            final Object two = localMethodStack[--regSP];
            final Object three = localMethodStack[--regSP];

            localMethodStack[regSP++] = two;
            localMethodStack[regSP++] = top;
            localMethodStack[regSP++] = three;
            localMethodStack[regSP++] = two;
            localMethodStack[regSP++] = top;

          }
          break;
          case 94: // DUP2_X2
          {
            final Object top = localMethodStack[--regSP];
            final Object two = localMethodStack[--regSP];
            final Object three = localMethodStack[--regSP];
            final Object fourth = localMethodStack[--regSP];

            localMethodStack[regSP++] = two;
            localMethodStack[regSP++] = top;
            localMethodStack[regSP++] = fourth;
            localMethodStack[regSP++] = three;
            localMethodStack[regSP++] = two;
            localMethodStack[regSP++] = top;

          }
          break;
          case 95: // SWAP
          {
            final Object top = localMethodStack[regSP - 1];
            localMethodStack[regSP - 1] = localMethodStack[regSP - 2];
            localMethodStack[regSP - 2] = top;
          }
          break;
          case 96: // IADD
          {
            final int first = toNumber(localMethodStack[--regSP]).intValue();
            int sec = toNumber(localMethodStack[--regSP]).intValue();
            localMethodStack[regSP++] = first + sec;
          }
          break;
          case 97: // LADD
          {
            final long first = toNumber(localMethodStack[--regSP]).longValue();
            regSP--;
            final long second = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = first + second;
          }
          break;
          case 98: // FADD
          {
            final float first = toNumber(localMethodStack[--regSP]).floatValue();
            final float second = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = first + second;
          }
          break;
          case 99: // DADD
          {
            final double first = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final double second = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = first + second;
          }
          break;
          case 100: // ISUB
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a - b;
          }
          break;
          case 101: // LSUB
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a - b;
          }
          break;
          case 102: // FSUB
          {
            final float b = toNumber(localMethodStack[--regSP]).floatValue();
            final float a = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = a - b;
          }
          break;
          case 103: // DSUB
          {
            final double b = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final double a = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = a - b;
          }
          break;
          case 104: // IMUL
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a * b;
          }
          break;
          case 105: // LMUL
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a * b;
          }
          break;
          case 106: // FMUL 
          {
            final float b = toNumber(localMethodStack[--regSP]).floatValue();
            final float a = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = a * b;
          }
          break;
          case 107: // DMUL
          {
            final double b = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final double a = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = a * b;
          }
          break;
          case 108: // IDIV
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a / b;
          }
          break;
          case 109: // LDIV
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a / b;
          }
          break;
          case 110: // FDIV
          {
            final float b = toNumber(localMethodStack[--regSP]).floatValue();
            final float a = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = a / b;
          }
          break;
          case 111: // DDIV
          {
            final double b = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final double a = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = a / b;
          }
          break;
          case 112: // IREM
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a % b;
          }
          break;
          case 113: // LREM
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a % b;
          }
          break;
          case 114: // FREM
          {
            final float b = toNumber(localMethodStack[--regSP]).floatValue();
            final float a = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = a % b;
          }
          break;
          case 115: // DREM
          {
            final double b = toNumber(localMethodStack[--regSP]).doubleValue();
            --regSP;
            final double a = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = a % b;
          }
          break;
          case 116: // INEG
          {
            final int a = 0 - toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a;
          }
          break;
          case 117: // LNEG
          {
            final long a = 0 - toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a;
          }
          break;
          case 118: // FNEG
          {
            final float a = 0 - toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[regSP - 1] = a;
          }
          break;
          case 119: // DNEG 
          {
            final double a = 0 - toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[regSP - 1] = a;
          }
          break;
          case 120: // ISHL 
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a << b;
          }
          break;
          case 121: // LSHL
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a << b;
          }
          break;
          case 122: // ISHR
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a >> b;
          }
          break;
          case 123: // LSHR
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a >> b;
          }
          break;
          case 124: // IUSHR
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a >>> b;
          }
          break;
          case 125: // LUSHR
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a >>> b;
          }
          break;
          case 126: // IAND 
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a & b;
          }
          break;
          case 127: // LAND
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a & b;
          }
          break;
          case 128: // IOR 
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a | b;
          }
          break;
          case 129: // LOR 
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a | b;
          }
          break;
          case 130: // IXOR
          {
            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[regSP - 1]).intValue();
            localMethodStack[regSP - 1] = a ^ b;
          }
          break;
          case 131: // LXOR 
          {
            final long b = toNumber(localMethodStack[--regSP]).longValue();
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[regSP - 1] = a ^ b;
          }
          break;
          case 132: // IINC
          {
            int index = methodBytecodes[regPC++] & 0xFF;

            if (nextInstructionWide) {
              index = (index << 8) | (methodBytecodes[regPC++] & 0xFF);
            }

            int cons = methodBytecodes[regPC++];

            if (nextInstructionWide) {
              cons = (cons << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }

            localVars[index] = toNumber(localVars[index]).intValue() + cons;
          }
          break;
          case 133: // I2L
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[regSP - 1] = null;
            localMethodStack[regSP++] = value.longValue();
          }
          break;
          case 134: // I2F
          {
            final int top = regSP - 1;
            localMethodStack[top] = toNumber(localMethodStack[top]).floatValue();
          }
          break;
          case 135: // I2D
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[regSP - 1] = null;
            localMethodStack[regSP++] = value.doubleValue();
          }
          break;
          case 136: // L2I
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[--regSP] = null;
            localMethodStack[regSP - 1] = value.intValue();
          }
          break;
          case 137: // L2F
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[--regSP] = null;
            localMethodStack[regSP - 1] = value.floatValue();
          }
          break;
          case 138: // L2D
          {
            localMethodStack[regSP - 1] = toNumber(localMethodStack[regSP - 1]).doubleValue();
          }
          break;
          case 139: // F2I
          {
            localMethodStack[regSP - 1] = toNumber(localMethodStack[regSP - 1]).intValue();
          }
          break;
          case 140: // F2L
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[regSP - 1] = null;
            localMethodStack[regSP++] = value.longValue();
          }
          break;
          case 141: // F2D
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[regSP - 1] = null;
            localMethodStack[regSP++] = value.doubleValue();
          }
          break;
          case 142: // D2I
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[--regSP] = null;
            localMethodStack[regSP - 1] = value.intValue();
          }
          break;
          case 143: // D2L
          {
            localMethodStack[regSP - 1] = toNumber(localMethodStack[regSP - 1]).longValue();
          }
          break;
          case 144: // D2F 
          {
            final Number value = toNumber(localMethodStack[regSP - 1]);
            localMethodStack[--regSP] = null;
            localMethodStack[regSP - 1] = value.floatValue();
          }
          break;
          case 145: // I2B 
          {
            final int sp = regSP - 1;
            localMethodStack[sp] = (int) toNumber(localMethodStack[sp]).byteValue();
          }
          break;
          case 146: // I2C 
          {
            final int sp = regSP - 1;
            localMethodStack[sp] = (char)(toNumber(localMethodStack[sp]).intValue());
          }
          break;
          case 147: // I2S
          {
            final int sp = regSP - 1;
            localMethodStack[sp] = toNumber(localMethodStack[sp]).shortValue();
          }
          break;
          case 148: // LCMP
          {
            final long b = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[--regSP] = null;
            --regSP;
            final long a = toNumber(localMethodStack[regSP - 1]).longValue();
            localMethodStack[--regSP] = null;
            localMethodStack[regSP - 1] = a == b ? 0 : a > b ? 1 : -1;
          }
          break;
          case 149: // FCMPL
          case 150: // FCMPG
          {
            final float b = toNumber(localMethodStack[regSP - 1]).floatValue();
            localMethodStack[--regSP] = null;
            final int index = regSP - 1;
            final float a = toNumber(localMethodStack[index]).floatValue();
            if (Float.isNaN(a) || Float.isNaN(b)) {
              localMethodStack[index] = instruction == 150 ? 1 : -1;
            } else {
              localMethodStack[index] = Float.compare(a, b);
            }
          }
          break;
          case 151: // DCMPL
          case 152: // DCMPG
          {
            final double b = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[--regSP] = null;
            regSP--;
            final double a = toNumber(localMethodStack[regSP - 1]).doubleValue();
            localMethodStack[--regSP] = null;

            final int index = regSP - 1;

            if (Double.isNaN(a) || Double.isNaN(b)) {
              localMethodStack[index] = instruction == 152 ? 1 : -1;
            } else {
              localMethodStack[index] = Double.compare(a, b);
            }
          }
          break;
          case 153: // IFEQ
          case 154: // IFNE
          case 155: // IFLT
          case 156: // IFGE
          case 157: // IFGT
          case 158: // IFLE
          {
            final int jumpOffset = readShortValueFromArray(methodBytecodes, regPC);
            regPC += 2;
            
            final Object res = localMethodStack[--regSP];
            final int value;
            
            if (res instanceof Boolean) {
              value = ((Boolean) res) ? 1 : 0;
            } else {
               value = toNumber(res).intValue();
            }

            final boolean doJump;
            switch (instruction) {
              // IFEQ
              case 153:
                doJump = value == 0;
                break;
              // IFNE
              case 154:
                doJump = value != 0;
                break;
              // IFLT
              case 155:
                doJump = value < 0;
                break;
              // IFGE
              case 156:
                doJump = value >= 0;
                break;
              // IFGT
              case 157:
                doJump = value > 0;
                break;
              // IFLE
              case 158:
                doJump = value <= 0;
                break;
              default:
                throw new Error("Unexpected code");
            }
            if (doJump) {
              regPC = lastPC + jumpOffset;
            }
          }
          break;
          case 159: // IF_ICMPEQ
          case 160: // IF ICMPNE
          case 161: // IF_ICMPLT
          case 162: // IF_ICPMGE
          case 163: // IF_ICMPGT
          case 164: // IF_ICMPLE 
          {
            final int jumpOffset = readShortValueFromArray(methodBytecodes, regPC);
            regPC += 2;

            final int b = toNumber(localMethodStack[--regSP]).intValue();
            final int a = toNumber(localMethodStack[--regSP]).intValue();

            final boolean doJump;

            switch (instruction) {
              // IF_ICMPEQ
              case 159:
                doJump = a == b;
                break;
              // IF_CMPNE
              case 160:
                doJump = a != b;
                break;
              // IF_ICMPLT
              case 161:
                doJump = a < b;
                break;
              // IF_ICMPGE
              case 162:
                doJump = a >= b;
                break;
              // IF_ICMPGT
              case 163:
                doJump = a > b;
                break;
              // IF_ICMPLE
              case 164:
                doJump = a <= b;
                break;
              default:
                throw new Error("unexpected code");
            }
            if (doJump) {
              regPC = lastPC + jumpOffset;
            }
          }
          break;
          case 165: // IF_ACMPEQ
          case 166: // IF_ACMPNE
          {
            final int jumpOffset = readShortValueFromArray(methodBytecodes, regPC);
            regPC += 2;

            final Object a = localMethodStack[regSP - 1];
            localMethodStack[--regSP] = null;
            final Object b = localMethodStack[regSP - 1];
            localMethodStack[--regSP] = null;

            final boolean doJump;

            switch (instruction) {
              case 165: // IF_ACMPEQ
                doJump = a == b;
                break;
              case 166: // IF_ACMPNE
                doJump = a != b;
                break;
              default:
                throw new Error("unexpected code");
            }
            if (doJump) {
              regPC = lastPC + jumpOffset;
            }
          }
          break;
          case 167: // GOTO
          case 200: // GOTO_2 
          {
            regPC = lastPC + ((instruction == 200) ? readIntFromArray(methodBytecodes, regPC) : readShortValueFromArray(methodBytecodes, regPC));
          }
          break;
          case 168: // JSR
          case 201: // JSR_W 
          {
            final int jump;
            if (instruction == 168) {
              jump = readShortValueFromArray(methodBytecodes, regPC);
              regPC += 2;
            } else {
              jump = readIntFromArray(methodBytecodes, regPC);
              regPC += 4;
            }

            localMethodStack[regSP++] = regPC;
            regPC = lastPC + jump;
          }
          break;
          case 169: // RET
          {
            int localVarIndex = methodBytecodes[regPC++] & 0xFF;
            if (nextInstructionWide) {
              localVarIndex = (localVarIndex << 8) | (methodBytecodes[regPC++] & 0xFF);
              nextInstructionWide = false;
            }
            regPC = toNumber(localVars[localVarIndex]).intValue();
          }
          break;
          case 170: // TABLESWITCH
          {
            regPC += (4 - (regPC % 4));

            final int defaultAddr = readIntFromArray(methodBytecodes, regPC);
            regPC += 4;
            final int lowValue = readIntFromArray(methodBytecodes, regPC);
            regPC += 4;
            final int highValue = readIntFromArray(methodBytecodes, regPC);
            regPC += 4;

            final int value = toNumber(localMethodStack[--regSP]).intValue();
            int offset = defaultAddr;

            if (value >= lowValue && value <= highValue) {
              offset = readIntFromArray(methodBytecodes, regPC + ((value - lowValue) << 2));
            }

            regPC = lastPC + offset;
          }
          break;
          // LOOKUPSWITCH
          case 171: {
            // pad
            regPC += (4 - (regPC % 4));

            final int defaultAddr = readIntFromArray(methodBytecodes, regPC);
            regPC += 4;
            final int pairsNumber = readIntFromArray(methodBytecodes, regPC);
            regPC += 4;

            final int value = toNumber(localMethodStack[--regSP]).intValue();
            int offset = defaultAddr;

            for (int li = 0; li < pairsNumber; li++) {
              int indx = readIntFromArray(methodBytecodes, regPC);
              if (value == indx) {
                regPC += 4;
                offset = readIntFromArray(methodBytecodes, regPC);
                break;
              } else {
                regPC += 8;
              }
            }
            regPC = lastPC + offset;
          }
          break;
          case 175: // DRETURN
          case 173: // LRETURN
          {
            final Object val = localMethodStack[regSP - 1];
            localMethodStack[--regSP] = null;
            return val;
          }
          case 172: // IRETURN
          case 174: // FRETURN
          case 176: // ARETURN
          {
            // return without check, to increase speed
            return localMethodStack[--regSP];
          }
          case 177: // RETURN 
          {
            return null;
          }
          case 178: // GETSTATIC
          case 179: //  PUTSTATIC
          {
            final int poolIndex = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;
            final JJJVMConstantPoolItem fieldRef = cpool.getItemAt(poolIndex);

            final String className = fieldRef.getClassName();
            final String fieldName = fieldRef.getName();
            final String fieldSignature = fieldRef.getSignature();
            final Object resolvedClass = className.equals(caller.getClassName()) ? caller : provider.resolveClass(className);
            final Object value;

            if (resolvedClass instanceof JJJVMClass) {
              final JJJVMClass theclass = (JJJVMClass) resolvedClass;
              final JJJVMField thefield = theclass.findField(fieldName);
              if (instruction == 178) {
                value = thefield.getStaticValue();
                if (isCategory2(value)) {
                  localMethodStack[regSP++] = null;
                }
                localMethodStack[regSP++] = value;
              } else {
                thefield.setStaticValue(localMethodStack[--regSP], method.isClinit());
              }
            } else {
              if (instruction == 178) {
                value = provider.getStatic(caller, className, fieldName, fieldSignature);
                if (isCategory2(value)) {
                  localMethodStack[regSP++] = null;
                }
                localMethodStack[regSP++] = value;
              } else {
                value = localMethodStack[--regSP];
                provider.setStatic(caller, className, fieldName, fieldSignature, value, method.isClinit());
              }
            }
          }
          break;
          case 180: // GETFIELD
          case 181: // PUTFIELD 
          {
            final int poolIndex = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;

            final JJJVMConstantPoolItem fieldRef = cpool.getItemAt(poolIndex);
            final String fieldName = fieldRef.getName();

            if (instruction == 180) {
              // GET
              final Object value = localMethodStack[--regSP];

              if (value instanceof JJJVMObject) {
                final Object result = ((JJJVMObject) value).getFieldValue(fieldName, true);
                if (isCategory2(result)) {
                  localMethodStack[regSP++] = null;
                }
                localMethodStack[regSP++] = result;
              } else {
                final String fieldSignature = fieldRef.getSignature();
                localMethodStack[regSP++] = provider.get(caller, value, fieldName, fieldSignature);
              }
            } else {
              // PUT
              final Object value = localMethodStack[regSP - 1];
              localMethodStack[--regSP] = null;

              if (isCategory2(value)) {
                regSP--;
              }

              final Object objectINstance = localMethodStack[--regSP];

              if (objectINstance instanceof JJJVMObject) {
                ((JJJVMObject) objectINstance).setFieldValue(fieldName, value, true);
              } else {
                final String fieldSignature = fieldRef.getSignature();
                provider.set(caller, objectINstance, fieldName, fieldSignature, value);
              }
            }

          }
          break;
          case 182: // INVOKEVIRTUAL
          case 183: // INVOKESPECIAL
          case 184: // INVOKESTATIC
          case 185: // INVOKEINTERFACE
          {
            final int methodRef = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;

            final JJJVMConstantPoolItem record = cpool.getItemAt(methodRef);

            int argsNumber = extractArgsNumber(record.getSignature());

            final Object[] argsArray = new Object[argsNumber];
            while (argsNumber > 0) {
              argsNumber--;
              argsArray[argsNumber] = localMethodStack[--regSP];
            }

            Object objInstance = null;

            if (instruction != 184) {
              // take instance from stack
              objInstance = localMethodStack[--regSP];

              if (instruction == 185) {
                regPC += 2;
              }
            }

            final String methodName = record.getName();
            final String signature = record.getSignature();
            final String klazzName = record.getClassName();

            final Object resolvedKlazz;
            if (instruction == 185) {
              // INOKEINTERFACE
              resolvedKlazz = objInstance instanceof JJJVMObject ? ((JJJVMObject) objInstance).getDeclaringClass() : provider.resolveClass(objInstance.getClass().getName().replace('.', '/'));
            } else {
              resolvedKlazz = klazzName.equals(caller.getClassName()) ? caller : provider.resolveClass(klazzName);
            }
            final Object result;
            if (resolvedKlazz instanceof JJJVMClass) {
              final JJJVMMethod foundMethod = ((JJJVMClass) resolvedKlazz).findMethod(methodName, signature);
              final JJJVMClass jjjvmclazz = foundMethod.getDeclaringClass();
              result = _invoke(jjjvmclazz, (JJJVMObject) objInstance, foundMethod, argsArray, regSP, localMethodStack, null);
            } else {
              result = provider.invoke(caller, objInstance, klazzName, methodName, signature, argsArray);
              if (result != null && "<init>".equals(methodName)) {
                // replace all instances by new one
                for (int i = 0; i < localMethodStack.length; i++) {
                  if (localMethodStack[i] == objInstance) {
                    localMethodStack[i] = result;
                  }
                }
              }
            }

            if (signature.charAt(signature.length() - 1) != TYPE_VOID) {
              localMethodStack[regSP++] = result;
            }
          }
          break;
          case 186: // INVOKEDYNAMIC
          {
            throw new UnsupportedOperationException("INVOKEDYNAMIC is not supported");
          }
          case 187: // NEW
          {
            final int classRef = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;
            localMethodStack[regSP++] = provider.allocate(caller, cpool.getItemAt(classRef).asString());
          }
          break;
          case 188: // NEWARRAY
          {
            final int count = toNumber(localMethodStack[--regSP]).intValue();
            final int atype = methodBytecodes[regPC++] & 0xFF;

            final Object result;

            switch (atype) {
              case 4: // boolean
              {
                result = new boolean[count];
              }
              break;
              case 5: // char
              {
                result = new char[count];
              }
              break;
              case 8: // byte
              {
                result = new byte[count];
              }
              break;
              case 9: // short
              {
                result = new short[count];
              }
              break;
              case 10: // int
              {
                result = new int[count];
              }
              break;
              case 11: // long
              {
                result = new long[count];
              }
              break;
              case 6: // float
              {
                result = new float[count];
              }
              break;
              case 7: // double
              {
                result = new double[count];
              }
              break;
              default: {
                throw new Error("Unexpected array type [" + atype + ']');
              }
            }

            localMethodStack[regSP++] = result;
          }
          break;
          case 189: // ANEWARAY
          {
            final int index = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;

            final int count = toNumber(localMethodStack[--regSP]).intValue();
            final String className = cpool.getItemAt(index).getClassName();
            final Object[] objArray = provider.newObjectArray(caller, className, count);

            localMethodStack[regSP++] = objArray;
          }
          break;
          case 190: // ARRAYLENGTH
          {
            final int topIndex = regSP - 1;
            localMethodStack[topIndex] = Array.getLength(localMethodStack[topIndex]);
          }
          break;
          case 191: // ATWHROW
          {
            final Object throwable = localMethodStack[--regSP];
            
            if (throwable == null) {
              throw new NullPointerException("ATHROW NULL");
            }
            
            if (throwable instanceof Throwable) {
              throw (Throwable) throwable;
            } else {
              provider.doThrow(caller, throwable);
            }
          }
          break;
          case 192: // CHECKCAST
          case 193: // INSTANCEOF 
          {
            final int cpIndex = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;
            final String rawClassName = cpool.getItemAt(cpIndex).getClassName();
            final int index = regSP - 1;
            final Object object = localMethodStack[index];

            if (instruction == 192) {
              if (object != null) {
                if (!provider.checkCast(caller, rawClassName, object)) {
                  throw new ClassCastException(object.getClass().getName() + " -> " + rawClassName);
                }
              }
            } else {
              if (object == null) {
                localMethodStack[index] = 0;
              } else {
                localMethodStack[index] = provider.checkCast(caller, rawClassName, object) ? 1 : 0;
              }
            }
          }
          break;
          case 194: // MONITORENTER
          {
            final Object obj = localMethodStack[--regSP];
            if (obj == null) {
              throw new NullPointerException("Monitor is null");
            }

            if (obj instanceof JJJVMObject) {
              ((JJJVMObject) obj).lock();
            } else {
              provider.doMonitor(caller, obj, true);
            }
          }
          break;
          case 195: // MONITOREXIT
          {
            final Object obj = localMethodStack[--regSP];
            if (obj == null) {
              throw new NullPointerException("Monitor is null");
            }

            if (obj instanceof JJJVMObject) {
              ((JJJVMObject) obj).unlock();
            } else {
              provider.doMonitor(caller, obj, false);
            }
          }
          break;
          case 196: // WIDE
          {
            nextInstructionWide = true;
          }
          break;
          case 197: // MULTIANEWARRAY
          {
            final int classRefIndex = readShortValueFromArray(methodBytecodes, regPC) & 0xFFFF;
            regPC += 2;
            int dimensionsSize = methodBytecodes[regPC++] & 0xFF;

            final int[] dimensions = new int[dimensionsSize];

            while (--dimensionsSize >= 0) {
              dimensions[dimensionsSize] = toNumber(localMethodStack[regSP - 1]).intValue();
              localMethodStack[--regSP] = null;
            }

            localMethodStack[regSP++] = provider.newMultidimensional(caller, cpool.getItemAt(classRefIndex).asString(), dimensions);
          }
          break;
          case 198: // IFNULL
          case 199: // IFNONNULL 
          {
            final Object obj = localMethodStack[--regSP];
            final boolean result = instruction == 198 ? obj == null : obj != null;
            
            if (result) {
              regPC = lastPC + readShortValueFromArray(methodBytecodes, regPC);
            } else {
              regPC += 2;
            }
          }
          break;
          case 202: // BREAKPOINT
            throw new UnsupportedOperationException("Reserved opcode BREAKPOINT");
          case 254: // IMDEP1
            throw new UnsupportedOperationException("Reserved opcode IMDEP1");
          case 255: // IMDEP2
            throw new UnsupportedOperationException("Reserved opcode IMDEP2");
          default: // Unknown instruction
          {
            throw new UnsupportedOperationException("Unexpected instruction [" + instruction + ']');
          }
        }
      }
      catch (Throwable thr) {
        JJJVMTryCatchRecord record = null;

        for (final JJJVMTryCatchRecord r : method.getTryCatchRecords()) {
          if (r.isActiveForAddress(lastPC)) {
            final String exceptionClassName = r.getJvmFormattedClassName();
            
            if (exceptionClassName == null) {
              // it process any exception, may be it is finally
              record = r;
              break;
            }

            if (provider.checkCast(caller, exceptionClassName, thr)) {
              record = r;
              break;
            }

          }
        }

        if (record != null) {
          localMethodStack[regSP++] = thr;
          regPC = record.getCodeAddress();
        } else {
          int[][] lineNumberTable = method.getLineNumberTable();
          if (lineNumberTable != null) {
            int line=0;
            for (int i=0;i<lineNumberTable.length;i++) {
              line = lineNumberTable[i][1];
              if (lastPC<lineNumberTable[i][0]) {
                if (i>0) {
                  line = lineNumberTable[i-1][1];
                }
                break;
              }
            }
            StackTraceElement element = new StackTraceElement(caller.getClassName(),method.getName(),caller.getSourceFileName(), line);
            thr.setStackTrace(new StackTraceElement[]{element});
            throw thr;
          } else {
            throw thr;
          }
        }
      }
    }
  }

  private static int extractArgsNumber(final String methodSignature) {
    synchronized (CACHED_NUMBER_OF_ARGS) {
      if (CACHED_NUMBER_OF_ARGS.containsKey(methodSignature)) {
        return CACHED_NUMBER_OF_ARGS.get(methodSignature);
      } else {
        final int len = methodSignature.length();
        boolean objFlag = false;
        int counter = 0;
        boolean work = true;
        for (int li = 0; li < len && work; li++) {
          switch (methodSignature.charAt(li)) {
            case '(':
              continue;
            case ')':
              work = false;
              break;
            case '[':
              continue;
            case TYPE_CLASS:
              counter++;
              objFlag = true;
              break;
            case ';':
              objFlag = false;
              break;
            default: {
              if (!objFlag) {
                counter++;
              }
            }
            break;
          }
        }

        if (work) {
          throw new IllegalArgumentException("Wrong signature [" + methodSignature + ']');
        }
        CACHED_NUMBER_OF_ARGS.put(methodSignature, counter);
        return counter;
      }
    }
  }

  private static Number toNumber(final Object obj) {
    if (obj.getClass() == Character.class) return (int) (Character) obj;
    return (Number) obj;
  }

  private static int readIntFromArray(final byte[] array, int offset) {
    final int b0 = array[offset++] & 0xFF;
    final int b1 = array[offset++] & 0xFF;
    final int b2 = array[offset++] & 0xFF;
    final int b3 = array[offset] & 0xFF;

    return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
  }

  private static int readShortValueFromArray(final byte[] array, int offset) {
    final int b0 = array[offset++] & 0xFF;
    final int b1 = array[offset] & 0xFF;
    return (short) ((b0 << 8) | b1);
  }

  private static boolean isCategory2(final Object obj) {
    return obj instanceof Double || obj instanceof Long;
  }
}
