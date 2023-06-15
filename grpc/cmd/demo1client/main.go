package main

import (
	"context"
	"flag"
	"github.com/lauid/grpc/pkg/grpc/pb"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"io"
	"log"
)

var file = flag.String("proc","meminfo","proc file")

func main() {
	conn, err := grpc.Dial("127.0.0.1:50051", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()

	message := "meminfo"
	if *file != "" {
		message = *file
	}

	// 创建发送结构体
	req := pb.RequestMessage{
		//Message: "stream server grpc",
		//Message: "meminfo",
		Message: message,
	}
	// 调用我们的服务(ListValue方法)
	grpcClient := pb.NewDemo1ServiceClient(conn)
	stream, err := grpcClient.ServerStreamingMethod(context.Background(), &req)
	if err != nil {
		log.Fatalf("Call ListStr err: %v", err)
	}
	for {
		//Recv() 方法接收服务端消息，默认每次Recv()最大消息长度为`1024*1024*4`bytes(4M)
		res, err := stream.Recv()
		// 判断消息流是否已经结束
		if err == io.EOF {
			break
		}
		if err != nil {
			log.Fatalf("ListStr get stream err: %v", err)
		}
		// 打印返回值
		log.Println(res.Message)
	}
}

