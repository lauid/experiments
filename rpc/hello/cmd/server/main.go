package main

import (
	"hello/pkg"
	"log"
	"net"
	"net/rpc"
	"net/rpc/jsonrpc"
)

func main() {
	err := pkg.RegisterHelloService(new(pkg.HelloService))
	if err != nil {
		log.Fatal("register service err:", err)
	}

	listener, err := net.Listen("tcp", ":2345")
	if err != nil {
		log.Fatal("ListenTCP error: ", err)
	}
	defer listener.Close()

	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Fatal("Accept error: ", err)
		}

		go rpc.ServeCodec(jsonrpc.NewServerCodec(conn))
	}
}
