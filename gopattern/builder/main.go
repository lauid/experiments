package main

import "fmt"

type IComputer interface {
	MakeCpu()
	MakeKeyBoard()
	MakeScreen()
}

type Creator struct {
	computer IComputer
}

func (c *Creator) Construct() *IComputer {
	c.computer.MakeCpu()
	c.computer.MakeKeyBoard()
	c.computer.MakeScreen()
	return &c.computer
}

var _ IComputer = (*HuaWeiComputer)(nil)

type HuaWeiComputer struct {
	Cpu      string
	KeyBoard string
	Screen   string
}

func (h *HuaWeiComputer) MakeCpu() {
	h.Cpu = "cpu"
	fmt.Println("cpu building...")
}
func (h *HuaWeiComputer) MakeKeyBoard() {
	h.KeyBoard = "keyboard"
	fmt.Println("keyboard building...")
}
func (h *HuaWeiComputer) MakeScreen() {
	h.Screen = "screen"
	fmt.Println("screen building...")
}

func main() {
	h := HuaWeiComputer{}
	c := Creator{computer: &h}
	computer := c.Construct()
	fmt.Println("%+v", *computer)
}
