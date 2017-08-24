[![Java 5.0+](https://img.shields.io/badge/java-5.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![Yandex.Money donation](https://img.shields.io/badge/donation-Я.деньги-yellow.svg)](http://yasobe.ru/na/iamoss)

# History
In 2009 I had some free time during weekend and decided to try to develop small JVM interpreter in pure Java which could be used in J2ME CLDC 1.0 platform because the platform didn't have any ClassLoader support but it was very useful to load compiled class files through network and execute them. The Development of the "proof of concept" took about 3 days and worked with good speed even on Nokia 6100. The project was published as OSS project on my home page (titled as "M-JVM") and I even detected some interest from mobile software developers.  

In 2015 I decided to rework my home page and removed the old project from there, but because it still can be useful for someone, I made refactored it and moved sources to GitHub under title J-J-JVM project. I made some improvements in the project, added support of inner classes, double and long values, added dozens of tests. The Library is tests for compatibility with Android API 2.0r1+.    

__The JVM interpreter doesn't contain any "Poka-yoke" (mistake-proofing) mechanism and verification of byte-code, it doesn't make any stack map verification and any requests or communication with Java security manager!__

# Hello world
For instance we have some class which prints "Hello world!" to the console.
```Java
package com.igormaznitsa.testjjjvm;

public class HelloWorld {
  public static void main(final String ... args){
    System.out.println("Hello world!");
  }
}
```
And we want to execute the main method within JJJVM. To Load the class body we use Javassist. But lets not just print the message but will catch it and make some processing.
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
And the code above will print
```
<<Hello world!>>
```
