package main

import (
	"context"
	"log"
	"time"
)

func main() {
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
				time.Sleep(1*time.Second)
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
