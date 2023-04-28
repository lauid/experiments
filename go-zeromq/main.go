package main

import (
	"context"
	"fmt"
	"github.com/go-zeromq/zmq4"
	"log"
	"time"
)

const (
	addr = "tcp://127.0.0.1:5566"
)

func main2() {
	reqSock := zmq4.NewReq(context.Background())
	defer reqSock.Close()

	if err := reqSock.Listen(addr); err != nil {
		log.Fatalf("listen err: %s", err)
	}

	for {
		recvMsg, err := reqSock.Recv()
		if err != nil {
			log.Fatalf("recv msg err: %s", err)
		}
		log.Printf("recv msg %s", recvMsg)

		time.Sleep(time.Second * 2)

		msg := zmq4.NewMsgString("hello world")
		if err := reqSock.Send(msg); err != nil {
			log.Fatalf("send msg err: %s", err)
		}
	}
}

func main() {
	go func() {
		if err := hwserver(); err != nil {
			fmt.Println("server err:", err)
		}
	}()
	if err := hwClient(); err != nil {
		fmt.Println(err)
	}
}

func hwClient() error {
	ctx := context.Background()
	// Socket to talk to clients
	socket := zmq4.NewReq(ctx, zmq4.WithDialerRetry(time.Second))
	defer socket.Close()

	if err := socket.Dial(addr); err != nil {
		return fmt.Errorf("dial err: %w", err)
	}

	for {
		reply := fmt.Sprintf("Hello")
		if err := socket.Send(zmq4.NewMsgString(reply)); err != nil {
			return fmt.Errorf("sending reply: %w", err)
		}
		// Do some 'work'
		time.Sleep(time.Second)

		msg, err := socket.Recv()
		if err != nil {
			return fmt.Errorf("receiving: %w", err)
		}
		fmt.Println("client Received ", msg)

	}
}

func hwserver() error {
	ctx := context.Background()
	// Socket to talk to clients
	socket := zmq4.NewRep(ctx)
	defer socket.Close()
	if err := socket.Listen(addr); err != nil {
		return fmt.Errorf("listening: %w", err)
	}

	for {
		msg, err := socket.Recv()
		if err != nil {
			return fmt.Errorf("receiving: %w", err)
		}
		fmt.Println("Server Received ", msg)

		// Do some 'work'
		time.Sleep(time.Second)

		reply := fmt.Sprintf("World")
		if err := socket.Send(zmq4.NewMsgString(reply)); err != nil {
			return fmt.Errorf("sending reply: %w", err)
		}
	}
}

func main1() {
	ctx := context.Background()
	pubSock := zmq4.NewPub(ctx)
	//if pubSock != nil {
	//	log.Fatalf("new pub err: %v", pubSock)
	//}
	defer pubSock.Close()
	pullUrl := "tcp://127.0.0.1:5563"
	if err := pubSock.Listen(pullUrl); err != nil {
		log.Fatalf("listen %s err: %v", pullUrl, err)
	}

	msgA := zmq4.NewMsgFrom(
		[]byte("A"),
		[]byte("We would like to see this"),
	)
	msgB := zmq4.NewMsgFrom(
		[]byte("B"),
		[]byte("We would like to see this"),
	)

	go sub(pullUrl)

	var err error
	for {
		err = pubSock.Send(msgA)
		if err != nil {
			log.Fatal(err)
		}

		err = pubSock.Send(msgB)
		if err != nil {
			log.Fatal(err)
		}
		time.Sleep(time.Second * 2)
	}
}

func sub(addr string) {
	subSock := zmq4.NewSub(context.Background())

	err := subSock.Dial(addr)
	if err != nil {
		log.Fatal("dial err: ", err)
	}

	if err := subSock.SetOption(zmq4.OptionSubscribe, "B"); err != nil {
		log.Fatalf("count not subscribe:%v", err)
	}
	if err := subSock.SetOption(zmq4.OptionSubscribe, "A"); err != nil {
		log.Fatalf("count not subscribe:%v", err)
	}

	for {
		msg, err := subSock.Recv()
		if err != nil {
			log.Fatalf("recv message err: %v", err)
		}
		log.Printf("[%s] %s\n", msg.Frames[0], msg.Frames[1])
	}
}
