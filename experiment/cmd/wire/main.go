package main

import (
	"experiment/cmd/wire/wire1"
	"experiment/internal/wires"
)

func main()  {
	main2()
}

func main1() {
	message := wires.NewMessage("ABCDED")
	greeter := wires.NewGreeter(message)
	event := wires.NewEvent(greeter)
	event.Start()
}

func main2()  {
	event := wire1.InitializeEvent("HELLO ABC")
	event.Start()
}
