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

package com.igormaznitsa.jjjvm.impl.jse;

import com.igormaznitsa.jjjvm.model.JJJVMClass;
import com.igormaznitsa.jjjvm.model.JJJVMObject;
import com.igormaznitsa.jjjvm.model.JJJVMProvider;
import com.igormaznitsa.jjjvm.model.JJJVMInnerClassRecord;
import com.igormaznitsa.jjjvm.impl.JJJVMClassImpl;
import com.igormaznitsa.jjjvm.impl.JJJVMImplUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/**
 * Implementation of provider optimized for Java SE 1.5+.
 *
 * @see sun.misc.Unsafe
 */
public class JSEProviderImpl implements JJJVMProvider {

  /**
   * Class loader which loads and provides class byte-code
   */
  public interface ClassDataLoader {

    /**
     * Load class byte-code.
     *
     * @param jvmFormattedClassName the JVM formatted class name, must not be
     *                              null.
     * @return byte-code of the class, or null if class not found
     * @throws IOException it must be throws for transport error
     */
    byte[] loadClassBody(String jvmFormattedClassName) throws IOException;
  }

  protected static final sun.misc.Unsafe UNSAFE = getUnsafe();

  private static sun.misc.Unsafe getUnsafe() {
    try {
      final Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      return (sun.misc.Unsafe) field.get(null);
    } catch (Exception ex) {
      throw new Error("Can't get access to sun.misc.Unsafe", ex);
    }
  }

  protected final Map<String, Object> classCache = new HashMap<String, Object>();
  protected final Map<String, Class[]> parsedArgsCache = new HashMap<String, Class[]>();
  protected final Map<String, Map<String, Boolean>> cachedCast = new HashMap<String, Map<String, Boolean>>();

  protected final ClassDataLoader classDataLoader;

  public JSEProviderImpl() {
    this.classDataLoader = new ClassDataLoader() {
      public byte[] loadClassBody(String jvmFormattedClassName) throws IOException {
        return null;
      }
    };
  }

  public JSEProviderImpl(final ClassDataLoader classLoader) {
    if (classLoader == null) {
      throw new NullPointerException("Loader is null");
    }
    this.classDataLoader = classLoader;
  }

  protected JJJVMClass loadClassFromLoader(final String jvmFormattedClassName) throws Throwable {
    if (this.classDataLoader == null) {
      throw new NullPointerException("Class loader is not defined");
    }
    final byte[] classBody = this.classDataLoader.loadClassBody(jvmFormattedClassName);
    if (classBody == null) {
      throw new ClassNotFoundException("Can't find body for class '" + jvmFormattedClassName + '\'');
    }
    return new JJJVMClassImpl(new ByteArrayInputStream(classBody), this);
  }

