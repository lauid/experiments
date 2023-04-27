package main

import (
	"fmt"
	"time"
)

func putInTea() <-chan string {
	vegetables := make(chan string)
	go func() {
		time.Sleep(5 * time.Second)
		vegetables <- "茶叶已经放入茶杯~"
	}()

	return vegetables
}

func main() {
	teaCh := putInTea()
	waterCh := boilingWater()

	fmt.Println("已经安排放茶叶和烧水，休息一会")
	time.Sleep(2 * time.Second)
	fmt.Println("沏茶了，看看茶叶和水好了吗？")
	tea := <-teaCh
	water := <-waterCh
	fmt.Println("准备好了，可以沏茶了。", tea, water)

}

func boilingWater() <-chan string {
	water := make(chan string)
	go func() {
		time.Sleep(5 * time.Second)
		water <- "水已经烧开~"
	}()

	return water
}
