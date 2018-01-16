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

import static com.igormaznitsa.jjjvm.impl.JJJVMImplUtils.assertNotNull;

import com.igormaznitsa.jjjvm.model.*;
import com.igormaznitsa.jjjvm.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains class parser and JVM byte-code interpreter.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html}
 *
 * @see JJJVMProvider
 * @see JJJVMConstantPoolImpl
 * @see JJJVMClassMethodImpl
 * @see JJJVMClassFieldImpl
 * @see JJJVMObject
 */
public final class JJJVMClassImpl extends JJJVMInterpreter implements JJJVMClass {

    private final int classFileFormatVersion;
    private final int flags;
    private final int classNameIndex;
    private final int superClassNameIndex;
    private final String[] implementedInterfaces;
    private final Map<String, JJJVMField> declaredFields;
    private final Map<String, JJJVMMethod> declaredMethods;
    private final JJJVMProvider provider;
    private final JJJVMConstantPoolImpl constantPool;
    private final JJJVMInnerClassRecord[] innerClasses;
    private final String sourceFile;

    private static final Map<String, String> loadingClasses = new ConcurrentHashMap<String, String>();

    // constructor for test purposes
    public JJJVMClassImpl() {
        this.classFileFormatVersion = 0;
        this.flags = 0;
        this.classNameIndex = 0;
        this.superClassNameIndex = 0;
        this.implementedInterfaces = null;
        this.provider = null;
        this.constantPool = null;
        this.declaredMethods = null;
        this.declaredFields = null;
        this.innerClasses = JJJVMConstants.EMPTY_INNERCLASS_ARRAY;
        this.sourceFile = null;
    }

    /**
     * It parses and create instance of class which represented by input stream.
     *
     * @param in       stream contains array describing a compiled java class, must not
     *                 be null
     * @param provider a provider which implements misc service methods to process
     *                 byte code and resolve classes, must not be null
     * @throws Throwable it will be thrown for errors
     */
    public JJJVMClassImpl(final InputStream in, final JJJVMProvider provider) throws Throwable {
        assertNotNull("Provider is not defined", provider);
        assertNotNull("InputStream is null", in);

        this.provider = provider;

        final DataInputStream inStream = in instanceof DataInputStream ? (DataInputStream) in : new DataInputStream(in);

        if (inStream.readInt() != 0xCAFEBABE) {
            throw new IOException("Not Java class");
        }

        this.classFileFormatVersion = inStream.readInt();
        this.constantPool = new JJJVMConstantPoolImpl(this, inStream);
        this.flags = inStream.readUnsignedShort();
        this.classNameIndex = inStream.readUnsignedShort();

        loadingClasses.put(this.getClassName(), this.getClassName());

        try {
            this.superClassNameIndex = inStream.readUnsignedShort();
            final int numberOfInterfaces = inStream.readUnsignedShort();
            this.implementedInterfaces = numberOfInterfaces == 0 ? JJJVMConstants.EMPTY_STRING_ARRAY : new String[numberOfInterfaces];
            for (int i = 0; i < numberOfInterfaces; i++) {
                final String interfaceClassName = this.constantPool.getItemAt(inStream.readUnsignedShort()).asString();
                this.implementedInterfaces[i] = interfaceClassName;
                this.provider.resolveClass(interfaceClassName);
            }
            this.declaredFields = loadFields(inStream);
            this.declaredMethods = loadMethods(inStream);

            JJJVMInnerClassRecord[] detectedInnerClassess = null;
            String sourceFileName = null;
            int classAttributeNumber = inStream.readUnsignedShort();
            while (--classAttributeNumber >= 0) {
                final int nameIndex = inStream.readUnsignedShort();
                final int dataSize = inStream.readInt();
                final String attrName = this.constantPool.getItemAt(nameIndex).asString();
                if (JJJVMConstants.ATTRNAME_INNERCLASSES.equals(attrName)) {
                    detectedInnerClassess = readInnerClasses(inStream);
                } else if (JJJVMConstants.ATTRNAME_SOURCEFILE.equals(attrName)) {
                    sourceFileName = this.constantPool.getItemAt(inStream.readUnsignedShort()).asString();
                } else {
                    JJJVMImplUtils.skip(inStream, dataSize);
                }
            }
            this.sourceFile = sourceFileName;
            this.innerClasses = detectedInnerClassess == null ? JJJVMConstants.EMPTY_INNERCLASS_ARRAY : detectedInnerClassess;

            final JJJVMMethod clinitMethod = findMethod("<clinit>", "()V");
            if (clinitMethod != null && (clinitMethod.getFlags() & JJJVMConstants.ACC_NATIVE) == 0) {
                try {
                    clinitMethod.invoke(null, null);
                } catch (Throwable thr) {
                    throw new InvocationTargetException(thr, "Error during <clinit> [" + clinitMethod.getDeclaringClass().getName() + ']');
                }
            }

            this.provider.registerExternalClass(this.getClassName(), this);
        } finally {
            loadingClasses.remove(getClassName());
        }
    }

