package main

import "fmt"

/*
#include "hi.h" //非标准c头文件，所以用引号

#cgo CFLAGS: -I./
#cgo LDFLAGS: -L./libs -lhi
*/
import "C"

func main() {
	C.hi()

	fmt.Println("Hello cgo.")
}
