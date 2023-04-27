package main

import "fmt"

func main() {
	//采购
	accessories := Buy(6)
	//组装
	compoutes := Build(accessories)
	//打包
	packs := Pack(compoutes)

	for p := range packs {
		fmt.Println(p)
	}
}

// Pack 打包
func Pack(in <-chan string) <-chan string {
	out := make(chan string)
	go func() {
		defer close(out)
		for c := range in {
			out <- fmt.Sprintf("打包（%s）", c)
		}
	}()

	return out
}

// Build 组装
func Build(in <-chan string) <-chan string {
	out := make(chan string)
	go func() {
		defer close(out)
		for c := range in {
			out <- fmt.Sprintf("组装(%s)", c)
		}
	}()

	return out
}

// Buy 工序1 采购
func Buy(n int) <-chan string {
	out := make(chan string)
	go func() {
		defer close(out)
		for i := 0; i < n; i++ {
			out <- fmt.Sprint("配件", i)
		}
	}()

	return out
}
