参考： https://www.cnblogs.com/sctb/p/13816110.html

## 按照内核编译环境

> sudo apt-get install build-essential linux-headers-`uname -r` flex bison


获取从 "make test" 获得的值，并使用它来创建设备文件，以便我们可以从用户空间与内核模块进行通信：

> sudo mknod /dev/lkm_example c MAJOR 0
在上面的示例中，将MAJOR替换为你运行 "make test" 或 "dmesg" 后得到的值，我得到的MAJOR为236，如上图，mknod命令中的 "c" 告诉mknod我们需要创建一个字符设备文件。


现在我们可以从设备中获取内容：

> cat /dev/lkm_example

或者通过 "dd" 命令：

> dd if=/dev/lkm_example of=test bs=14 count=100

你也可以通过应用程序访问此设备，它们不必编译应用程序--甚至Python、Ruby和PHP脚本也可以访问这些数据。
