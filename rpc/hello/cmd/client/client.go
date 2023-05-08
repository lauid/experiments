package main

import (
	"fmt"
	"hello/pkg"
	"log"
	"net"
	"net/rpc"
	"net/rpc/jsonrpc"
)

var _ pkg.HelloServiceInterface = (*HelloServiceClient)(nil)

type HelloServiceClient struct {
	*rpc.Client
}

func (h *HelloServiceClient) Hello(request string, reply *string) error {
	return h.Client.Call(pkg.HelloServiceName+".Hello", request, reply)
}

func DialRpc(network, address string) (*HelloServiceClient, error) {
	conn, err := net.Dial(network, address)
	if err != nil {
		log.Fatal("dialing err: ", err)
	}

	client := rpc.NewClientWithCodec(jsonrpc.NewClientCodec(conn))

	return &HelloServiceClient{
		Client: client,
	}, nil
}

func main() {
	client, err := DialRpc("tcp", "localhost:2345")
	defer client.Client.Close()

	var reply string
	err = client.Hello("hello", &reply)
	if err != nil {
		log.Fatal("client call error: ", err)
	}
	fmt.Println(reply)
}