    public JJJVMProvider getProvider() {
        return this.provider;
    }

    /**
     * Get the source file name for the class.
     * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.10}
     *
     * @return the found source name or null if there was not any definition.
     */
    public String getSourceFileName() {
        return this.sourceFile;
    }

    private JJJVMInnerClassRecord[] readInnerClasses(final DataInputStream inStream) throws Throwable {
        final int numberOfClassess = inStream.readUnsignedShort();

        final JJJVMInnerClassRecord[] result = new JJJVMInnerClassRecord[numberOfClassess];
        for (int i = 0; i < numberOfClassess; i++) {
            final JJJVMInnerClassRecord record = new JJJVMInnerClassRecord(this, inStream);
            result[i] = record;
            if (isClassLoading(record.getInnerClassInfo().asString())) {
                continue;
            }
            this.provider.resolveInnerClass(this, record);
        }

        return result;
    }

    /**
     * Check, is the class still in loading mode.
     *
     * @param qualifiedClassName class name to check, must not be null
     * @return true if the class with the name is still in the loading list, false
     * otherwise
     */
    public static boolean isClassLoading(final String qualifiedClassName) {
        return loadingClasses.containsKey(qualifiedClassName);
    }

    /**
     * Get number of currently loading classes.
     *
     * @return number of currently loading classes.
     */
    public static int getNumberOfLoadingClasses() {
        return loadingClasses.size();
    }

    /**
     * Get records describing inner classes of the class.
     * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.6}
     *
     * @return array of the inner class records, must not be null
     */
    public JJJVMInnerClassRecord[] getInnerClassRecords() {
        return this.innerClasses;
    }

    /**
     * Resolve and return object representing superclass.
     *
     * @return object represents superclass, must not be null
     * @throws Throwable it will be thrown if impossible to resolve class or some
     *                   errors
     */
    public Object resolveSuperclass() throws Throwable {
        return this.provider.resolveClass(this.constantPool.getItemAt(this.superClassNameIndex).asString());
    }

    /**
     * Get array contains jvm formatted names of interfaces implemented by the
     * class.
     * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1}
     *
     * @return array contains names of interfaces, must not be null
     */
    public String[] getImplementedInterfaceNames() {
        return this.implementedInterfaces;
    }

    public int getFlags() {
        return this.flags;
    }

    /**
     * Get jvm formatted class name.
     *
     * @return class name in jvm format like "java/lang/Object$1"
     */
    public String getClassName() {
        return this.constantPool.getItemAt(this.classNameIndex).asString();
    }

    /**
     * Get name in normal format.
     *
     * @return class name in normal format like "java.lang.Object$1"
     */
    public String getName() {
        return normalizeClassName(getClassName());
    }

    /**
     * Get name in canonical format.
     *
     * @return class name in canonical format like "java.lang.Object.1"
     */
    public String getCanonicalName() {
        return getName().replace('$', '.');
    }

    /**
     * Get class format version.
     *
     * @return the class format version
     * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1}
     */
    public int getClassFormatVersion() {
        return this.classFileFormatVersion;
    }

    /**
     * Get the constant pool of the class
     * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4}
     *
     * @return the constant pool of the class, must not be null
     */
    public JJJVMConstantPoolImpl getConstantPool() {
        return this.constantPool;
    }

    private static String makeMethodUID(final String methodName, final String methodSignature) {
        return methodName + '.' + methodSignature;
    }

