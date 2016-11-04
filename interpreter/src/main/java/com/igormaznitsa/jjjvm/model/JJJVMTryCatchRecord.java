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
package com.igormaznitsa.jjjvm.model;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Record contains data about try..catch block.
 * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.5}
 */
public class JJJVMTryCatchRecord {

  protected final int pcStart;
  protected final int pcEnd;
  protected final int codeAddress;
  protected final String jvmFormattedClassName;

  public int getStartPC() {
    return pcStart;
  }

  public int getEndPC() {
    return pcEnd;
  }

  public int getCodeAddress() {
    return codeAddress;
  }

  public String getJvmFormattedClassName() {
    return jvmFormattedClassName;
  }

  public final boolean isActiveForAddress(final int pcReg) {
    return pcReg >= this.pcStart && pcReg <= this.pcEnd;
  }

  public JJJVMTryCatchRecord(final int pcStart, final int pcEnd, final int pcAddress, final JJJVMConstantPoolItem classRef) {
    this.jvmFormattedClassName = classRef == null ? null : classRef.asString();
    this.pcStart = pcStart;
    this.pcEnd = pcEnd;
    this.codeAddress = pcAddress;
  }

  public JJJVMTryCatchRecord(final JJJVMConstantPool constantPool, final DataInputStream inStream) throws IOException {
    this(inStream.readUnsignedShort(), inStream.readUnsignedShort(), inStream.readUnsignedShort(), constantPool.getItemAt(inStream.readUnsignedShort()));
  }

}
