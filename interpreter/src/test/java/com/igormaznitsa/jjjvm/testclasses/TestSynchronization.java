package com.igormaznitsa.jjjvm.testclasses;

public class TestSynchronization {

  public synchronized static void smethod1() {
    System.out.println(1);
    System.out.println(2);
    System.out.println(3);
    System.out.println(4);
    System.out.println(5);
  }

  public synchronized static void smethod2() {
    System.out.println(6);
    System.out.println(7);
    System.out.println(8);
    System.out.println(9);
    System.out.println(10);
  }

  private final Object synchro = new Object();

  public void method1() {
    synchronized (synchro) {
      System.out.println(1);
      System.out.println(2);
      System.out.println(3);
      System.out.println(4);
      System.out.println(5);
    }
  }

  public void method2() {
    synchronized (synchro) {
      System.out.println(6);
      System.out.println(7);
      System.out.println(8);
      System.out.println(9);
      System.out.println(10);
    }
  }

}
