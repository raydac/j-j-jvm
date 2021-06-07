[![Java 5.0+](https://img.shields.io/badge/java-5.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-cyan.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![YooMoney donation](https://img.shields.io/badge/donation-Yoo.money-blue.svg)](https://yoomoney.ru/to/41001158080699)

# History
In 2009 during my vacation time I was playing with code and made experiments to develop pure Java JVM interpreter which could be compatible with J2ME CLDC 1.0 (it didn't provide any possibility to load external class, and the task was not only academic one). During three days the interpreter written, and I got some "proof of concept". The result worked well and more or less fast on Nokia 6100. Then I published the project as OSS one on my home page (titled as "M-JVM").  

In 2015, my home page was reworked and old projects were removed. But because such project can be still useful for someone (as minimum for learning purposes), I made refactoring and published its sources on GitHub under title J-J-JVM project. Some improvements were made in the published project: added support of inner classes, added support for double and long values, added dozens of tests. The library written in manner to be compatible with Android API 2.0r1+.    

__The JVM interpreter doesn't contain any "Poka-yoke" (mistake-proofing) mechanism and verification of byte-code, it doesn't make any stack map verification and any requests or communication with Java security manager !__

# Hello world
For instance, you can see below class writing just "Hello world!" on console.
```Java
package com.igormaznitsa.testjjjvm;

public class HelloWorld {
  public static void main(final String ... args){
    System.out.println("Hello world!");
  }
}
```
Let use Javassist to load the class but make some changes in its execution with J-J-JVM library.
```Java
JJJVMProvider provider = new JSEProviderImpl(){
  @Override
  public Object invoke(JJJVMClass caller, Object instance, String jvmFormattedClassName, String methodName, String methodSignature, Object[] arguments) throws Throwable {
    if (jvmFormattedClassName.equals("java/io/PrintStream") && methodName.equals("println") && methodSignature.equals("(Ljava/lang/String;)V")){
      System.out.println("<<"+arguments[0]+">>");
      return null;
    }
    return super.invoke(caller, instance, jvmFormattedClassName, methodName, methodSignature, arguments); //To change body of generated methods, choose Tools | Templates.
  }
};
JJJVMClassImpl jjjvmClass = new JJJVMClassImpl(new ByteArrayInputStream(javassist.ClassPool.getDefault().get("com.igormaznitsa.testjjjvm.HelloWorld").toBytecode()), provider);
jjjvmClass.findMethod("main", "([Ljava/lang/String;)V").invoke(null, null);
```
and the code above will print
```
<<Hello world!>>
```
