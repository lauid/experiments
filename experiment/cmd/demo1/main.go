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
}

func Case1(a any) any {
	if v, ok := a.(int); ok {
		return v * 2
	}
	return a
}

func Case2(a any) any {
	if v, ok := a.(int); ok {
		return v * 2
	}
	return a
}

func main4() {
OuterLoop:
	for i := 0; i < 2; i++ {
	innerLoop:
		for j := 0; j < 5; j++ {
			switch j {
			case 2:
				fmt.Println(i, j)
				break innerLoop
			case 3:
				fmt.Println(i, j)
				break OuterLoop
			}
		}
	}

	fmt.Println("aaaaaaaaaaaa.")
}

func main3() {
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
