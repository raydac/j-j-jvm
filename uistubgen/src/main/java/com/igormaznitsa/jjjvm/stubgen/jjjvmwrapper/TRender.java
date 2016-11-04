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
package com.igormaznitsa.jjjvm.stubgen.jjjvmwrapper;

import com.igormaznitsa.jjjvm.stubgen.jjjvmwrapper.model.ClassItem;
import com.igormaznitsa.jjjvm.stubgen.jjjvmwrapper.model.PackageItem;
import com.igormaznitsa.jjjvm.stubgen.jjjvmwrapper.model.Storage;
import com.igormaznitsa.jjjvm.stubgen.utils.Utils;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

public class TRender extends JLabel implements TreeCellRenderer {

  private static final long serialVersionUID = 7727878199201586657L;
  protected ImageIcon iconStorage;
  protected ImageIcon iconPackage;
  protected ImageIcon iconClass;

  protected Color colorSelectedBackground;
  protected Color colorSelectedForeground;
  protected Color colorForeground;
  protected Color colorBackground;

  public TRender() {
    super();
    iconStorage = new ImageIcon(Utils.loadImage("images/icon_storage.gif"));
    iconPackage = new ImageIcon(Utils.loadImage("images/icon_packageitem.gif"));
    iconClass = new ImageIcon(Utils.loadImage("images/icon_classitem.gif"));

    colorSelectedForeground = UIManager.getColor("Tree.selectionForeground");
    colorSelectedBackground = UIManager.getColor("Tree.selectionBackground");
    colorForeground = UIManager.getColor("Tree.textForeground");
    colorBackground = UIManager.getColor("Tree.textBackground");
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    String str = value.toString() + "  ";

    if (selected) {
      setOpaque(true);
      setForeground(colorSelectedForeground);
      setBackground(colorSelectedBackground);
    } else {
      setOpaque(false);
      setForeground(colorForeground);
      setBackground(colorBackground);
    }

    if (value instanceof Storage) {
      setIcon(iconStorage);
      setText(str);
    } else if (value instanceof PackageItem) {
      setIcon(iconPackage);
      setText(str);
    } else if (value instanceof ClassItem) {
      setIcon(iconClass);
      setText(str);
    } else {
      setIcon(null);
      setText(str);
    }
    return this;
  }
}
