package main

import (
	"flag"
	"fmt"
	"os"
)

var (
	gpio      = flag.String("gpio", "", "gpio")
	direction = flag.String("direction", "", "direction")
	value     = flag.String("value", "", "value")
)

func main() {
	flag.Parse()
	if *gpio == "" {
		panic(fmt.Errorf("gpio cannot empty"))
	}
	if *direction == "" {
		panic(fmt.Errorf("direction cannot empty"))
	}
	if *value == "" {
		panic(fmt.Errorf("value cannot empty"))
	}

	fmt.Println("-------准备操作GPIO: " + *gpio)
	fmt.Println("导出GPIO")
	err := writeToFile("/sys/class/gpio/export", *gpio)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("设置GPIO为输出方向")
	err = writeToFile("/sys/class/gpio/gpio"+*gpio+"/direction", *direction)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("控制GPIO输出高电平")
	err = writeToFile("/sys/class/gpio/gpio"+*gpio+"/value", *value)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("卸载GPIO")
	err = writeToFile("/sys/class/gpio/unexport", *gpio)
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
