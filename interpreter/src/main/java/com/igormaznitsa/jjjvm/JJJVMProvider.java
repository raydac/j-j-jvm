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

public interface JJJVMProvider {

  public Object invoke(JJJVMClass caller, Object instance, String jvmFormattedClassName, String methodName, String methodSignature, Object[] _arguments) throws Throwable;
  public Object allocate(JJJVMClass caller, String jvmFormattedClassName) throws Throwable;
  public Object[] newObjectArray(JJJVMClass caller, String jvmFormattedClassName, int arrayLength) throws Throwable;
  public Object newMultidimensional(JJJVMClass caller, String jvmFormattedClassName, int[] arrayDimensions) throws Throwable;
  public Object get(JJJVMClass caller, Object obj, String fieldName, String fieldSignature) throws Throwable;
  public void set(JJJVMClass caller, Object obj, String fieldName, String fieldSignature, Object fieldValue) throws Throwable;
  public Object getStatic(JJJVMClass caller, String jvmFormattedClassName, String fieldName, String fieldSignature) throws Throwable;
  public void setStatic(JJJVMClass caller, String jvmFormattedClassName, String fieldName, String fieldSignature, Object value) throws Throwable;
  public boolean checkCast(JJJVMClass caller, String jvmFormattedClassName, Object objectToCheck) throws Throwable;
  public void doThrow(JJJVMClass caller, Object objectProvidedAsThrowable) throws Throwable;
  public Object resolveClass(String jvmFormattedClassName) throws Throwable;
  public void registerExternalClass(String jvmFormattedClassName, Object clazz);
  public void doMonitor(JJJVMClass caller, Object object, boolean lock) throws Throwable;
  public JJJVMClass resolveInnerClass(JJJVMClass caller, JJJVMInnerClassRecord innerClassRecord) throws Throwable;
}
