sudo tar -C /usr/local/ -xzf go1.19.7.linux-amd64.tar.gz


go env -w GO111MODULE=on

go env -w  GOPROXY=https://goproxy.cn,direct

export PATH=$PATH:/usr/local/go/bin:~/go/bin



那怎么知道执行go test的时候编译器是否做了内联优化呢？很简单，给go test增加-gcflags="-m"参数，-m表示打印编译器做出的优化决定。

$ go test -gcflags="-m" -v -bench=BenchmarkWrong -count 1



#### govulncheck

全新的govulncheck 命令可以帮助你发现代码里的安全漏洞。

安装和使用方法如下：

$ go install golang.org/x/vuln/cmd/govulncheck@latest
$ govulncheck ./...


#### GC

GODEBUG=gctrace=1 go run main.go
