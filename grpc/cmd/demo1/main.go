package main

import (
	"context"
	"fmt"
	"github.com/lauid/grpc/pkg/grpc/pb"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
	"io/ioutil"
	"log"
	"net"
	"os/exec"
	"time"
)

var _ pb.Demo1ServiceServer = (*server)(nil)

type server struct {
	pb.UnimplementedDemo1ServiceServer
}

type Message struct {
	Msg []byte
	Err error
}

type SysReader struct {
}

func (s *SysReader) procInfo(ctx context.Context, msgChan chan Message, file string) {
	defer close(msgChan)
	timer := time.NewTicker(time.Second * 1)
	defer timer.Stop()

	for {
		select {
		case <-ctx.Done():
			fmt.Println("cancel.")
			return
		case <-timer.C:
			fmt.Println("reading...")
			fileName := "/proc/meminfo"
			if file != "" {
				fileName = "/proc/" + file
			}
			data, err := ioutil.ReadFile(fileName)
			if err != nil{
				msgChan <- Message{
					Msg: data,
					Err: fmt.Errorf("文件读取出错：%w",err),
				}
				return
			}
			msgChan <- Message{
				Msg: data,
				Err: nil,
			}

		default:

		}
	}
}

func (s *server) ServerStreamingMethod(req *pb.RequestMessage, stream pb.Demo1Service_ServerStreamingMethodServer) error {
	messageChan := make(chan Message, 100)

	sysReader := SysReader{}
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	go sysReader.procInfo(ctx, messageChan, req.Message)

	for message := range messageChan{
		streamMsg := string(message.Msg)
		if message.Err != nil{
			streamMsg += message.Err.Error()
		}

		resp := &pb.ResponseMessage{
			Message: streamMsg,
		}

		if err := stream.Send(resp); err != nil {
			fmt.Println(err)
			return err
		}
		if message.Err != nil {
			return message.Err
		}
	}
	return nil
}

func execCommand(name string, args ...string) string {
	cmd := exec.Command(name, args...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		log.Println(err)
	}
	return string(output)
}

func main() {
	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Fatalf("failed to listen:%v", err)
	}
	s := grpc.NewServer()
	reflection.Register(s)
	pb.RegisterDemo1ServiceServer(s, &server{})

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
