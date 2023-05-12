package main

import (
	"flag"
	"fmt"
	"github.com/tarm/serial"
	"log"
)

var (
	tty = flag.String("tty", "/dev/ttymxc1", "tty")
)

func main() {
	flag.Parse()
	ttyDevice := *tty

	//设置串口编号
	ser := &serial.Config{Name: ttyDevice, Baud: 115200}

	//打开串口
	conn, err := serial.OpenPort(ser)
	defer fmt.Println("close......")
	defer conn.Close()
	if err != nil {
		log.Fatal(err)
	}

	buf := make([]byte, 128)
	for {
		n, err := conn.Read(buf)
		if err != nil {
			fmt.Println("Error reading from serial port: ", err)
			return
		}

		fmt.Println(string(buf[:n]))
	}
}
