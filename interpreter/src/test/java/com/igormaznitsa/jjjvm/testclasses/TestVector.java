package com.igormaznitsa.jjjvm.testclasses;

import java.util.Vector;

public class TestVector {
  public Vector fillVector(final Vector v){
    v.clear();
    for(int i=0;i<15;i++){
      v.add(i);
    }
    return v;
  }
  
  public Vector makeVector(){
    final Vector vec =  new Vector();
    for(int i=0;i<10; i++){
      vec.add(i);
    }
    return vec;
  }
}