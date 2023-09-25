package main

import (
	"context"
	"fmt"
	"io"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/tarm/serial"
)

func main() {
	main1()
}

func main3() {
	ctx1, cancel1 := context.WithTimeout(context.Background(), 10*time.Second)
	go func() {
		for {
			select {
			case <-ctx1.Done():
				fmt.Println(ctx1.Err())
				return
			default:
				fmt.Println("...")
				time.Sleep(2 * time.Second)
			}
		}
	}()

	listen()
	cancel1()
	time.Sleep(2 * time.Second)
}

func listen() {
	sig := make(chan os.Signal, 1)
	signal.Notify(sig, os.Interrupt, syscall.SIGTERM, syscall.SIGINT)
	fmt.Println(<-sig)
}

func main2() {
	ctx, cancel := context.WithCancel(context.Background())
	worker := func(name string) {
		for true {
			select {
			case <-ctx.Done():
				fmt.Println(name, ",", ctx.Err())
				return
			default:
				fmt.Println(name, time.Now().Format(time.RFC3339))
				time.Sleep(time.Duration(3-rand.Intn(3)) * time.Second)
			}
		}
	}
	worker2 := func(name string) {
		for true {
			select {
			case <-ctx.Done():
				fmt.Println(name, ",", ctx.Err())
				return
			default:
				fmt.Println(name, time.Now().Format(time.RFC3339))
				time.Sleep(time.Duration(3-rand.Intn(3)) * time.Second)
			}
		}
	}

	go worker("A")
	go worker("B")
	go worker2("C")

	sig := make(chan os.Signal, 1)
	signal.Notify(sig, os.Interrupt, syscall.SIGTERM, syscall.SIGINT)
	fmt.Println(<-sig)
	cancel()
	time.Sleep(2 * time.Second)
}

func main1() {
	defer fmt.Println("exit.......")
	c := &serial.Config{
		Name:        "/dev/ttyS4",
		Baud:        115200,
		ReadTimeout: time.Millisecond * 10,
	}
	s, err := serial.OpenPort(c)
	if err != nil {
		log.Fatal(err)
		return
	}
	defer func() {
		// 关闭串口连接
		err = s.Close()
		if err != nil {
			log.Fatal(err)
		}
		fmt.Println("close.")
	}()

	ctx, cancel := context.WithCancel(context.Background())
	//ctx1, cancel1 := context.WithTimeout(context.Background(), 10*time.Second)
	//go func() {
	//	for {
	//		select {
	//		case <-ctx.Done():
	//			fmt.Println("write.", ctx.Err())
	//			return
	//		default:
	//			fmt.Println("writing.")
	//			n, err := s.Write([]byte(time.Now().Format(time.RFC3339)))
	//			if err != nil {
	//				log.Fatal(err)
	//			}
	//			fmt.Printf("Sent %d bytes\n", n)
	//			time.Sleep(1 * time.Second)
	//		}
	//	}
	//}()

	go func() {
		for {
			select {
			//case <-ctx1.Done():
			//	fmt.Println("read.", ctx1.Err())
			//	return
			case <-ctx.Done():
				fmt.Println("read2.", ctx.Err())
				return
			default:
				fmt.Println("reading.")
				// 接收数据
				buf := make([]byte, 128)
				n, err := s.Read(buf)
				if err == io.EOF{
					return
				}
				if err != nil {
					log.Fatal(err)
					return
				}
				fmt.Printf("Received %d bytes: %s\n", n, string(buf[:n]))
			}
		}
	}()

	sig := make(chan os.Signal, 1)
	signal.Notify(sig, os.Interrupt, syscall.SIGTERM, syscall.SIGINT)
	fmt.Println(<-sig)
	cancel()
	fmt.Println("cancel")
	time.Sleep(2*time.Second)
}

const (
	a uint8 = 1 << iota
	b
	c
	d
	e
	f
)

func main6() {
	str1 := "HELLO,WORLD"
	fmt.Println(str1[:2])
	fmt.Println(len(str1))
	p := []string{"A", "B", "C", "D"}
	fmt.Println(p)
	fmt.Println(p[1:2])
	fmt.Println(p[2:])
	p1 := p[:0]
	fmt.Println(p1)
}

