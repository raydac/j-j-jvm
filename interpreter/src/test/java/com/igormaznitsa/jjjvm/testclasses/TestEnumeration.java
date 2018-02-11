package com.igormaznitsa.jjjvm.testclasses;

import java.util.List;

public class TestEnumeration {

  public String testDISABLED(){
    return TestEnum.DISABLED.getCode();
  }

  public String testENABLED(){
    return TestEnum.getByCode("Y").getCode();
  }
}