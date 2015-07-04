# History
In 2009 I had some free time during weekend and decided to try to develop a small JVM interpreter which could be used under J2ME CLDC 1.0 because that platform didn't have any ClassLoader support but it was very useful to load compiled class files through network and execute them. The Development of proof of concept took about 3 days and worked with good speed even on Nokia 6100. The project was published as OSS project on my home page as "M-JVM" and even had some interest from mobile community.  

In 2015 I decided to rework my home page and remove the old project from there, but because it can be useful for someone, I made some refactoring and moved it to GitHub as J-J-JVM project. I have strongly improved the project, now it supports inner classes, double and long values, has a lot of tests. The Library is compatible with Android API 2.0r1.    

__The JVM interpreter doesn't contain any "Poka-yoke" (mistake-proofing) mechanism and checking in the byte-code processor, it doesn't make any stack map checking and doesn't make any requests to security manager!__

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
And we want to execute the main method of the class with J-J-JVM. To Load the class body we use Javassist. But lets not just print the message but will catch it and make some processing.
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

# Donation   
If you like the software you can make some donation to the author   
[![https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
