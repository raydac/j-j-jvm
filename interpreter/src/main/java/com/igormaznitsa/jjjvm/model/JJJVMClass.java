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

import java.util.Map;

public interface JJJVMClass extends JJJVMConstants {

  JJJVMProvider getProvider();

  JJJVMConstantPool getConstantPool();

  JJJVMMethod findMethod(String methodName, String methodSignature) throws Throwable;

  JJJVMField findField(String fieldName) throws Throwable;

  JJJVMMethod findDeclaredMethod(String methodName, String methodSignature);

  JJJVMField findDeclaredField(String fieldName);

  Map<String, JJJVMField> getAllDeclaredFields();

  Map<String, JJJVMMethod> getAllDeclaredMethods();

  Object resolveSuperclass() throws Throwable;

  String[] getImplementedInterfaceNames();

  int getClassFormatVersion();

  int getFlags();

  String getName();

  String getClassName();

  String getCanonicalName();

  String getSourceFileName();

  Object readStaticField(String fieldName) throws Throwable;

  void writeStaticField(String fieldName, Object value) throws Throwable;

  JJJVMObject newInstance(boolean callDefaultConstructor) throws Throwable;

  JJJVMObject newInstance(String constructorSignature, Object[] args, Object[] stack, Object[] localVariables) throws Throwable;

  JJJVMObject initInstanceFields(JJJVMObject obj) throws Throwable;

}
