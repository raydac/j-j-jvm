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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

public class Storage implements TreeModel {

  protected String name;
  protected List<TreeModelListener> treeModelListeners;
  protected List<StorageItem> items;
  public static final Comparator<StorageItem> ALPHA_COMPARATOR = new Comparator<StorageItem>() {

    @Override
    public int compare(final StorageItem o1, final StorageItem o2) {
      // packages on top
      if (o1 instanceof PackageItem && o2 instanceof ClassItem) {
        return -1;
      }
      if (o2 instanceof PackageItem && o1 instanceof ClassItem) {
        return 1;
      }

      return o1.getName().compareTo(o2.getName());
    }
  };

  public Storage(final String name) {
    this.name = name;
    this.treeModelListeners = new ArrayList<TreeModelListener>();
    this.items = new ArrayList<StorageItem>();
  }

  public void addArchive(final File archiveFile) throws IOException {
    FileInputStream inStream = null;
    try {
      inStream = new FileInputStream(archiveFile);
      final ZipInputStream zipInputStream = new ZipInputStream(inStream);
      final byte[] ab_bufferArray = new byte[1024];

      while (true) {
        final ZipEntry zipEntry = zipInputStream.getNextEntry();
        if (zipEntry == null) {
          break;
        }

        if (zipEntry.isDirectory()) {
          continue;
        }

        if (!zipEntry.getName().endsWith(".class")) {
          continue;
        }

        final ByteArrayOutputStream bufferForEntry = new ByteArrayOutputStream(16384);
        byte[] classDataArray = null;
        if (zipEntry.getSize() < 0) {
          while (true) {
            int i_readLen = zipInputStream.read(ab_bufferArray, 0, ab_bufferArray.length);
            if (i_readLen < 0) {
              break;
            }
            bufferForEntry.write(ab_bufferArray, 0, i_readLen);
          }
          zipInputStream.closeEntry();
          bufferForEntry.close();
          classDataArray = bufferForEntry.toByteArray();
        }
        else {
          while (true) {
            int i_readLen = zipInputStream.read(ab_bufferArray, 0, ab_bufferArray.length);
            if (i_readLen < 0) {
              break;
            }
            bufferForEntry.write(ab_bufferArray, 0, i_readLen);
          }
          zipInputStream.closeEntry();
          bufferForEntry.close();
          classDataArray = bufferForEntry.toByteArray();
        }

        addClass(new ByteArrayInputStream(classDataArray), false);
      }
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (IOException _thr) {
        }
      }

      for (final TreeModelListener l : treeModelListeners) {
        l.treeStructureChanged(new TreeModelEvent(this, new TreePath(this)));
      }
    }
  }

  public ClassItem[] getAllClassItems() {
    final List<ClassItem> resultList = new ArrayList<ClassItem>();

    for (final StorageItem item : this.items) {
      if (item instanceof PackageItem) {
        for (final ClassItem c : ((PackageItem) item).getClassItems()) {
          resultList.add(c);
        }
      }
      else {
        if (item instanceof ClassItem) {
          resultList.add((ClassItem) item);
        }
      }
    }
    return resultList.toArray(new ClassItem[resultList.size()]);
  }

  public PackageItem getPackageForName(final String packageName) {
    for (final StorageItem item : this.items) {
      if (item instanceof PackageItem) {
        if (item.getName().equals(packageName)) {
          return (PackageItem) item;
        }
      }
    }
    return null;
  }

  public void removeAll() {
    this.items.clear();
    for (TreeModelListener p_listener : this.treeModelListeners) {
      p_listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(this)));
    }
  }

  protected PackageItem addPackage(final String packageName) {
    final PackageItem newPackage = new PackageItem(packageName);
    this.items.add(newPackage);
    return newPackage;
  }

  public void addClass(final InputStream classStream, final boolean doNotify) throws IOException {
    final JavaClass jclazz = new ClassParser(classStream, null).parse();

    String packageName = jclazz.getPackageName();
    if (packageName == null || packageName.length() == 0) {
      packageName = "<default>";
    }

    PackageItem packageItem = getPackageForName(packageName);
    if (packageItem == null) {
      packageItem = addPackage(packageName);
    }

    packageItem.addClass(new ClassItem(packageItem, jclazz));

    if (doNotify) {
      for (final TreeModelListener l : this.treeModelListeners) {
        l.treeStructureChanged(new TreeModelEvent(this, new TreePath(this)));
      }
    }
  }

  public void removeItem(final StorageItem item) {
    if (item instanceof ClassItem) {
      final PackageItem packageItem = ((ClassItem) item).getPackage();
      if (packageItem != null) {
        packageItem.removeClass((ClassItem) item);
        if (packageItem.getChildCount(packageItem) == 0) {
          this.items.remove(packageItem);
        }
      }
    }
    else {
      if (item instanceof PackageItem) {
        this.items.remove(item);
      }
    }

    for (final TreeModelListener l : this.treeModelListeners) {
      l.treeStructureChanged(new TreeModelEvent(this, new TreePath(this)));
    }
  }

  public void addClassFromFile(final File classFile) throws IOException {
    FileInputStream fileStream = null;
    try {
      fileStream = new FileInputStream(classFile);
      addClass(fileStream, true);
    }
    finally {
      if (fileStream != null) {
        try {
          fileStream.close();
        }
        catch (Throwable _thr) {
        }
      }
    }
  }

  @Override
  public Object getRoot() {
    return this;
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (this == parent) {
      return this.items.get(index);
    }
    else {
      if (parent instanceof PackageItem) {
        return ((PackageItem) parent).getChild(parent, index);
      }
      else {
        return null;
      }
    }
  }

  @Override
  public int getChildCount(final Object parent) {
    if (this == parent) {
      return this.items.size();
    }
    else {
      if (parent instanceof PackageItem) {
        return ((PackageItem) parent).getChildCount(parent);
      }
      else {
        return 0;
      }
    }
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
    if (parent == this) {
      return this.items.indexOf(child);
    }
    else {
      if (parent instanceof PackageItem) {
        return ((PackageItem) parent).getIndexOfChild(parent, child);
      }
      else {
        return -1;
      }
    }
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    if (!this.treeModelListeners.contains(l)) {
      this.treeModelListeners.add(l);
    }
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    this.treeModelListeners.remove(l);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
