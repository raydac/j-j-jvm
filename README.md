[![Java 11.0+](https://img.shields.io/badge/java-11.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-cyan.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![YooMoney donation](https://img.shields.io/badge/donation-Yoo.money-blue.svg)](https://yoomoney.ru/to/41001158080699)

# History

In 2009, during my vacation, I was experimenting with code and developed a pure Java JVM interpreter compatible with
J2ME CLDC 1.0. This version didn’t support loading external classes, and the task was not purely academic. Within three
days, I had successfully written the interpreter and produced a proof of concept. The result worked well and ran
reasonably fast on a Nokia 6100. I then published the project as open source on my homepage under the name M-JVM.

In 2015, my homepage was redesigned and the old projects were removed. Since I believed this project could still be
useful—at least for learning purposes—I refactored it and published the source code on GitHub under the name J-J-JVM.
The updated version included several improvements: support for inner classes, support for double and long values, and
dozens of additional tests.

__The JVM interpreter doesn't contain any "Poka-yoke" (mistake-proofing) mechanism and verification of byte-code, it doesn't make any stack map verification and any requests or communication with Java security manager !__

# How to build

It is a regular maven project and doesn't have any magic, just build through `mvn clean install` in the root of the
project.

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
