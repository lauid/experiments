package main
//
//import "fmt"
//import "errors"
//
//type MyError struct {
//	message string
//}
//
//func (e *MyError) Error() string {
//	return e.message
//}
//
//func main() {
//	err1 := &MyError{"error message"}
//	err2 := &MyError{"error message"}
//	if errors.Is(err1, err2) {
//		fmt.Println("err1 and err2 are the same error")
//	}
//}
//
//
import (
	"errors"
	"fmt"
)
func main() {
	err1 := errors.New("something went wrong")
	err2 := fmt.Errorf("aa %w",err1)
	if errors.Is(err2, err1) {
		fmt.Println("err1 and err2 have the same error value")
	} else {
		fmt.Println("err1 and err2 have different error values")
	}
}