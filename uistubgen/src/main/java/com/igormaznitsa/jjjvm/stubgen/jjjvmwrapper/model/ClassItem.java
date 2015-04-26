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
package com.igormaznitsa.jjjvm.stubgen.jjjvmwrapper.model;

import org.apache.bcel.classfile.JavaClass;

public class ClassItem implements StorageItem {

  protected boolean enabled;
  protected String className;

  protected JavaClass jclazz;
  protected String internalClassID;

  protected PackageItem parent;

  public ClassItem(final PackageItem packageItem, final JavaClass jclazz) {
    this.className = jclazz.getClassName();
    this.jclazz = jclazz;
    this.parent = packageItem;

    final int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex >= 0) {
      this.className = className.substring(lastDotIndex + 1);
    }

    this.enabled = true;

    this.internalClassID = jclazz.getClassName();
  }

  public PackageItem getPackage() {
    return parent;
  }

  public JavaClass getJavaClass() {
    return jclazz;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(final boolean value) {
    this.enabled = value;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof ClassItem)) {
      return false;
    }
    return this.internalClassID.equals(((ClassItem) obj).internalClassID);
  }

  @Override
  public int hashCode() {
    return this.internalClassID.hashCode();
  }

  @Override
  public String toString() {
    return this.className;
  }

  @Override
  public String getName() {
    return this.className;
  }
}
