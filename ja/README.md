#javac HelloWorld.java
#java HelloWorld

如果我们 Java 编译后的class文件不在当前目录，我们可以使用 -classpath 来指定class文件目录：

C:> java -classpath C:\java\DemoClasses HelloWorld
以上命令中我们使用了 -classpath 参数指定了 HelloWorld 的 class 文件所在目录。

如果class文件在jar文件中，则命令如下：

c:> java -classpath C:\java\myclasses.jar

