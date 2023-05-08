package pkg

import "net/rpc"

const HelloServiceName = "HelloServiceName"

type HelloServiceInterface interface {
	Hello(request string, reply *string) error
}

func RegisterHelloService(svc HelloServiceInterface) error {
	return rpc.RegisterName(HelloServiceName, svc)
}

var _ HelloServiceInterface = (*HelloService)(nil)

type HelloService struct {
}

func (p *HelloService) Hello(request string, reply *string) error {
	*reply = "hello:" + request

	return nil
}