  public JJJVMClass resolveInnerClass(final JJJVMClass caller, final JJJVMInnerClassRecord innerClassRecord) throws Throwable {
    final String outerClassName = innerClassRecord.getOuterClassInfo() == null ? null : innerClassRecord.getOuterClassInfo().getClassName();
    final String innerClassName = innerClassRecord.getInnerClassInfo().getClassName();

    JJJVMClass result;

    synchronized (this.classCache) {
      result = (JJJVMClass) this.classCache.get(innerClassName);
      if (result == null) {
        if (outerClassName != null && !this.classCache.containsKey(outerClassName) && !JJJVMClassImpl.isClassLoading(outerClassName)) {
          final JJJVMClass outerClass = loadClassFromLoader(outerClassName);
          this.classCache.put(outerClassName, outerClass);
          result = (JJJVMClass) this.classCache.get(innerClassName);
          if (result == null) {
            throw new Error("Unexpected state, inner class [" + innerClassName + "] is not loaded");
          }
        } else {
          result = loadClassFromLoader(innerClassName);
          this.classCache.put(innerClassName, result);
        }
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
      throw new IllegalArgumentException("Unexpected class object [" + clazz + ']');
    }

    synchronized (this.classCache) {
      this.classCache.put(jvmFormattedClassName, clazz);
    }
  }

  public Object resolveClass(final String jvmFormattedClassName) throws Throwable {
    Object result;
    synchronized (this.classCache) {
      result = this.classCache.get(jvmFormattedClassName);
      if (result == null) {
        try {
          result = loadClassFromLoader(jvmFormattedClassName);
        } catch (ClassNotFoundException ex) {
          result = Class.forName(jvmFormattedClassName.replace('/', '.'));
        }
        this.classCache.put(jvmFormattedClassName, result);
      }
    }
    return result;
  }

  public Object allocate(final JJJVMClass caller, final String jvmFormattedClassName) throws Throwable {
    final Object klazz = resolveClass(jvmFormattedClassName);
    if (klazz instanceof JJJVMClass) {
      return ((JJJVMClass) klazz).newInstance(false);
    } else {
      return UNSAFE.allocateInstance((Class) klazz);
    }
  }

  public Object invoke(final JJJVMClass caller, final Object instance, final String jvmFormattedClassName, final String methodName, final String methodSignature, final Object[] arguments) throws Throwable {
    if ("java/lang/Object".equals(jvmFormattedClassName) && methodName.startsWith("<")) {
      return null;
    }

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
      if (Modifier.isAbstract(klazz.getModifiers())) {
        throw new Error("Attempt directly instantiate abstract class " + klazz.getName());
      }

      Constructor constructor;
      try {
        constructor = klazz.getConstructor(paramClasses);
      } catch (NoSuchMethodException ex) {
        constructor = klazz.getDeclaredConstructor(paramClasses);
      }
      constructor.setAccessible(true);
      return constructor.newInstance(arguments);
    } else {
      final Method method = findMethod(klazz, methodName, paramClasses);
      JJJVMImplUtils.makeAccessible(method);
      return method.invoke(instance, castArgs(method.getParameterTypes(), arguments));
    }
  }

  private static Object[] castArgs(final Class[] types, final Object[] args) {
    if (types.length>0){
      for(int i=0;i<types.length;i++){
        final Class type = types[i];
        final Object arg = args[i];
        if (type == char.class && arg instanceof Number) {
          args[i] = (char)((Number)arg).intValue();
        }
      }
    }
    return args;
  }

  public Object[] newObjectArray(final JJJVMClass caller, final String jvmFormattedClassName, final int arrayLength) throws Throwable {
    final Object resolvedClass = resolveClass(jvmFormattedClassName);
    if (resolvedClass instanceof JJJVMClass) {
      return new JJJVMClass[arrayLength];
    } else {
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
        } else {
          buffer.append(chr);
        }
      } else {
        if (chr == '[') {
          continue;
        }
        if (chr == TYPE_CLASS) {
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
        case TYPE_BYTE:
          resolvedClass = byte.class;
          break;
        case TYPE_CHAR:
          resolvedClass = char.class;
          break;
        case TYPE_FLOAT:
          resolvedClass = float.class;
          break;
        case TYPE_LONG:
          resolvedClass = long.class;
          break;
        case TYPE_SHORT:
          resolvedClass = short.class;
          break;
        case TYPE_BOOLEAN:
          resolvedClass = boolean.class;
          break;
        case TYPE_INT:
          resolvedClass = int.class;
          break;
        case TYPE_DOUBLE:
          resolvedClass = double.class;
          break;
        default:
          throw new Error("Unexpected type [" + extractedType + ']');
      }
    } else {
      resolvedClass = resolveClass(extractedType);
    }

