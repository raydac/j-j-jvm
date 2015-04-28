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
import java.util.Map.Entry;

/**
 * Implementation of provider for Java SE.
 *
 * @see sun.misc.Unsafe
 */
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
  private final Map<String, Map<String, Boolean>> cachedCast = new HashMap<String, Map<String, Boolean>>();

  /**
   * Class loader which loads and provides class byte-code
   */
  public interface ClassLoader {

    /**
     * Load class byte-code.
     *
     * @param jvmFormattedClassName the JVM formatted class name, must not be
     * null.
     * @return byte-code of the class, must not be null
     * @throws IOException it must be throws for transport error
     * @throws ClassNotFoundException it must be thrown if class not found
     */
    byte[] loadClass(String jvmFormattedClassName) throws IOException, ClassNotFoundException;
  }

  private final ClassLoader classBodyProvider;

  public JSEProviderImpl(final ClassLoader classLoader) {
    this.classBodyProvider = classLoader;
  }

  public JJJVMClass resolveInnerClass(final JJJVMClass caller, final JJJVMInnerClassRecord innerClassRecord) throws Throwable {
    final String outerClassName = innerClassRecord.getOuterClassInfo() == null ? null : innerClassRecord.getOuterClassInfo().getClassName();
    final String innerClassName = innerClassRecord.getInnerClassInfo().getClassName();

    final JJJVMClass result;

    synchronized (this.classCache) {
      if (outerClassName != null && !this.classCache.containsKey(outerClassName) && !JJJVMClass.isClassLoading(outerClassName)) {
        final JJJVMClass outerClass = new JJJVMClass(new ByteArrayInputStream(this.classBodyProvider.loadClass(outerClassName)), this);
        this.classCache.put(outerClassName, outerClass);
        result = (JJJVMClass) this.classCache.get(innerClassName);
        if (result == null) {
          throw new Error("Unexpectedstate, inner class [" + innerClassName + "] is not loaded");
        }
      }
      else {
        result = new JJJVMClass(new ByteArrayInputStream(this.classBodyProvider.loadClass(innerClassName)), this);
        this.classCache.put(innerClassName, result);
      }
    }
    return result;
  }

  public void registerExternalClass(final String jvmFormattedClassName, final Object clazz) {
    if (clazz == null) {
      throw new NullPointerException("Class is null");
    }
    if (jvmFormattedClassName == null) {
      throw new NullPointerException("Class name is null");
    }
    if (!(clazz instanceof Class || clazz instanceof JJJVMClass)) {
      throw new IllegalArgumentException("unexpected class object");
    }
    synchronized (this.classCache) {
      this.classCache.put(jvmFormattedClassName, clazz);
    }
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
    if (klazz instanceof JJJVMClass) {
      return ((JJJVMClass) klazz).newInstance(false);
    }
    else {
      return UNSAFE.allocateInstance((Class) klazz);
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
      if ("<init>".equals(methodName)) {
        // constructor
        final Constructor constructor = klazz.getConstructor(paramClasses);
        constructor.setAccessible(true);
        return constructor.newInstance(arguments);
      }
      else {
        final Method method = findMethod(klazz, methodName, paramClasses);
        method.setAccessible(true);
        return method.invoke(instance, arguments);
      }
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

  private static String extractTypeFromFieldSignature(final String fieldSignature) {
    final StringBuilder buffer = new StringBuilder(fieldSignature.length());
    boolean className = false;
    for (int i = 0; i < fieldSignature.length(); i++) {
      final char chr = fieldSignature.charAt(i);

      if (className) {
        if (chr == ';') {
          break;
        }
        else {
          buffer.append(chr);
        }
      }
      else {
        if (chr == '[') {
          continue;
        }
        if (chr == 'L') {
          className = true;
        }
        buffer.append(chr);
      }
    }
    return buffer.toString();
  }

  public Object newMultidimensional(final JJJVMClass caller, final String jvmFormattedClassName, final int[] arrayDimensions) throws Throwable {
    final String extractedType = extractTypeFromFieldSignature(jvmFormattedClassName);

    final Object resolvedClass;
    if (extractedType.length() == 1) {
      switch (extractedType.charAt(0)) {
        case 'B':
          resolvedClass = byte.class;
          break;
        case 'C':
          resolvedClass = char.class;
          break;
        case 'F':
          resolvedClass = float.class;
          break;
        case 'J':
          resolvedClass = long.class;
          break;
        case 'S':
          resolvedClass = short.class;
          break;
        case 'Z':
          resolvedClass = boolean.class;
          break;
        case 'I':
          resolvedClass = int.class;
          break;
        case 'D':
          resolvedClass = double.class;
          break;
        default:
          throw new Error("Unexpected type [" + extractedType + ']');
      }
    }
    else {
      resolvedClass = resolveClass(extractedType);
    }

    if (resolvedClass instanceof JJJVMClass) {
      return Array.newInstance(JJJVMClass.class, arrayDimensions);
    }
    else {
      final Class klazz = (Class) resolvedClass;
      return Array.newInstance(klazz, arrayDimensions);
    }
  }

  private static Field findField(final Class klazz, final String fieldName) throws Throwable {
    Field found = klazz.getDeclaredField(fieldName);
    if (found == null) {
      final Class superKlazz = klazz.getSuperclass();
      if (superKlazz != null) {
        found = findField(superKlazz, fieldName);
      }
    }
    if (found == null) {
      throw new Error("Can't find field '" + fieldName + '\'');
    }
    found.setAccessible(true);
    return found;
  }

  private static Method findMethod(final Class klazz, final String methodName, final Class[] types) throws Throwable {
    Method found = klazz.getDeclaredMethod(methodName, types);
    if (found == null) {
      final Class superKlazz = klazz.getSuperclass();
      if (superKlazz != null) {
        found = findMethod(superKlazz, methodName, types);
      }
    }
    if (found == null) {
      throw new Error("Can't find method '" + methodName + '\'');
    }
    found.setAccessible(true);
    return found;
  }

  public Object get(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      return jjjobj.getKlazz().findDeclaredField(fieldName).get(jjjobj);
    }
    else {
      return findField(obj.getClass(), fieldName).get(obj);
    }
  }

  public void set(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature, final Object fieldValue) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      jjjobj.getKlazz().findDeclaredField(fieldName).set(jjjobj, fieldValue);
    }
    else {
      findField(obj.getClass(), fieldName).set(obj, fieldValue);
    }
  }

  public Object getStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      return ((JJJVMClass) resolved).findDeclaredField(fieldName).getStaticValue();
    }
    else {
      return findField((Class) resolved, fieldName).get(null);
    }
  }

  public void setStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature, final Object value) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      ((JJJVMClass) resolved).findDeclaredField(fieldName).setStaticValue(value);
    }
    else {
      findField((Class) resolved, fieldName).set(null, value);
    }
  }

  public boolean checkCast(final JJJVMClass caller, final String jvmFormattedClassName, final Object value) throws Throwable {
    if ("java/lang/Object".equals(jvmFormattedClassName)) {
      return true;
    }
    synchronized (this.cachedCast) {
      Map<String, Boolean> record = this.cachedCast.get(jvmFormattedClassName);
      if (record == null) {
        record = new HashMap<String, Boolean>();
        this.cachedCast.put(jvmFormattedClassName, record);
      }

      boolean result = true;
      if (value instanceof JJJVMObject) {
        final JJJVMObject jjjvmObj = (JJJVMObject) value;
        final String objClassName = jjjvmObj.getKlazz().getClassName();

        Boolean flag = record.get(objClassName);
        if (flag == null) {
          result = JJJVMClass.findClassForNameInHierarchy(jjjvmObj.getKlazz(), jvmFormattedClassName) != null;
          record.put(objClassName, result);
        }
        else {
          result = flag;
        }
      }
      else {
        final String klazzName = value.getClass().getName();
        Boolean flag = record.get(klazzName);
        if (flag == null) {
          final Object theclazz = this.resolveClass(jvmFormattedClassName);
          try {
            ((Class) theclazz).cast(value);
          }
          catch (ClassCastException ex) {
            result = false;
          }
          record.put(klazzName, result);
        }
        else {
          result = flag;
        }
      }
      return result;
    }
  }

  public void doThrow(final JJJVMClass caller, final Object objectProvidedAsThrowable) throws Throwable {
    if (objectProvidedAsThrowable instanceof Throwable){
      throw (Throwable)objectProvidedAsThrowable;
    }else{
      throw new Throwable(objectProvidedAsThrowable.toString());
    }
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

  private static Class makeMultidimensionArrayClass(final Class klazz, final int dimensionNumber) {
    return Array.newInstance(klazz, new int[dimensionNumber]).getClass();
  }

  private Class[] parseArgsFromMethodSignature(final String signature) throws Throwable {
    final List<Class> resultList = new ArrayList<Class>();

    final StringBuilder klazzNameBuffer = new StringBuilder(16);

    boolean started = false;
    boolean clazzName = false;

    int dimensions = 0;

    for (int i = 0; i < signature.length(); i++) {

      final char chr = signature.charAt(i);

      if (clazzName) {
        if (chr == ';') {
          clazzName = false;
          final String className = klazzNameBuffer.toString();
          klazzNameBuffer.setLength(0);

          final Object resolvedClass = resolveClass(className);
          if (resolvedClass instanceof Class) {
            resultList.add(dimensions == 0 ? (Class) resolvedClass : makeMultidimensionArrayClass((Class) resolvedClass, dimensions));
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
      else {
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
            resultList.add(dimensions == 0 ? int.class : makeMultidimensionArrayClass(int.class, dimensions));
            dimensions = 0;
            break;
          case 'B':
            resultList.add(dimensions == 0 ? byte.class : makeMultidimensionArrayClass(byte.class, dimensions));
            dimensions = 0;
            break;
          case 'C':
            resultList.add(dimensions == 0 ? char.class : makeMultidimensionArrayClass(char.class, dimensions));
            dimensions = 0;
            break;
          case 'D':
            resultList.add(dimensions == 0 ? double.class : makeMultidimensionArrayClass(double.class, dimensions));
            dimensions = 0;
            break;
          case 'F':
            resultList.add(dimensions == 0 ? float.class : makeMultidimensionArrayClass(float.class, dimensions));
            dimensions = 0;
            break;
          case 'J':
            resultList.add(dimensions == 0 ? long.class : makeMultidimensionArrayClass(long.class, dimensions));
            dimensions = 0;
            break;
          case 'S':
            resultList.add(dimensions == 0 ? short.class : makeMultidimensionArrayClass(short.class, dimensions));
            dimensions = 0;
            break;
          case 'Z':
            resultList.add(dimensions == 0 ? boolean.class : makeMultidimensionArrayClass(boolean.class, dimensions));
            dimensions = 0;
            break;
          case '[':
            dimensions++;
            break;
          default:
            throw new Error("Unexpected signature [" + signature + ']');
        }
      }
    }

    return resultList.toArray(new Class[resultList.size()]);
  }

}
