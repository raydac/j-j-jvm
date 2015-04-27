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

import com.igormaznitsa.jjjvm.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class JSEProviderImpl implements JJJVMProvider {

  private static final sun.misc.Unsafe UNSAFE = getUnsafe();

  private static sun.misc.Unsafe getUnsafe() {
    try {
      final Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      return (sun.misc.Unsafe) field.get(null);
    }
    catch (Exception ex) {
      throw new Error("Can't get access to sun.misc.Unsafe");
    }
  }

  private final Map<String, Object> classCache = new HashMap<String, Object>();
  private final Map<String, Class[]> parsedArgsCache = new HashMap<String, Class[]>();

  public interface ClassLoader {

    byte[] loadClass(String className) throws IOException, ClassNotFoundException;
  }

  private final ClassLoader classBodyProvider;

  public JSEProviderImpl(final ClassLoader classLoader) {
    this.classBodyProvider = classLoader;
  }

  public Object resolveClass(final String jvmFormattedClassName) throws Throwable {
    Object clazz = null;
    synchronized (this.classCache) {
      clazz = this.classCache.get(jvmFormattedClassName);
      if (clazz == null) {
        try {
          clazz = Class.forName(jvmFormattedClassName.replace('/', '.'));
        }
        catch (ClassNotFoundException ex) {
          clazz = new JJJVMClass(new ByteArrayInputStream(this.classBodyProvider.loadClass(jvmFormattedClassName)), this);
        }
        this.classCache.put(jvmFormattedClassName, clazz);
      }
    }
    return clazz;
  }

  public Object allocate(final JJJVMClass caller, final String jvmFormattedClassName) throws Throwable {
    final Object klazz = resolveClass(jvmFormattedClassName);
    if (klazz == Class.class) {
      return UNSAFE.allocateInstance((Class) klazz);
    }
    else if (klazz == JJJVMClass.class) {
      return ((JJJVMClass) klazz).newInstance(false);
    }
    else {
      throw new Error("Illegal object [" + klazz + ']');
    }
  }

  public Object invoke(final JJJVMClass caller, final Object instance, final String jvmFormattedClassName, final String methodName, final String methodSignature, final Object[] arguments) throws Throwable {
    if (instance instanceof JJJVMObject) {
      final JJJVMClass klazz = ((JJJVMObject) instance).getKlazz();
      return klazz.invoke((JJJVMObject) instance, klazz.findMethod(methodName, methodSignature), arguments, null, null);
    }
    else {
      final Object resolvedClass = resolveClass(jvmFormattedClassName);
      Class[] paramClasses;
      synchronized (this.parsedArgsCache) {
        paramClasses = this.parsedArgsCache.get(methodSignature);
        if (paramClasses == null) {
          paramClasses = parseArgsFromMethodSignature(methodSignature);
          this.parsedArgsCache.put(methodSignature, paramClasses);
        }
      }

      final Class klazz = (Class) resolvedClass;
      final Method method = klazz.getMethod(methodName, paramClasses);
      return method.invoke(instance, arguments);
    }
  }

  public Object[] newObjectArray(final JJJVMClass caller, final String jvmFormattedClassName, final int arrayLength) throws Throwable {
    final Object resolvedClass = resolveClass(jvmFormattedClassName);
    if (resolvedClass instanceof JJJVMClass) {
      return new JJJVMClass[arrayLength];
    }
    else {
      return (Object[]) Array.newInstance((Class) resolvedClass, arrayLength);
    }
  }

  public Object newMultidimensional(final JJJVMClass caller, final String jvmFormattedClassName, final int[] arrayDimensions) throws Throwable {
    final Object resolvedClass = resolveClass(jvmFormattedClassName);

    if (resolvedClass instanceof JJJVMClass) {
      return Array.newInstance(JJJVMClass.class, arrayDimensions);
    }
    else {
      final Class klazz = (Class) resolvedClass;
      return Array.newInstance(klazz, arrayDimensions);
    }
  }

  public Object get(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      return jjjobj.getKlazz().findDeclaredField(fieldName).get(jjjobj);
    }
    else {
      return obj.getClass().getField(fieldName).get(obj);
    }
  }

  public void set(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature, final Object fieldValue) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      jjjobj.getKlazz().findDeclaredField(fieldName).set(jjjobj, fieldValue);
    }
    else {
      obj.getClass().getField(fieldName).set(obj, fieldValue);
    }
  }

  public Object getStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      return ((JJJVMClass) resolved).findDeclaredField(fieldName).getStaticValue();
    }
    else {
      return ((Class) resolved).getField(fieldName).get(null);
    }
  }

  public void setStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature, final Object value) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      ((JJJVMClass) resolved).findDeclaredField(fieldName).setStaticValue(value);
    }
    else {
      ((Class) resolved).getField(fieldName).set(null, value);
    }
  }

  public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) throws Throwable {
    boolean result = true;
    if (!"java/lang/Object".equals(jvmFormattedClassName)) {
      if (value instanceof JJJVMObject) {
        result = JJJVMClass.findClassForNameInHierarchy(((JJJVMObject) value).getKlazz(), jvmFormattedClassName) != null;
      }
      else {
        final Object theclazz = this.resolveClass(jvmFormattedClassName);
        if (theclazz instanceof JJJVMClass) {
          return false;
        }
        try {
          ((Class) theclazz).cast(value);
        }
        catch (ClassCastException ex) {
          result = false;
        }
      }
    }
    return result;
  }

  public void doThrow(final JJJVMClass caller, final Object objectProvidedAsThrowable) throws Throwable {
    if (objectProvidedAsThrowable instanceof JJJVMThrowable) {
      throw (Throwable) objectProvidedAsThrowable;
    }
    else {
      if (objectProvidedAsThrowable instanceof RuntimeException) {
        throw new JJJVMRuntimeException((Throwable) objectProvidedAsThrowable);
      }
      else {
        throw new JJJVMRuntimeException((Throwable) objectProvidedAsThrowable);
      }
    }
  }

  public boolean isExceptionCompatible(final JJJVMClass caller, final Object exception, final String rawExceptionClassName) throws Throwable {
    return false;
  }

  public void doMonitor(final JJJVMClass caller, final Object object, boolean lock) throws Throwable {
    if (object instanceof JJJVMObject) {
      final JJJVMObject jjjvmobj = (JJJVMObject) object;
      if (lock) {
        jjjvmobj.lock();
      }
      else {
        jjjvmobj.unlock();
      }
    }
    else {
      if (lock) {
        UNSAFE.monitorEnter(object);
      }
      else {
        UNSAFE.monitorExit(object);
      }
    }
  }

  private Class[] parseArgsFromMethodSignature(final String signature) throws Throwable {
    // TODO implement array support

    final List<Class> resultList = new ArrayList<Class>();

    final StringBuilder klazzNameBuffer = new StringBuilder(16);

    boolean started = false;
    boolean clazzName = false;

    int dimensions = 0;

    for (int i = 0; i < signature.length(); i++) {

      final char chr = signature.charAt(i);

      if (clazzName) {
        if (chr == ';') {
          final String className = klazzNameBuffer.toString();
          klazzNameBuffer.setLength(0);

          final Object resolvedClass = resolveClass(className);
          if (resolvedClass instanceof Class) {
            resultList.add((Class) resolvedClass);
          }
          else {
            throw new IllegalArgumentException("Unsupported object type as argument [" + resolvedClass + ']');
          }

          dimensions = 0;
        }
        else {
          klazzNameBuffer.append(chr);
        }
      }

      switch (chr) {
        case '(':
          started = true;
          dimensions = 0;
          break;
        case ')':
          started = false;
          dimensions = 0;
          i = signature.length();
          break;
        case 'L':
          clazzName = true;
          break;
        case 'I':
          resultList.add(int.class);
          dimensions = 0;
          break;
        case 'B':
          resultList.add(byte.class);
          dimensions = 0;
          break;
        case 'C':
          resultList.add(char.class);
          dimensions = 0;
          break;
        case 'D':
          resultList.add(double.class);
          dimensions = 0;
          break;
        case 'F':
          resultList.add(float.class);
          dimensions = 0;
          break;
        case 'J':
          resultList.add(long.class);
          dimensions = 0;
          break;
        case 'S':
          resultList.add(short.class);
          dimensions = 0;
          break;
        case 'Z':
          resultList.add(boolean.class);
          dimensions = 0;
          break;
        case '[':
          dimensions++;
          break;
        default:
          throw new Error("unexpected char [" + chr + ']');
      }
    }

    return resultList.toArray(new Class[resultList.size()]);
  }

}
