package main

import (
	"fmt"
	"sync"
)

type singleton struct {
}

var app *singleton
var once sync.Once

func createApp() *singleton {
	once.Do(func() {
		app = &singleton{}
	})

	return app
}

func main() {
	for i := 0; i < 5; i++ {
		app1 := createApp()
		fmt.Printf("%p\n", app1)
	}
}
