package main

import (
	"flag"
	"fmt"
	"github.com/stianeikeland/go-rpio"
	"os"
	"strconv"
)

func main() {
	var gpio string
	flag.StringVar(&gpio, "gpio", "", "gpio")
	if err := rpio.Open(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	defer rpio.Close()

	// 设置GPIO引脚为输出模式
	gpioI, _ := strconv.Atoi(gpio)
	fmt.Println(gpioI)
	pin := rpio.Pin(uint8(gpioI))
	pin.Output()

	// 设置GPIO引脚为高电平
	pin.High()

	// 测量GPIO引脚的电压
	voltage := strconv.FormatFloat(float64(pin.Read()), 'f', 2, 64)
	fmt.Printf("GPIO引脚的电压为 %vV\n", voltage)
}
