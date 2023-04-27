vagrant@node3:~/code/demo/cmd/cgo3$ export LD_LIBRARY_PATH=/home/vagrant/code/demo/cmd/cgo3/lib:$LD_LIBRARY_PATH
vagrant@node3:~/code/demo/cmd/cgo3$ go build main.go
vagrant@node3:~/code/demo/cmd/cgo3$ 
vagrant@node3:~/code/demo/cmd/cgo3$
vagrant@node3:~/code/demo/cmd/cgo3$ ldd main
        linux-vdso.so.1 =>  (0x00007ffd889dc000)
        libhello.so => /home/vagrant/code/demo/cmd/cgo3/lib/libhello.so (0x00007f42c957e000)
        libpthread.so.0 => /lib/x86_64-linux-gnu/libpthread.so.0 (0x00007f42c9361000)
        libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007f42c8f97000)
        /lib64/ld-linux-x86-64.so.2 (0x00007f42c9780000)
vagrant@node3:~/code/demo/cmd/cgo3$ go run main.go 
---------------hello world.



-----

Golang使用pkg-config自动获取头文件和链接库的方法

使用pkg-config可以自动获取头文件和链接库的方法如下：

1. 安装pkg-config

在Linux系统中，pkg-config通常已经预装了。如果没有安装，可以使用以下命令安装：

```shell
sudo apt-get install pkg-config
```

2. 编写pkg-config文件

在项目根目录下创建一个名为`<package>.pc`的文件，其中`<package>`是要使用的库的名称。例如，如果要使用libcurl库，则文件名为`libcurl.pc`。

文件内容如下：

```text
prefix=/usr/local
exec_prefix=${prefix}
libdir=${exec_prefix}/lib
includedir=${prefix}/include

Name: <package>
Description: <description>
Version: <version>

Requires:
Libs: -L${libdir} -l<package>
Cflags: -I${includedir}
```

其中，需要替换以下内容：

- `<package>`：要使用的库的名称。
- `<description>`：库的描述信息。
- `<version>`：库的版本号。

3. 使用pkg-config

在Go代码中，可以使用`os/exec`包来执行pkg-config命令。例如，使用libcurl库的示例代码如下：

```go
package main

import (
    "os/exec"
)

func main() {
    cmd := exec.Command("pkg-config", "--cflags", "--libs", "libcurl")
    output, err := cmd.Output()
    if err != nil {
        panic(err)
    }
    flags := strings.Split(strings.TrimSpace(string(output)), " ")
    for _, flag := range flags {
        if flag != "" {
            fmt.Println(flag)
        }
    }
}
```

该示例代码执行`pkg-config --cflags --libs libcurl`命令，并输出获取到的头文件和链接库信息。可以根据需要修改命令中的`libcurl`为其他库的名称。