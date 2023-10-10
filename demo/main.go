package main

import (
	"fmt"
	"time"
)

func worker(errC chan error) {
	time.Sleep(1 * time.Second)
	//errC <- errors.New("this a err")
	errC <- nil
}

func main() {
	errC := make(chan error)
	go worker(errC)

	getErr := func(errC chan error) {
		select {
		case err := <-errC:
			if err != nil {
				fmt.Println("---------------------", err)
			} else {
				fmt.Println("no err")
			}
		}
	}

	getErr(errC)
}
