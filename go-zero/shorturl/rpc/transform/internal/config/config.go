package config

import "github.com/zeromicro/go-zero/zrpc"

type Config struct {
	zrpc.RpcServerConf
	Transform zrpc.RpcClientConf // 手动代码
}
