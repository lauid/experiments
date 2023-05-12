package main

import (
	"errors"
	"fmt"
	run "github.com/oklog/run"
	"time"
)

func main() {
	var g run.Group
	{
		g.Add(func() error {
			time.Sleep(time.Second * 10)
			return nil
		}, func(err error) {
			fmt.Printf("The 1 actor was interrupted with: %v\n", err)
		})
	}
	{
		cancel := make(chan struct{})
		g.Add(func() error {
			select {
			case <-time.After(time.Second * 5):
				fmt.Printf("--The first actor had its time elapsed\n")
				return nil
			case <-cancel:
				fmt.Printf("--The first actor was canceled\n")
				return nil
			}
		}, func(err error) {
			fmt.Printf("--The first actor was interrupted with: %v\n", err)
			close(cancel)
		})
	}
	{
		g.Add(func() error {
			fmt.Printf("The second actor is returning immediately\n")
			return errors.New("immediate teardown")
		}, func(err error) {
			fmt.Printf("The second actor was interrupted with: %v\n", err)
		})
	}

	fmt.Printf("The group was terminated with: %v\n", g.Run())
}
