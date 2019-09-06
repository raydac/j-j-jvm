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
 * Interface describes service which allows JJJVMClass to get needed
 * information and do some business.
 *
 * @see com.igormaznitsa.jjjvm.impl.jse.JSEProviderImpl
 */
public interface JJJVMProvider extends JJJVMConstants {

  /**
   * Make invocation of a method
   *
   * @param caller the class calling the method, must not be null
   * @param instance the object instance, if the method is static then it will
   * be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of the class which is owner of the method, must not be null
   * @param methodName the method name, must not be null
   * @param methodSignature the method signature in standard JVM format
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4}
   * @param _arguments arguments for the cass
   * @return the result of invocation, if the method void then null must be
   * returned
   * @throws Throwable it will be thrown for problems or inside exceptions
   */
  Object invoke(JJJVMClass caller, Object instance, String jvmFormattedClassName, String methodName, String methodSignature, Object[] _arguments) throws Throwable;

  /**
   * Allocate memory for new instance of a class.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of the class which instance must be allocated
   * @return an object describing memory area reserved for new instance, must
   * not be null
   * @throws Throwable it will be thrown for errors
   */
  Object allocate(JJJVMClass caller, String jvmFormattedClassName) throws Throwable;

  /**
   * Create array of objects of defined class.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of class of elements of the generated array
   * @param arrayLength number of elements in result array, must be zero or
   * greater
   * @return single dimensional array for defined class with defined number of
   * cells, must not be null
   * @throws Throwable it will be thrown for errors
   */
  Object[] newObjectArray(JJJVMClass caller, String jvmFormattedClassName, int arrayLength) throws Throwable;

  /**
   * Create multi-dimensional array of elements of defined class.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of class of elements of the generated array
   * @param arrayDimensions number of elements in every dimension of generated
   * array, must not be null
   * @return generated multidimensional array as object, must not be null
   * @throws Throwable it will be thrown for errors
   */
  Object newMultidimensional(JJJVMClass caller, String jvmFormattedClassName, int[] arrayDimensions) throws Throwable;

  /**
   * Get value from a field of an object.
   *
   * @param caller the class calling the method, must not be null
   * @param obj the object instance which field should be read, it must not be
   * null
   * @param fieldName the field name, must not be null
   * @param fieldSignature the field signature, must not be null
   * @return the read object from the field
   * @throws Throwable it will be thrown for errors
   */
  Object get(JJJVMClass caller, Object obj, String fieldName, String fieldSignature) throws Throwable;

  /**
   * Write value into field of Object.
   *
   * @param caller the class calling the method, must not be null
   * @param obj object which field should be written, it must not be null
   * @param fieldName the field name, must not be null
   * @param fieldSignature the field signature, must not be null
   * @param fieldValue the value to be written into the field
   * @throws Throwable it will be thrown for errors
   */
  void set(JJJVMClass caller, Object obj, String fieldName, String fieldSignature, Object fieldValue) throws Throwable;

  /**
   * Read value from a static field.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * which static field should be read, must not be null
   * @param fieldName the field name, must not be null
   * @param fieldSignature the field signature, must not be null
   * @return the read value from the static field
   * @throws Throwable it will be thrown for errors
   */
  Object getStatic(JJJVMClass caller, String jvmFormattedClassName, String fieldName, String fieldSignature) throws Throwable;

  /**
   * Write value into a static field.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * which static field should be changed, must not be null
   * @param fieldName the field name, must not be null
   * @param fieldSignature the field signature, must not be null
   * @param value the value to be written into the static field
   * @param force force set value to final field
   * @throws Throwable it will be thrown for errors
   */
  void setStatic(JJJVMClass caller, String jvmFormattedClassName, String fieldName, String fieldSignature, Object value, boolean force) throws Throwable;

  /**
   * Check that object can be casted to class.
   *
   * @param caller the class calling the method, must not be null
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of class for which we check the object, must not be null
   * @param objectToCheck the object to be checked, must not be null
   * @return true if the object can be casted to the class, false otherwise
   * @throws Throwable it will be thrown for errors
   */
  boolean checkCast(JJJVMClass caller, String jvmFormattedClassName, Object objectToCheck) throws Throwable;

  /**
   * Throw a Throwable based on provided object.
   *
   * @param caller the class calling the method, must not be null
   * @param objectProvidedAsThrowable base object to be used for throwable
   * object constructing
   * @throws Throwable the exception will be thrown, type of error will be based
   * on the provided object
   */
  void doThrow(JJJVMClass caller, Object objectProvidedAsThrowable) throws Throwable;

  /**
   * Resolve class by its jvm formatted name.
   *
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2},
   * must not be null
   * @return object representing class defined by the name, must not be null
   * @throws Throwable it will be thrown for errors
   * @see #resolveInnerClass(com.igormaznitsa.jjjvm.JJJVMClass,
   * com.igormaznitsa.jjjvm.JJJVMInnerClassRecord)
   */
  Object resolveClass(String jvmFormattedClassName) throws Throwable;

  /**
   * Register object describing a class under some jvm formatted class name.
   *
   * @param jvmFormattedClassName the jvm formatted name
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2}
   * of the registering class, must not be null
   * @param clazz the object describing class, must not be null
   */
  void registerExternalClass(String jvmFormattedClassName, Object clazz);

  /**
   * Lock or unlock monitor for an object.
   *
   * @param caller the class calling the method, must not be null
   * @param object object to lock or unlock, must not be null
   * @param lock true if the object must be locked, false otherwise
   * @throws Throwable it will be thrown for errors
   */
  void doMonitor(JJJVMClass caller, Object object, boolean lock) throws Throwable;

  /**
   * Resolve some inner class.
   *
   * @param caller the class calling the method, must not be null
   * @param innerClassRecord recod describing inner class info structure, must
   * not be null
   * @return object describing the inner class, must not be null
   * @throws Throwable it will be thrown for errors
   * @see #resolveClass(java.lang.String)
   */
  JJJVMClass resolveInnerClass(JJJVMClass caller, JJJVMInnerClassRecord innerClassRecord) throws Throwable;
}
