package main

import (
	"fmt"
	"github.com/pkg/errors"
	"os"
)

type customerError struct {
	message string
}

func (e *customerError) Error() string {
	return e.message
}

func main() {
	main2()
}

func main4() {
	err := &customerError{
		message: "ERROR_A",
	}

	var ce *customerError
	if errors.As(err, &ce) {
		fmt.Println(ce.message)
	}
}

func main3() {
	err := errors.New("something went wrong")
	wrappedErr := fmt.Errorf("additional context: %w", err)

	// Unwrap example
	fmt.Println(errors.Unwrap(wrappedErr)) // Output: something went wrong

	// Wrap example
	newErr := errors.Wrap(wrappedErr, "more context")
	fmt.Println(newErr) // Output: more context: additional context: something went wrong

	// Unwrap example
	fmt.Println(errors.Unwrap(wrappedErr))
}
func main2(){
	err1 := errors.New("error message")
	err2 := errors.New("error message")
	if errors.Is(err1, err2) {
		fmt.Println("err1 and err2 are the same error")
	}else {
		fmt.Println("err1 and err2 are not the same error")
	}

	err11 := &customerError{
		"error message",
	}
	err22 := &customerError{
		"error message",
	}
	if errors.Is(err11, err22) {
		fmt.Println("err11 and err22 are the same error")
	} else {
		fmt.Println("err11 and err22 are not the same error")
	}
}

func main1() {
	//text := "hello world\r\n"
	//text = strings.TrimSuffix(text, "\n")
	//text = strings.TrimSuffix(text, "\r")
	//fmt.Printf("%q", text)

	err1 := errors.New("errorA")
	err12 := errors.New("errorA")
	err11 := err1
	if errors.Is(err1, err11) {
		fmt.Println("err1 is err11.")
	} else {
		fmt.Println("err1 is not err11.")
	}
	if errors.As(err1, err12) {
		fmt.Println("err1 is err12.")
	} else {
		fmt.Println("err1 is not err12.")
	}
	err2 := errors.New("errorB")
	//err2 := fmt.Errorf("errorB")
	err3 := errors.Wrap(err2, "num2 err")

	fmt.Println(err3)

	err4 := fmt.Errorf("num4 err: %w", err3)
	fmt.Println(err4)
	err5 := errors.Unwrap(err4)
	fmt.Println(err5)
	err6 := errors.Unwrap(err5)
	fmt.Println(err6)
}

func readFile(filename string) ([]byte, error) {
	data, err := os.ReadFile(filename)
	if err != nil {
		return nil, err
	}
	return data, nil
}

func processFile(filename string) error {
	data, err := readFile(filename)
	if err != nil {
		return fmt.Errorf("failed to read file2: %w", err)
	}
	fmt.Println(string(data))
	return nil
}

func main5() {
	err := processFile("1.txt")
	if err != nil {
		fmt.Println(err)
		fmt.Println(errors.Is(err, os.ErrNotExist))
		err = errors.Unwrap(err)
		fmt.Println(err)
		err = errors.Unwrap(err)
		fmt.Println(err)
		return
	}
}