    if (resolvedClass instanceof JJJVMClass) {
      return Array.newInstance(JJJVMClass.class, arrayDimensions);
    } else {
      final Class klazz = (Class) resolvedClass;
      return Array.newInstance(klazz, arrayDimensions);
    }
  }

  protected static Field findField(final Class klazz, final String fieldName) throws Throwable {
    Field found = null;
    try {
      found = klazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException ex) {
      final Class superKlazz = klazz.getSuperclass();
      if (superKlazz != null) {
        found = findField(superKlazz, fieldName);
      }
      if (found == null) {
        throw new Error("Can't find field '" + fieldName + '\'');
      }
      JJJVMImplUtils.makeAccessible(found);
    }
    return found;
  }

  protected static Method findMethod(final Class klazz, final String methodName, final Class[] types) throws Throwable {
    Method found = null;
    try {
      found = klazz.getDeclaredMethod(methodName, types);
    } catch (NoSuchMethodException ex) {
      final Class superKlazz = klazz.getSuperclass();
      if (superKlazz != null) {
        found = findMethod(superKlazz, methodName, types);
      }
      if (found == null) {
        throw new Error("Can't find method '" + methodName + '\'');
      }
      JJJVMImplUtils.makeAccessible(found);
    }
    return found;
  }

  public Object get(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      return jjjobj.getDeclaringClass().findDeclaredField(fieldName).get(jjjobj);
    } else {
      return findField(obj.getClass(), fieldName).get(obj);
    }
  }

  public void set(final JJJVMClass caller, final Object obj, final String fieldName, final String fieldSignature, final Object fieldValue) throws Throwable {
    if (obj instanceof JJJVMObject) {
      final JJJVMObject jjjobj = (JJJVMObject) obj;
      jjjobj.getDeclaringClass().findDeclaredField(fieldName).set(jjjobj, fieldValue);
    } else {
      findField(obj.getClass(), fieldName).set(obj, fieldValue);
    }
  }

  public Object getStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      return ((JJJVMClass) resolved).findDeclaredField(fieldName).getStaticValue();
    } else {
      return findField((Class) resolved, fieldName).get(null);
    }
  }

  public void setStatic(final JJJVMClass caller, final String jvmFormattedClassName, final String fieldName, final String fieldSignature, final Object value) throws Throwable {
    final Object resolved = resolveClass(jvmFormattedClassName);
    if (resolved instanceof JJJVMClass) {
      ((JJJVMClass) resolved).findDeclaredField(fieldName).setStaticValue(value);
    } else {
      findField((Class) resolved, fieldName).set(null, value);
    }
  }

  /**
   * Check is it possible or to to cast the class to class defined by its name.
   *
   * @param jvmFormattedClassName name of the target class, must not be null
   * @return true if the class can be casted to the target class, false
   * otherwise
   * @throws Throwable it will be thrown for errors
   */
  public static boolean tryCastTo(final JJJVMClass klazz, final String jvmFormattedClassName) throws Throwable {
    final boolean result;
    if ("java/lang/Object".equals(jvmFormattedClassName)) {
      result = true;
    } else {
      result = findClassForNameInHierarchy(klazz, jvmFormattedClassName) != null;
    }
    return result;
  }

  /**
   * Make search among ancestors and interfaces of a class for a class defined
   * by its normalized name
   *
   * @param klazz           a class which ancestors will be used for search, it can be
   *                        null
   * @param normalClassName normalized class name of target class, must not be
   *                        null
   * @return object of found class or null if not found or the root class is
   * null
   * @throws Throwable it will be thrown for error
   */
  public static Class findClassForNameInHierarchy(final Class klazz, final String normalClassName) throws Throwable {
    if (klazz == null) {
      return null;
    }
    if ("java.lang.Object".equals(normalClassName)) {
      return java.lang.Object.class;
    }

    if (klazz.getName().equals(normalClassName)) {
      return klazz;
    }
    for (final Class interfaceClass : klazz.getInterfaces()) {
      final Class obj = findClassForNameInHierarchy(interfaceClass, normalClassName);
      if (obj != null) {
        return obj;
      }
    }
    return findClassForNameInHierarchy(klazz.getSuperclass(), normalClassName);
  }

  /**
   * Make search among ancestors and interfaces of a JJJVMClass for a class
   * defined by its jvm formatted name.
   *
   * @param klazz                 a class which ancestors will be used for search, it can be
   *                              null
   * @param jvmFormattedClassName jvm formatted class name name of target class,
   *                              must not be null
   * @return object of found class or null if not found or the root class is
   * null
   * @throws Throwable it will be thrown for error
   */
  public static Object findClassForNameInHierarchy(final JJJVMClass klazz, final String jvmFormattedClassName) throws Throwable {
    if (klazz == null) {
      return false;
    }
    if ("java/lang/Object".equals(jvmFormattedClassName)) {
      return java.lang.Object.class;
    }
    if (jvmFormattedClassName.equals(klazz.getClassName())) {
      return klazz;
    }

    final String normalizedName = JJJVMClassImpl.normalizeClassName(jvmFormattedClassName);

    for (final String inter : klazz.getImplementedInterfaceNames()) {
      if (inter.equals(jvmFormattedClassName)) {
        return true;
      }
      final Object resolvedClass = klazz.getProvider().resolveClass(jvmFormattedClassName);
      final Object detected;
      if (resolvedClass instanceof JJJVMClass) {
        detected = findClassForNameInHierarchy((JJJVMClass) resolvedClass, jvmFormattedClassName);
      } else {
        detected = findClassForNameInHierarchy((Class) resolvedClass, jvmFormattedClassName);
      }
      if (detected != null) {
        return detected;
      }
    }

    final Object superKlazz = klazz.resolveSuperclass();
    if (superKlazz instanceof JJJVMClass) {
      return findClassForNameInHierarchy((JJJVMClass) superKlazz, jvmFormattedClassName);
    } else {
      return findClassForNameInHierarchy((Class) superKlazz, normalizedName);
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
        final String objClassName = jjjvmObj.getDeclaringClass().getClassName();

        Boolean flag = record.get(objClassName);
        if (flag == null) {
          result = findClassForNameInHierarchy(jjjvmObj.getDeclaringClass(), jvmFormattedClassName) != null;
          record.put(objClassName, result);
        } else {
          result = flag;
        }
      } else {
        final String klazzName = value.getClass().getName();
        Boolean flag = record.get(klazzName);
        if (flag == null) {
          final Object theclazz = this.resolveClass(jvmFormattedClassName);
          try {
            ((Class) theclazz).cast(value);
          } catch (ClassCastException ex) {
            result = false;
          }
          record.put(klazzName, result);
        } else {
          result = flag;
        }
      }
      return result;
    }
  }

  public void doThrow(final JJJVMClass caller, final Object objectProvidedAsThrowable) throws Throwable {
    if (objectProvidedAsThrowable instanceof Throwable) {
      throw (Throwable) objectProvidedAsThrowable;
    } else {
      throw new Throwable(objectProvidedAsThrowable.toString());
    }
  }

  public void doMonitor(final JJJVMClass caller, final Object object, boolean lock) throws Throwable {
    if (object instanceof JJJVMObject) {
      final JJJVMObject jjjvmobj = (JJJVMObject) object;
      if (lock) {
        jjjvmobj.lock();
      } else {
        jjjvmobj.unlock();
      }
    } else {
      if (lock) {
        UNSAFE.monitorEnter(object);
      } else {
        UNSAFE.monitorExit(object);
      }
    }
  }

  protected static Class makeMultidimensionArrayClass(final Class klazz, final int dimensionNumber) {
    return Array.newInstance(klazz, new int[dimensionNumber]).getClass();
  }

  protected Class[] parseArgsFromMethodSignature(final String signature) throws Throwable {
    final List<Class> resultList = new ArrayList<Class>();
    final StringBuilder klazzNameBuffer = new StringBuilder(16);
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
          } else {
            throw new IllegalArgumentException("Must be java.lang.Class [" + resolvedClass + ']');
          }

          dimensions = 0;
        } else {
          klazzNameBuffer.append(chr);
        }
      } else {
        final Class klazz;
        switch (chr) {
          case '(':
            dimensions = 0;
            klazz = null;
            break;
          case ')':
            dimensions = 0;
            klazz = null;
            i = signature.length();
            break;
          case TYPE_CLASS:
            klazz = null;
            clazzName = true;
            break;
          case TYPE_INT:
            klazz = int.class;
            break;
          case TYPE_BYTE:
            klazz = byte.class;
            break;
          case TYPE_CHAR:
            klazz = char.class;
            break;
          case TYPE_DOUBLE:
            klazz = double.class;
            break;
          case TYPE_FLOAT:
            klazz = float.class;
            break;
          case TYPE_LONG:
            klazz = long.class;
            break;
          case TYPE_SHORT:
            klazz = short.class;
            break;
          case TYPE_BOOLEAN:
            klazz = boolean.class;
            break;
          case '[':
            klazz = null;
            dimensions++;
            break;
          default:
            throw new Error("Unexpected signature [" + signature + ']');
        }
        if (klazz != null) {
          resultList.add(dimensions == 0 ? klazz : makeMultidimensionArrayClass(klazz, dimensions));
          dimensions = 0;
        }
      }
    }

    return resultList.toArray(new Class[resultList.size()]);
  }

}
