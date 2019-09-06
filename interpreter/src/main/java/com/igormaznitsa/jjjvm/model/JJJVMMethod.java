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

/**
 * Describing a class method.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6}
 */
public interface JJJVMMethod extends JJJVMConstants {

  /**
   * Class containing the method.
   *
   * @return the class object which contains the method, must not be null
   */
  JJJVMClass getDeclaringClass();

  /**
   * Method flags.
   *
   * @return method flags
   */
  int getFlags();

  /**
   * Method name.
   *
   * @return the method name as String, must not be null
   */
  String getName();

  /**
   * Method signature contains info about arguments and result type.
   *
   * @return the method signature as String, must not be null
   */
  String getSignature();

  /**
   * Size of local variable area needed for work of the method code.
   *
   * @return size of local variable area for the method code.
   */
  int getMaxLocals();

  /**
   * Max stack depth needed for method work.
   *
   * @return max stack depth for the method
   */
  int getMaxStackDepth();

  /**
   * Byte-code of the method.
   *
   * @return the byte-code of the method, must not be null
   */
  byte[] getBytecode();

  /**
   * List of jvm formatted names of declared exceptions for the method.
   *
   * @return list of names as String array, must not be null
   */
  String[] getDeclaredExceptions();

  /**
   * get try..catch records for the method byte code.
   *
   * @return array of try..catch methods for the method byte-code, must not be
   * null
   */
  JJJVMTryCatchRecord[] getTryCatchRecords();

  /**
   * Invoke the method code.
   *
   * @param object instance of object (this), it can be null for static method
   * @param arguments arguments for the method
   * @return result of invocation or null for void methods
   * @throws Throwable it will be thrown for errors
   */
  Object invoke(JJJVMObject object, Object[] arguments) throws Throwable;

  /**
   * @return line numbers table (if exists) to translate byte-code position to source-code line
   */
  public int[][] getLineNumberTable();

  /**
   * Flag shows that it is clinit() method.
   * @return true if it is static void <linit(), false otherwise.
   */
  public boolean isClinit();
}
