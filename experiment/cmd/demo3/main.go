package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func main() {
	main1()
}

func main11() {
	file, err := os.ReadFile("aa.log")
	if err != nil {
		log.Println(err)
		return
	}
	//fmt.Println(string(file))

	//fmt.Println(len(file))
	// 创建或打开日志文件
	file2, err := os.OpenFile("bb.log", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		log.Fatal(err)
	}
	defer func(file2 *os.File) {
		err := file2.Close()
		if err != nil {
			log.Println(err)
		}
	}(file2)

	for i := 0; i < len(file); i += 32 {
		end := i + 32
		if end > len(file) {
			end = len(file)
		}
		//fmt.Println(i)
		//fmt.Println(end)

		chunk := file[i:end]
		fmt.Println(string(chunk), ",")
		_, err := file2.Write(chunk)
		if err != nil {
			fmt.Println(err)
			return
		}
	}
}

func main3() {
	s := []string{"A", "B", "C"}
	counter := 0
	for _, v := range s {
		s = append(s, v)
		counter++
	}
	fmt.Println(counter)
	fmt.Println(s)
}

func main2() {
	var m = map[string]int{
		"A": 21,
		"B": 22,
		"C": 23,
	}
	counter := 0
	for k, v := range m {
		if counter == 0 {
			delete(m, "A")
		}
		counter++
		fmt.Println(k, v)
	}
	fmt.Println("counter is ", counter)
	fmt.Println(m)
}

func main1() {
	ctx, cancelFunc := context.WithCancel(context.Background())

	go func() {
		ticker := time.NewTicker(3 * time.Second)
		defer ticker.Stop()
		for true {
			select {
			case <-ticker.C:
				fmt.Println("timer1:", time.Now().Format(time.RFC3339))
			case <-ctx.Done():
				fmt.Println("timer1 ,cancel")
				return
			}
			sec := 5 - rand.Intn(5)
			time.Sleep(time.Duration(sec) * time.Second)
		}
	}()

	go func() {
		ticker := time.NewTicker(2 * time.Second)
		defer ticker.Stop()
		for true {
			select {
			case <-ticker.C:
				fmt.Println("timer2:", time.Now().Format(time.RFC3339))
			case <-ctx.Done():
				fmt.Println("timer2 ,cancel")
				return
			}
			sec := 5 - rand.Intn(5)
			time.Sleep(time.Duration(sec) * time.Second)
		}
	}()

	c := make(chan os.Signal, 1)
	signal.Notify(c, syscall.SIGINT, syscall.SIGTERM, syscall.SIGABRT)
	select {
	case sig := <-c:
		fmt.Println("service get signal: ", sig)
		cancelFunc()
	}
	time.Sleep(20 * time.Second)
	fmt.Println(ctx.Err())
}

func main4() {
	messages := make(chan string)
	go func() {
		for true {
			msg := <-messages
			fmt.Println("subscribe1 get message,", msg)
		}
	}()
	go func() {
		for true {
			msg := <-messages
			fmt.Println("subscribe2 get message,", msg)
		}
	}()

	for i := 0; i < 10; i++ {
		messages <- fmt.Sprintf("Message %d", i)
	}
	close(messages)
}
