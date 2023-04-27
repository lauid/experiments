package main

import "fmt"

/*
#include <stdio.h>

void hi(){
	printf("hello world.\n");
}
*/
import "C" //这里可看作封装的伪包C, 这条语句要紧挨着上面的注释块，不可在它俩之间间隔空行！

func main() {
	C.hi()
	fmt.Println("Hi, c-go")
}
