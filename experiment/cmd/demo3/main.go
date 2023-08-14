package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func main() {
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
		ticker := time.NewTicker(2 * time.Second)
		defer ticker.Stop()
		for true {
			select {
			case <-ticker.C:
				fmt.Println("timer1:", time.Now().Format(time.RFC3339))
			case <-ctx.Done():
				fmt.Println("timer1 ,cancel")
				return
			}
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
		}
	}()

	c := make(chan os.Signal, 1)
	signal.Notify(c, syscall.SIGINT, syscall.SIGTERM, syscall.SIGABRT)
	select {
	case sig := <-c:
		fmt.Println("service get signal: ", sig)
		cancelFunc()
	}
	fmt.Println(ctx.Err())
}
