安装xgo docker镜像,这个镜像需要花较长的时间下载
docker pull karalabe/xgo-latest

这里latest指的最新的编译环境,如果需要的话也可以指定安装具体版本的镜像,在这里,这个版本的差异主要是golang版本的不同,由于编译的时候可以指定golang版本进行编译,所以我们直接安装最新版本即可.

安装xgo
<!-- go get github.com/karalabe/xgo -->
go install github.com/karalabe/xgo

设置GOPATH

$ cd $GOPATH/src

xgo --targets=darwin/amd64 .
