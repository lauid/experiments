package main

import (
	"bufio"
	"context"
	"flag"
	"fmt"
	"github.com/tarm/serial"
	"log"
	"time"
)

var (
	//tty = flag.String("tty", "/dev/ttymxc1", "tty")
	tty = flag.String("tty", "COM4", "tty")
)

func main() {
	flag.Parse()
	ttyDevice := *tty

	//设置串口编号
	ser := &serial.Config{Name: ttyDevice, Baud: 115200}

	var conn *serial.Port
	//打开串口
	conn, err := serial.OpenPort(ser)
	defer fmt.Println("close......")
	defer conn.Close()
	if err != nil {
		log.Fatal(err)
		time.Sleep(1 * time.Millisecond)
	}

	// 创建一个带缓冲的读取器
	reader := bufio.NewReader(conn)

	for {
		// 读取一行数据
		line, err := reader.ReadString('\n')
		if err != nil {
			fmt.Println(err)
			return
		}
		fmt.Print(line)
	}
}

func main1() {
	ctx, _ := context.WithTimeout(context.Background(), 6*time.Second)
	fmt.Println("start at ", time.Now().Format(time.RFC3339))

	go func() {
		defer func() {
			fmt.Println("goroutine exit ", time.Now().Format(time.RFC3339))
		}()

		for {
			select {
			case <-ctx.Done():
				fmt.Println("Done.", time.Now().Format(time.RFC3339))
				return
			default:
				fmt.Println("case default ", time.Now().Format(time.RFC3339))
				time.Sleep(time.Second)
			}
		}
	}()

	time.Sleep(20 * time.Second)
	fmt.Println("stop at ", time.Now().Format(time.RFC3339))
}
