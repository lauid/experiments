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
