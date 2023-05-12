package main

import (
	"fmt"
	"io"
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/tarm/serial"
)

func main() {
	res := Join([]string{"a=1", "b=2"}, "&")
	fmt.Println(res)
}

func Join(a []string, sep string) string {
	if len(a) == 0 {
		return ""
	}
	if len(a) == 1 {
		return a[0]
	}
	n := len(sep) * (len(a) - 1)
	for i := 0; i < len(a); i++ {
		n += len(a[i])
	}

	b := make([]byte, n)
	bp := copy(b, a[0])
	for _, s := range a[1:] {
		bp += copy(b[bp:], sep)
		bp += copy(b[bp:], s)
	}
	return string(b)
}

func main2() {
	//设置串口编号
	ser := &serial.Config{Name: "COM4", Baud: 115200}

	//打开串口
	conn, err := serial.OpenPort(ser)
	defer fmt.Println("close......")
	defer conn.Close()
	if err != nil {
		log.Fatal(err)
	}

	//启动一个协程循环发送
	go func() {
		for {
			revData := []byte("echo bbcc > /tmp/test.log && cat /tmp/test.log\r\n")
			_, err := conn.Write(revData)
			if err != nil {
				log.Println(err)
				continue
			}
			log.Printf("Tx:%s \n", revData)
			time.Sleep(time.Second)
		}
	}()

	//保持数据持续接收
	go func() {
		time.Sleep(1 * time.Second)
		for {
			var buf []byte
			for {
				b := make([]byte, 1024)
				lens, err := conn.Read(b)
				if err != nil {
					switch err {
					case io.EOF:
						if buf != nil {
							//log.Printf("Rx:%x \n", buf)
							buf = nil
							break
						}
					default:
						log.Println(err)
						buf = nil
						break
					}
				}
				buf = append(buf, b[:lens]...)
				if strings.LastIndex(string(buf), "\r\n") > 0 {
					break
				}
			}
			log.Printf("Rx:%s \n", buf)
		}
	}()

	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, syscall.SIGTERM, syscall.SIGINT)
	fmt.Println(<-signalChan)
}
func main1() {
	c := &serial.Config{Name: "/dev/ttymxc4", Baud: 115200}
	s, err := serial.OpenPort(c)
	if err != nil {
		log.Fatal(err)
	}

	// 发送数据
	n, err := s.Write([]byte("hello"))
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("Sent %d bytes\n", n)

	// 接收数据
	buf := make([]byte, 128)
	n, err = s.Read(buf)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("Received %d bytes: %s\n", n, string(buf[:n]))

	// 关闭串口连接
	err = s.Close()
	if err != nil {
		log.Fatal(err)
	}
}
