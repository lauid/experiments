package main

import (
	"context"
	"fmt"
	"log"
	"time"
)

func main() {
	ctx := context.Background()
	ctxWithTimeout, cancelFunc := context.WithTimeout(ctx, 2*time.Second)
	defer cancelFunc()

	doSomething(ctxWithTimeout, "小黑")

	ctxWithTimeout2, cancelFunc2 := context.WithTimeout(ctx, 5*time.Second)
	defer cancelFunc2()

	doSomething(ctxWithTimeout2, "小白")
}

func doSomething(ctx context.Context, name string) {
	ctx = context.WithValue(ctx, "key", name)

	value := ctx.Value("key")
	fmt.Printf("Do something with %v\n", value)

	select {
	case <-time.After(3 * time.Second):
		fmt.Printf("%v operation completed\n", name)
	case <-ctx.Done():
		fmt.Printf("%v opeartion cancelled due to timeout or cancellation\n", name)
	}
}

func main1() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	go func(ctx context.Context) {
		for {
			select {
			case <-ctx.Done():
				log.Println("in goroutine cancel")
				return
			default:
				log.Println("goroutine to do")
				time.Sleep(1 * time.Second)
			}
		}
	}(ctx)

	for i := 0; i < 10; i++ {
		time.Sleep(1 * time.Second)
		if i == 5 {
			cancel()
			cancel()
			cancel()
			cancel()
			cancel()
		}
	}
}
