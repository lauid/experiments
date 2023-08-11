package main

import (
	"context"
	"fmt"
	"time"
)

func main() {
	defer fmt.Println("main ext.")
	ctx, cancel := context.WithCancel(context.Background())
	go doSomething1(ctx)
	time.Sleep(1 * time.Second)

	cancel()

	time.Sleep(10 * time.Second)
}

func doSomething1(ctx context.Context) {
	defer fmt.Println("do1 exit.")
	ctx = context.WithValue(ctx, "k", 1)
	doSomething2(ctx)
}

func doSomething2(ctx context.Context) {
	defer fmt.Println("do2 exit.")
	ctx = context.WithValue(ctx, "k", 2)
	doSomething3(ctx)
}

func doSomething3(ctx context.Context) {
	defer fmt.Println("do3 exit.")
	ctx = context.WithValue(ctx, "k", 2)
	select {
	case <-time.After(3 * time.Second):
		fmt.Println("operation complete")
	case <-ctx.Done():
		fmt.Println("operation cancel")
	}
}
