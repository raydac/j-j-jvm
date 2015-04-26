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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.*;

public class SplashDialog extends JDialog implements Runnable {
  private static final long serialVersionUID = 1812332099452703575L;

  protected final long delay;

  public SplashDialog(final String splashImagePath, final long delayToShow) {
    super();

    this.delay = delayToShow;

    if (delayToShow <= 0) {
      throw new IllegalArgumentException("Delay is less than zero or equ");
    }

    setModal(true);
    setUndecorated(true);
    setResizable(false);

    final JPanel panel = new JPanel(new BorderLayout());

    final URL imgUrl = ClassLoader.getSystemResource(splashImagePath);
    if (imgUrl == null) {
      throw new IllegalArgumentException("Can't load splash image " + splashImagePath);
    }
    final ImageIcon splashImage = new ImageIcon(imgUrl);
    panel.add(new JLabel(splashImage), BorderLayout.CENTER);
    setContentPane(panel);

    final Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
    pack();

    setLocation((scrSize.width - getWidth()) / 2, (scrSize.height - getHeight()) / 2);
    setAlwaysOnTop(true);

    new Thread(this).start();
    new Thread(new Runnable() {
      @Override
      public void run() {
        setVisible(true);
      }
    }
    ).start();
  }

  @Override
  public void run() {
    while (!isVisible() && !Thread.currentThread().isInterrupted()) {
      Thread.yield();
    }

    if (Thread.currentThread().isInterrupted()) {
      dispose();
      return;
    }

    try {
      Thread.sleep(this.delay);
    }
    catch (Throwable thr) {
    }
    finally {
      this.dispose();
    }
  }
}
