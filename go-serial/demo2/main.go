package main

import (
	"bufio"
	"context"
	"errors"
	"fmt"
	"github.com/tarm/serial"
	"log"
	"os"
	"strings"
	"time"
)

// SerialPort 串口结构体
type SerialPort struct {
	portName string
	baudRate int
	reader   *bufio.Reader
	writer   *bufio.Writer
	port     *serial.Port
}

// NewSerialPort 创建一个串口实例
func NewSerialPort(portName string, baudRate int) (*SerialPort, error) {
	c := &serial.Config{Name: portName, Baud: baudRate}
	s, err := serial.OpenPort(c)
	if err != nil {
		return nil, err
	}

	// 创建一个带缓冲的读取器
	reader := bufio.NewReader(s)

	writer := bufio.NewWriter(s)

	return &SerialPort{portName: portName, baudRate: baudRate, reader: reader, writer: writer, port: s}, nil
}

// ReadLine 读取一行数据
func (sp *SerialPort) ReadLine() (string, error) {
	line, err := sp.reader.ReadString('\n')
	if err != nil {
		return "", err
	}
	return line, nil
}

// Close 关闭串口连接
func (sp *SerialPort) Close() error {
	return sp.port.Close()
}

// Write 往串口写数据
func (sp *SerialPort) Write(msg string) error {
	//writeData := []byte("echo bbcc > /tmp/test.log && cat /tmp/test.log\r\n")
	writeData := []byte(msg)
	_, err := sp.port.Write(writeData)
	return err
}

func checkSerialOutPut(ctx context.Context, checkStr string, errChan chan error, startTime time.Time) {
	defer func(start time.Time) {
		log.Printf("Method checkSerialOutput took %s\n", time.Since(start))
	}(startTime)

	//time.Sleep(1 * time.Second)
	//errChan <- nil
	//return

	sp, err := NewSerialPort(serialName, 115200)
	if err != nil {
		//log.Println(err)
		errChan <- err
		return
	}
	defer sp.Close()

	successMarkCounter := 0
	log.Println("start", strings.Repeat("-", 20))
	for {
		line, err := sp.ReadLine()
		if err != nil {
			errChan <- err
			return
		}
		log.Print(line)
		if strings.Index(line, checkStr) != -1 {
			successMarkCounter += 1
		}

		if successMarkCounter >= 3 {
			break
		}
	}
	errChan <- nil
	log.Println("end", strings.Repeat("-", 20))
}

//写码
func checkWriteCode(code string) error {
	sp, err := NewSerialPort(serialName, 115200)
	if err != nil {
		return err
	}
	defer sp.Close()

	msg := fmt.Sprintf("echo '%s' > /tmp/test.log && cat /tmp/test.log\n", code)
	err = sp.Write(msg)
	if err != nil {
		return err
	}

	_, _ = sp.ReadLine()
	line, err := sp.ReadLine()
	if err != nil {
		return err
	}
	if strings.Index(line, code) != -1 {
		return nil
	}
	errMsg := fmt.Sprintf("写入:%v,读取:%v,不一致", code, line)
	return errors.New(errMsg)
}

//串口输出检测
func checkSerialUpgrade() {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	errChan := make(chan error, 0)
	startTime := time.Now()
	go checkSerialOutPut(ctx, "####################", errChan, startTime)
	select {
	case <-ctx.Done():
		log.Println("串口检测超时，", time.Since(startTime))
		return
	case err := <-errChan:
		if err != nil {
			log.Println("Exit err:", err)
			return
		} else {
			log.Println("串口检测升级成功")
		}
	}
}

// gpioSetValue gpio操作
func gpioSetValue(gpio string, value string) {
	fmt.Println("-------准备操作GPIO: " + gpio)
	fmt.Println("导出GPIO")
	err := writeToFile("/sys/class/gpio/export", gpio)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("设置GPIO为输出方向")
	err = writeToFile("/sys/class/gpio/gpio"+gpio+"/direction", "out")
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("控制GPIO输出高电平")
	err = writeToFile("/sys/class/gpio/gpio"+gpio+"/value", value)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("卸载GPIO")
	err = writeToFile("/sys/class/gpio/unexport", gpio)
	if err != nil {
		fmt.Println(err)
		return
	}
}

func writeToFile(filename string, data string) error {
	file, err := os.OpenFile(filename, os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = file.WriteString(data)
	if err != nil {
		return err
	}

	return nil
}

var (
	serialName string = "COM4"
)

func main() {
	//todo 短接
	log.Println("[短接]")
	//gpioSetValue("", "1")

	//上电
	log.Println("[上电]")
	//gpioSetValue("68", "1")

	//todo
	log.Println("[串口输出检测]")
	checkSerialUpgrade()

	//断电
	log.Println("断电")
	//gpioSetValue("68", "0")

	//todo 断开短接
	log.Println("断开短接")

	//上电
	log.Println("上电")
	//gpioSetValue("68", "1")

	//写码
	log.Println("写码")
	err := checkWriteCode("HELLO")
	if err != nil {
		fmt.Println(err)
	} else {
		log.Println("write success")
	}
}