    private Map<String, JJJVMMethod> loadMethods(final DataInputStream inStream) throws IOException {
        final int numberOfMethods = inStream.readUnsignedShort();
        final Map<String, JJJVMMethod> result = new HashMap<>(numberOfMethods);
        for (int i = 0; i < numberOfMethods; i++) {
            final JJJVMMethod newMethod = new JJJVMClassMethodImpl(this, inStream);
            result.put(makeMethodUID(newMethod.getName(), newMethod.getSignature()), newMethod);
        }
        return result;
    }

    private Map<String, JJJVMField> loadFields(final DataInputStream inStream) throws Throwable {
        final int numberOfFields = inStream.readUnsignedShort();
        final Map<String, JJJVMField> result = new HashMap<>(numberOfFields);
        for (int i = 0; i < numberOfFields; i++) {
            final JJJVMField newField = new JJJVMClassFieldImpl(this, inStream);
            result.put(newField.getName(), newField);
        }
        return result;
    }

    static void skipAllAttributesInStream(final DataInputStream inStream) throws IOException {
        int numberOfAttributes = inStream.readUnsignedShort();
        while (--numberOfAttributes >= 0) {
            // skip name
            JJJVMImplUtils.skip(inStream, 2);
            //skip data
            JJJVMImplUtils.skip(inStream, inStream.readInt());
        }
    }

    /**
     * Normalize a class name from jvm formatted form to normal form.
     *
     * @param jvmFormattedClassName jvm formatted name to be normalized, must not
     *                              be null
     * @return normalized name, for instance "java/lang/Object" becomes
     * "java.lang.Object"
     */
    public static String normalizeClassName(final String jvmFormattedClassName) {
        return jvmFormattedClassName.replace('/', '.');
    }

    /**
     * Find for field in the class and its ancestors.
     *
     * @param fieldName the fied name, must not be null
     * @return found field object or null if the field has not been found
     * @throws Throwable it will be thrown for errors
     */
    public final JJJVMField findField(final String fieldName) throws Throwable {
        JJJVMField result = findDeclaredField(fieldName);
        if (result == null) {
            for (final String inter : this.implementedInterfaces) {
                final Object resolvedClass = this.provider.resolveClass(inter);
                if (resolvedClass != null && resolvedClass instanceof JJJVMClass) {
                    result = ((JJJVMClass) resolvedClass).findField(fieldName);
                    if (result != null) {
                        break;
                    }
                }
            }

            if (result == null) {
                final Object resolvedClass = this.resolveSuperclass();
                if (resolvedClass != null && resolvedClass instanceof JJJVMClass) {
                    result = ((JJJVMClass) resolvedClass).findField(fieldName);
                }
            }
        }
        return result;
    }

    /**
     * Find for field defined only in the class.
     *
     * @param fieldName the fied name, must not be null
     * @return found field object or null if the field has not been found
     */
    public final JJJVMField findDeclaredField(final String fieldName) {
        return this.declaredFields.get(fieldName);
    }

    /**
     * Find for method defined by the class or in its ancestors.
     *
     * @param methodName      the method name, must not be null
     * @param methodSignature the method signature, must not be null
     * @return found method object or null if not found
     * @throws Throwable it will be thrown for errors
     */
    public final JJJVMMethod findMethod(final String methodName, final String methodSignature) throws Throwable {
        JJJVMMethod result = findDeclaredMethod(methodName, methodSignature);
        if (result == null) {
            final Object resolvedClass = this.resolveSuperclass();
            if (resolvedClass != null && resolvedClass instanceof JJJVMClass) {
                result = ((JJJVMClass) resolvedClass).findMethod(methodName, methodSignature);
            }
        }
        return result;
    }

    /**
     * Find for method declared only in the class.
     *
     * @param methodName      the method name, must not be null
     * @param methodSignature the method signature, must not be null
     * @return found method object or null if not found
     */
    public final JJJVMMethod findDeclaredMethod(final String methodName, final String methodSignature) {
        return this.declaredMethods.get(makeMethodUID(methodName, methodSignature));
    }

    private void assertCanBeInstantiated() {
        if ((this.flags & (JJJVMConstants.ACC_ABSTRACT | JJJVMConstants.ACC_INTERFACE)) != 0) {
            throw new IllegalStateException("Class '" + this.getName() + "' abstract one or interface");
        }
    }

