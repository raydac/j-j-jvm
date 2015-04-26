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
package com.igormaznitsa.jjjvm.stubgen.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

public class Utils {

  public static final byte[] loadResource(final String resourcePath) throws IOException {
    InputStream inStream = null;
    try {
      inStream = Utils.class.getClassLoader().getResourceAsStream(resourcePath);
      if (inStream == null) {
        throw new IOException("Can't find resource '" + resourcePath + '\'');
      }
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(16384);

      final int BUFFERSIZE = 1024;
      final byte[] readBuffer = new byte[BUFFERSIZE];

      while (true) {
        int i_read = inStream.read(readBuffer);
        if (i_read < 0) {
          break;
        }
        buffer.write(readBuffer, 0, i_read);
      }

      return buffer.toByteArray();
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (Throwable _thr) {
        }
      }
    }
  }

  public static byte[] loadFromURL(final URL url) throws IOException {
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(false);
    connection.setDoInput(true);

    connection.connect();

    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      InputStream inStream = null;
      ByteArrayOutputStream buffer = null;
      try {
        inStream = connection.getInputStream();
        buffer = new ByteArrayOutputStream(16384);

        final byte[] readBuffer = new byte[16384];
        while (true) {
          int i_read = inStream.read(readBuffer, 0, readBuffer.length);
          if (i_read <= 0) {
            break;
          }
          buffer.write(readBuffer, 0, i_read);
        }
        buffer.flush();

        return buffer.toByteArray();
      }
      catch (Throwable thr) {
        throw (IOException) (new IOException(thr.getClass().getCanonicalName()).initCause(thr));
      }
      finally {
        try {
          connection.disconnect();
        }
        catch (Throwable thr) {
        }

        if (buffer != null) {
          try {
            buffer.close();
          }
          catch (Throwable _thr) {
          }
        }
      }
    }
    else {
      return null;
    }
  }

  public static final void saveByteArrayToFile(final File file, final byte[] array) throws IOException {
    FileOutputStream outStream = null;
    try {
      outStream = new FileOutputStream(file);
      outStream.write(array);
      outStream.flush();
    }
    finally {
      if (outStream != null) {
        try {
          outStream.close();
        }
        catch (Throwable thr) {
        }
      }
    }
  }

  public static final byte[] loadByteArrayFromFile(final File file) throws IOException {
    FileInputStream inStream = null;
    try {
      inStream = new FileInputStream(file);
      int len = (int) file.length();
      byte[] result = new byte[len];
      int pos = 0;
      while (len > 0) {
        int readLen = inStream.read(result, pos, len);
        if (readLen < 0) {
          break;
        }
        pos += readLen;
        len -= readLen;
      }
      if (len > 0) {
        throw new IOException("Can't read full file");
      }
      return result;
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (Throwable _thr) {
        }
      }
    }
  }

  public static final void changeMenuItemsState(final JMenu menu, final boolean enableState) {
    final int size = menu.getItemCount();
    for (int i = 0; i < size; i++) {
      final JMenuItem item = menu.getItem(i);
      item.setEnabled(enableState);
    }
  }

  public static final void toScreenCenter(final Window component) {
    final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    component.setLocation((dim.width - component.getWidth()) / 2, (dim.height - component.getHeight()) / 2);
  }

  public static final Image loadImage(final String _path) {
    try {
      return ImageIO.read(Utils.class.getClassLoader().getResourceAsStream(_path));
    }
    catch (Throwable thr) {
      thr.printStackTrace();
      return null;
    }
  }

  public static File selectFileForOpen(final Component parent, final FileFilter[] fileFilters, final String title, final FileFilter[] selectedFilters, final File initFile) {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setDragEnabled(false);
    chooser.setControlButtonsAreShown(true);
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    for (final FileFilter fileFilter : fileFilters) {
      chooser.addChoosableFileFilter(fileFilter);
    }

    chooser.setDialogTitle(title);
    chooser.setAcceptAllFileFilterUsed(false);

    if (initFile != null) {
      chooser.setCurrentDirectory(initFile);
      chooser.setName(initFile.getName());
    }

    int returnVal = chooser.showDialog(parent, "Open");
    selectedFilters[0] = chooser.getFileFilter();

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File p_file = chooser.getSelectedFile();
      return p_file;
    }
    else {
      return null;
    }

  }

  public static File selectFileForSave(final Component parent, final FileFilter fileFiltere, final String title, final File initFile) {
    final JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setDragEnabled(false);
    chooser.setControlButtonsAreShown(true);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(fileFiltere);
    chooser.setDialogTitle(title);
    chooser.setAcceptAllFileFilterUsed(false);

    if (initFile != null) {
      chooser.setSelectedFile(initFile);
    }

    if (chooser.showDialog(parent, "Save") == JFileChooser.APPROVE_OPTION) {
      final File file = chooser.getSelectedFile();
      return file;
    }
    else {
      return null;
    }
  }

}