//func main5() {
//	if runtime.GOOS != "linux"{
//		return
//	}
//	file, err := os.OpenFile("test.txt", os.O_RDWR|os.O_CREATE, 0666)
//	if err != nil {
//		fmt.Println("open file error:", err)
//		return
//	}
//	defer file.Close()
//	fd := file.Fd()
//	// 加锁
//	if err := syscall.Flock(int(fd), syscall.LOCK_EX|syscall.LOCK_NB); err != nil {
//		fmt.Println("lock file error:", err)
//		return
//	}
//	defer func() {
//		// 解锁
//		if err := syscall.Flock(int(fd), syscall.LOCK_UN); err != nil {
//			fmt.Println("unlock file error:", err)
//		}
//	}()
//}

func main7() {
	// 配置串口参数
	config := &serial.Config{
		Name:        "/dev/ttyUSB0",
		Baud:        9600,
		ReadTimeout: time.Millisecond * 100, // 设置读取超时时间为100ms
	}
	// 打开串口
	port, err := serial.OpenPort(config)
	if err != nil {
		fmt.Println(err)
		return
	}
	// 定义超时函数，当串口10秒内没有读取到数据时，将会触发
	timeoutFunc := func() {
		fmt.Println("Read timeout")
		// 关闭串口连接
		port.Close()
	}
	// 定义一个计时器，10秒后执行timeoutFunc函数
	timer := time.AfterFunc(time.Second*10, timeoutFunc)
	// 读取串口数据
	buf := make([]byte, 128)
	n, err := port.Read(buf)
	if err != nil {
		fmt.Println(err)
		return
	}
	// 成功读取到数据，停止计时器
	timer.Stop()
	// 输出读取到的数据
	fmt.Println(string(buf[:n]))
}
func main8() {
	file, err := os.OpenFile("/tmp/upgrade.lock", os.O_RDWR|os.O_CREATE, 0755)
	if err != nil {
		fmt.Println(err)
		return
	}
	defer file.Close()

	// 获取文件锁
	if err := file.Truncate(0); err != nil {
		fmt.Println(err)
		return
	}
	if err := file.Sync(); err != nil {
		fmt.Println(err)
		return
	}

	// 释放文件锁
	if err := file.Truncate(0); err != nil {
		fmt.Println(err)
		return
	}
	if err := file.Sync(); err != nil {
		fmt.Println(err)
		return
	}

}

func main9() {
	config := &serial.Config{
		Name:        "/dev/ttymxc2",
		Baud:        115200,
		ReadTimeout: time.Millisecond * 100,
	}

	port, err := serial.OpenPort(config)
	if err != nil {
		panic(err)
	}
	defer port.Close()

	buf := make([]byte, 128)
	dataChan := make(chan []byte)

	// goroutine to monitor data read from the gokit port
	go func() {
		log.Println("read start")
		for {
			n, err := port.Read(buf)
			if err != nil && err != io.EOF {
				fmt.Println(err)
				return
			}
			if err == io.EOF {
				continue
			}
			if n > 0 {
				dataChan <- buf[:n]
			}
		}
	}()

	timeout := time.After(time.Second * 60)

	for {
		select {
		case data := <-dataChan:
			log.Printf(">>> Read %d bytes: %s\n", len(data), string(data))
			// do something with the data
			timeout = time.After(time.Second * 60)
		case <-timeout:
			log.Println("Timeout")
			return
		}
	}
}
func main10() {
	config := &serial.Config{
		Name:        "/dev/ttymxc2",
		Baud:        115200,
		ReadTimeout: time.Millisecond * 100,
	}

	port, err := serial.OpenPort(config)
	if err != nil {
		panic(err)
	}
	defer port.Close()

	buf := make([]byte, 128)
	timeout := time.After(time.Second * 60)

ForLabel:
	for {
		select {
		case <-timeout:
			log.Println("Timeout")
			return
		default:
			log.Println("Reading.")
			n, err := port.Read(buf)
			if err != nil && err != io.EOF {
				fmt.Println(err)
				return
			}
			if err == io.EOF {
				continue ForLabel
			}
			log.Printf(">>> Read %d bytes: %s\n", n, string(buf[:n]))
			// do something with the data
			timeout = time.After(time.Second * 60)
		}
	}
}
