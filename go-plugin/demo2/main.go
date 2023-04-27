package main

import (
	"fmt"

	_ "github.com/lauid/common"
	"github.com/lauid/demo2/pkg"
)

func main() {
	fmt.Println("try to load plugin...")

	err := pkg.LoadPlugin("../demo2-plugins/plugin2.so")
	if err != nil {
		fmt.Println("LoadPlugin error:", err)
		return
	}

	fmt.Println("LoadPlugin ok")

	err = pkg.LoadPlugin("../demo2-plugins/plugin2.so")
	if err != nil {
		fmt.Println("Re-loadPlugin error:", err)
		return
	}

	fmt.Println("Re-LoadPlugin ok")

}
