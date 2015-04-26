package com.igormaznitsa.jjjvm.testclasses;

public final class TestKlazz1 {
  public static int sfield1 = 1234;
  public static long sfield2 = 56787L;
  public static String sfield3 = "Hello world";
  
  private final int field1 = 9876;
  private long field2 = 6666L;
  private String field3 = "Ugums";
  private double field4;
  
  public double doTableSwitch(long i){
    switch((int)i){
      case -1: return -i;
      case 0 : return i+1.3d;
      case 1 : return i*2;
      case 2 : return i*4;
      default: return i*i;
    }
  }
  
  public TestKlazz1(final double dbl){
    this.field4 = dbl;
  }
  
  public double doCalc(double a){
    return this.field4+a/34-(23*a);
  }
}
