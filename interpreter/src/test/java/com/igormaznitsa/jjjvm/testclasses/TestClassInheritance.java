package com.igormaznitsa.jjjvm.testclasses;

public class TestClassInheritance {
  public interface SomeInterface {
    public static final double dblStatField = 123.456d;
  }
  
  public static abstract class Klazz1 implements SomeInterface{
    public int field1;
    public int method1(int a){
      return a * field1;
    }
  }
  
  public static abstract class Klazz2 extends Klazz1{
    public int field2;
    public int method2(int a) {
      return a / field2;
    }
  } 
  
  public static class Klazz3 extends Klazz2{
    public int field3;
    
    public int method3(int a) {
      return a + field3;
    }

    public int calc(int a){
      return method3(method2(method1(a)));
    }
  }
}
