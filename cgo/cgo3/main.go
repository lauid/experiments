package main

import "unsafe"

//#cgo pkg-config: libhello
//#include <stdlib.h>
//#include <hello.h>
import "C"

func main() {
	msg := "hello world."
	cmsg := C.CString(msg)
	C.hello(cmsg)
	C.free(unsafe.Pointer(cmsg))
}
