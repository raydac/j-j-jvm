package com.igormaznitsa.jjjvm.testclasses;

import java.io.IOException;

public class TestThrow {

  public void throwIOE() throws IOException {
    throw new IOException("IOE");
  }

  public void throwDIFF(int val) throws Exception {
    try {
      switch (val) {
        case 0:
          throw new ArithmeticException("arith");
        case 1:
          throw new UnsupportedOperationException("uns");
        default:
          throw new NullPointerException("npe");
      }
    }
    catch (NullPointerException ex) {
      throw new IllegalStateException("ise", ex);
    }
  }
}
