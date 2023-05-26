package main

import (
	"fmt"
	"strings"
)

func main() {
	text := "hello world\r\n"
	text = strings.TrimSuffix(text,"\n")
	text = strings.TrimSuffix(text,"\r")
	fmt.Printf("%q", text)
}