    /**
     * Create new instance of the class.
     *
     * @param invokeDefaultConstructor true if to call the default constructor for
     *                                 the new instance, false if just allocated object
     * @return new object of the class, must not be null
     * @throws Throwable it will be thrown for errors
     */
    public JJJVMObject newInstance(final boolean invokeDefaultConstructor) throws Throwable {
        final JJJVMObject result = new JJJVMObject(this, null);
        initInstanceFields(result);
        if (invokeDefaultConstructor && !this.getClassName().equals("java/lang/Object")) {
            assertCanBeInstantiated();

            final JJJVMMethod constructor = this.findDeclaredMethod("<init>", "()V");
            if (constructor == null) {
                throw new IllegalAccessException("Can't find the default constructor");
            }
            invoke(this, result, constructor, null, null, null);
        }
        return result;
    }

    /**
     * Create new class instance and call defined constructor.
     *
     * @param constructorSignature the signature of the constructor to be called
     *                             after memory allocation, must not be null
     * @param args                 array of arguments for the constructor, can be null
     * @param stack                predefined stack for the call, can be null
     * @param vars                 predefined local variable array, can be null
     * @return new object instance of the class, must not be null
     * @throws Throwable it will be thrown for errors
     */
    public JJJVMObject newInstance(final String constructorSignature, final Object[] args, final Object[] stack, final Object[] vars) throws Throwable {
        assertCanBeInstantiated();
        final JJJVMMethod constructor = this.findMethod("<init>", constructorSignature);
        if (constructor == null) {
            throw new IllegalAccessException("Can't find the constructor [" + getClassName() + ' ' + constructorSignature + ']');
        }
        final JJJVMObject result = new JJJVMObject(this, null);
        initInstanceFields(result);
        invoke(this, result, constructor, args, stack, vars);
        return result;
    }

    public JJJVMObject initInstanceFields(final JJJVMObject obj) throws Throwable {
        final Object parent = this.resolveSuperclass();
        if (parent != null && parent instanceof JJJVMClass) {
            ((JJJVMClass) parent).initInstanceFields(obj);
        }

        for (final Entry<String, JJJVMField> current : this.declaredFields.entrySet()) {
            final JJJVMField theField = current.getValue();

            final String fieldSignature = theField.getSignature();
            final Object fieldValue;

            if (fieldSignature.length() > 1) {
                // it is an object type, should be inited by null
                fieldValue = null;
            } else {
                // it is a primitive type
                switch (fieldSignature.charAt(0)) {
                    case JJJVMConstants.TYPE_LONG:
                        fieldValue = (long) 0;
                        break;
                    case JJJVMConstants.TYPE_INT:
                    case JJJVMConstants.TYPE_SHORT:
                    case JJJVMConstants.TYPE_CHAR:
                        fieldValue = 0;
                        break;
                    case JJJVMConstants.TYPE_DOUBLE:
                        fieldValue = 0.0d;
                        break;
                    case JJJVMConstants.TYPE_FLOAT:
                        fieldValue = 0.0f;
                        break;
                    case JJJVMConstants.TYPE_BOOLEAN:
                        fieldValue = false;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported field type [" + fieldSignature + ']');
                }
            }

            if ((theField.getFlags() & JJJVMConstants.ACC_STATIC) == 0) {
                obj.setFieldValue(theField.getName(), fieldValue, false);
            }
        }

        return obj;
    }

    /**
     * Read value of a class static field.
     *
     * @param fieldName the field name, must not be null
     * @return the value from the static field
     * @throws NoSuchFieldException if the field is not found
     * @throws Throwable            it will be thrown for inside errors
     */
    public Object readStaticField(final String fieldName) throws Throwable {
        final JJJVMField field = this.findField(fieldName);

        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }

        return field.getStaticValue();
    }

    /**
     * Write value into a class static field.
     *
     * @param fieldName the field name, must not be null
     * @param value     value to be written into the field
     * @throws NoSuchFieldException  if the field is not found
     * @throws IllegalStateException if the field is final
     * @throws Throwable             it will be thrown for inside errors
     */
    public void writeStaticField(final String fieldName, final Object value) throws Throwable {
        final JJJVMField field = this.findField(fieldName);

        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }

        field.setStaticValue(value);
    }

    public Map<String, JJJVMField> getAllDeclaredFields() {
        return this.declaredFields;
    }

    public Map<String, JJJVMMethod> getAllDeclaredMethods() {
        return this.declaredMethods;
    }
}
