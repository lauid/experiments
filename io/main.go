package main

import (
	"bytes"
	"fmt"
	"io"
	"os"
	"strings"
)

func main() {
	multiWriter()
}

func teeReader() {
	fileCreate, err := os.Open("hello.log")
	if err != nil {
		panic(err)
	}

	reader := io.TeeReader(fileCreate, os.Stdout)
	b := make([]byte, 20)
	reader.Read(b)
}

func multiWriter() {
	fileCreate, err := os.OpenFile("hello.log", os.O_APPEND|os.O_RDWR, 0666)
	if err != nil {
		panic(err)
	}
	writers := io.MultiWriter(fileCreate, os.Stdin)
	_, err = writers.Write([]byte("GO HELLO."))
	if err != nil {
		panic(err)
	}
}

func multiReader() {
	readers := io.MultiReader(
		strings.NewReader("HELLO world."),
		bytes.NewBufferString("FOO BAR."),
	)

	p := make([]byte, 1000)
	buf := make([]byte, 2048)

	for n, err := readers.Read(p); err != io.EOF; n, err = readers.Read(p) {
		buf = append(buf, p[:n]...)
	}

	fmt.Println(string(buf))
}

func ReadFrom(reader io.Reader, num int) ([]byte, error) {
	p := make([]byte, num)
	n, err := reader.Read(p)
	if n > 0 {
		return p[:n], nil
	}

	return p, err
}

func main1() {
	data, err := ReadFrom(os.Stdin, 11)
	if err != nil {
		fmt.Println(err)
	}
	fmt.Println(string(data))

	data, err = ReadFrom(strings.NewReader("Hello world."), 11)
	if err != nil {
		fmt.Println(err)
	}
	fmt.Println(string(data))
}
