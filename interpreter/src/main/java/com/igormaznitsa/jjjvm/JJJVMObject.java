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

import com.igormaznitsa.JJJVMField;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Object container describing some instances of JJJVMClass.
 *
 * @see JJJVMClass#newInstance(boolean)
 * @see JJJVMClass#newInstance(java.lang.String, java.lang.Object[],
 * java.lang.Object[], java.lang.Object[])
 */
public final class JJJVMObject {

  /**
   * The Class of the object.
   */
  private final JJJVMKlazz klazz;
  /**
   * Flag shows that the object is finalized.
   */
  private final AtomicBoolean objectFinalized = new AtomicBoolean(false);
  /**
   * Map contains values of object fields.
   */
  private final Map<String, Object> fieldValues = new HashMap<String, Object>();
  /**
   * Monitor for the object.
   */
  private final ReentrantLock monitor = new ReentrantLock();

  public Object get(final String fieldName, final boolean checkKey) {
    if (checkKey && !this.fieldValues.containsKey(fieldName)) {
      throw new IllegalArgumentException("Unknown field name '" + fieldName + '\'');
    }
    return this.fieldValues.get(fieldName);
  }

  public void set(final String fieldName, final Object value, final boolean checkThatFieldPresented) {
    if (checkThatFieldPresented && !this.fieldValues.containsKey(fieldName)) {
      throw new IllegalArgumentException("Unknown field name '" + fieldName + '\'');
    }
    this.fieldValues.put(fieldName, value);
  }

  public JJJVMObject(final JJJVMKlazz klazz) throws Throwable {
    this.klazz = klazz;
    if (klazz != null) {
      final Map<String, JJJVMField> map = klazz.getDeclaredFields();
      // init constants
      for (final Entry<String, JJJVMField> e : map.entrySet()) {
        final JJJVMField field = e.getValue();
        if ((field.getFlags() & JJJVMField.ACC_STATIC) == 0) {
          this.fieldValues.put(field.getName(), field.getConstantValue());
        }
      }
    }
  }

  public JJJVMKlazz getKlazz() {
    return this.klazz;
  }

  public boolean isFinalized() {
    return this.objectFinalized.get();
  }

  public void doFinalize() throws Throwable {
    if (this.objectFinalized.compareAndSet(false, true)) {
      try {
        final JJJVMMethod finalizeMethod = this.klazz.findDeclaredMethod("finalize", "()V");
        if (finalizeMethod != null) {
          JJJVMInterpreter.invoke(this.klazz, this, finalizeMethod, null, null, null);
        }
      }
      finally {
        this.fieldValues.clear();
      }
    }
  }

  public void lock() throws InterruptedException {
    this.monitor.lockInterruptibly();
  }

  public void unlock() {
    this.monitor.unlock();
  }

  public boolean isLocked() {
    return this.monitor.isLocked();
  }

}
