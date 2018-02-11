package com.igormaznitsa.jjjvm.testclasses;

public enum TestEnum {
  DISABLED("N"),
  ENABLED("Y");

  private String code;

  TestEnum(String code) {
    this.code = code;
  }

  public static TestEnum getByCode(String code) {
    for (TestEnum e : values()) {
      if (e.code.equals(code)) {
        return e;
      }
    }
    return null;
  }

  public String getCode() {
    return code;
  }
}