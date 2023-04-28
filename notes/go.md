sudo tar -C /usr/local/ -xzf go1.19.7.linux-amd64.tar.gz


go env -w GO111MODULE=on


go env -w  GOPROXY=https://goproxy.cn,direct

export PATH=$PATH:/usr/local/go/bin:~/go/bin