package main

import (
	"fmt"
	"github.com/tarm/serial"
	"io"
	"log"
	"os"
	"time"
)

const (
	a uint8 = 1 << iota
	b
	c
	d
	e
	f
)

func main() {
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

func main4() {
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
func main3() {
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

func main1() {
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

	// goroutine to monitor data read from the serial port
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
func main2() {
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
