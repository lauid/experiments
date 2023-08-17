package main

import (
	"context"
	"demo1"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
)

func main() {
	var (
		httpAddr = flag.String("http", ":8080", "http listen address")
	)
	flag.Parse()
	ctx := context.Background()
	srv := demo1.NewService()

	errChan := make(chan error)
	go func() {
		c := make(chan os.Signal, 1)
		signal.Notify(c, os.Interrupt, syscall.SIGTERM, syscall.SIGINT)
		errChan <- fmt.Errorf("%s", <-c)
	}()

	endpoints := demo1.Endpoints{
		GetEndpoint:      demo1.MakeGetEndpoint(srv),
		StatusEndpoint:   demo1.MakeStatusEndpoint(srv),
		ValidateEndpoint: demo1.MakeValidateEndpoint(srv),
	}

	go func() {
		log.Println("demo1 is listening on port :", *httpAddr)
		handler := demo1.NewHTTPServer(ctx, endpoints)
		errChan <- http.ListenAndServe(*httpAddr, handler)
	}()

	log.Fatalln(<-errChan)
}