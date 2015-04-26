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

import java.io.IOException;
import java.util.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class PackageItem implements StorageItem, TreeModel {

  protected String packageName;
  protected List<StorageItem> listOfItems;
  protected boolean enabled;

  public PackageItem(final String packageName) {
    this.packageName = packageName;
    this.enabled = true;
    this.listOfItems = new ArrayList<StorageItem>();
  }

  @Override
  public Object getRoot() {
    return this;
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent == this) {
      return listOfItems.get(index);
    }
    else {
      return null;
    }
  }

  @Override
  public int getChildCount(final Object parent) {
    if (this == parent) {
      return listOfItems.size();
    }
    return 0;
  }

  @Override
  public boolean isLeaf(final Object node) {
    return node instanceof ClassItem;
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {

  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    if (this == parent) {
      return listOfItems.indexOf(child);
    }
    else {
      return -1;
    }
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {

  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {

  }

  @Override
  public String toString() {
    return this.packageName;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public void setEnabled(final boolean value) {
    this.enabled = value;
  }

  @Override
  public String getName() {
    return this.packageName;
  }

  public ClassItem[] getClassItems() {
    final List<ClassItem> resultList = new ArrayList<ClassItem>();
    for (final StorageItem itm : this.listOfItems) {
      if (itm instanceof ClassItem) {
        resultList.add((ClassItem) itm);
      }
    }
    return resultList.toArray(new ClassItem[resultList.size()]);
  }

  protected void addClass(final ClassItem item) throws IOException {
    if (this.listOfItems.contains(item)) {
      throw new IOException("Class already presented in the package [" + item.getJavaClass().getClassName() + ']');
    }
    this.listOfItems.add(item);
    Collections.sort(this.listOfItems, Storage.ALPHA_COMPARATOR);
  }

  protected void removeClass(final ClassItem classItem) {
    this.listOfItems.remove(classItem);
  }

}
