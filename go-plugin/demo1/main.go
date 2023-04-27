package main

import (
	"fmt"

	"github.com/lauid/demo1/pkg"
)

func main() {
	fmt.Println("try to LoadAndInvokeSomethingFromPlugin....")
	err := pkg.LoadAndInvokeSomethingFromPlugin("../demo1-plugins/plugin1.so")
	if err != nil {
		fmt.Println("LoadAndInvokeSomethingFromPlugin error: ", err)
		return
	}

	fmt.Println("LoadAndInvokeSomethingFromPlugin ok")
}
