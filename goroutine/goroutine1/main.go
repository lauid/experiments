package main

import (
	"fmt"
	"io"
	"net/http"
	"time"
)

type BarrierResponse struct {
	Err    error
	Status int
	Resp   string
}

func Barrier(urls []string) {
	requestNumber := len(urls)
	in := make(chan BarrierResponse, requestNumber)
	response := make([]BarrierResponse, requestNumber)
	defer close(in)

	for _, url := range urls {
		go doRequest(in, url)
	}

	var hasErr bool
	for i := 0; i < requestNumber; i++ {
		res := <-in
		if res.Err != nil {
			hasErr = true
			fmt.Println("Err: ", res.Err)
		}

		response[i] = res
	}

	if !hasErr {
		for _, barrierResponse := range response {
			fmt.Println(barrierResponse.Status)
		}
	}
}

func doRequest(out chan<- BarrierResponse, url string) {
	client := http.Client{
		Timeout: time.Duration(20 * time.Second),
	}
	res := BarrierResponse{}
	resp, err := client.Get(url)
	if resp != nil {
		res.Status = resp.StatusCode
	}

	if err != nil {
		res.Err = err
		out <- res
		return
	}

	byt, err := io.ReadAll(resp.Body)
	defer resp.Body.Close()
	if err != nil {
		res.Err = err
		out <- res
		return
	}

	res.Resp = string(byt)
	out <- res
	return
}

func main() {
	Barrier([]string{"https://yesno.wtf/api", "https://httpbin.org", "https://pokeapi.co"})
}
