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

import com.igormaznitsa.jjjvm.stubgen.utils.SplashDialog;
import javax.swing.SwingUtilities;

public class main {

  public static final String APPLICATION = "MJVM Stub Generator";
  public static final String VERSION = "1.0.5";
  public static final String AUTHOR = "Igor Maznitsa (rrg4400@gmail.com)";

  public static final void main(String... _args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new SplashDialog("images/splash.jpg", 3000L);
        new MainForm();
      }
    });
  }
}
