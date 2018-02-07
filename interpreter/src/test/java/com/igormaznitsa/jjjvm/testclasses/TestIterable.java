package com.igormaznitsa.jjjvm.testclasses;

import java.util.List;

public class TestIterable {

  public int iterate(List list){
    int counter=0;
    for (Object element: list) {
      counter++;
    }
    return counter;
  }
}