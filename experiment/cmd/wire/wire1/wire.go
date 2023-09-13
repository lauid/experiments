//go:build wireinject
// +build wireinject

// The build tag makes sure the stub is not built in the final build.

package wire

import (
	wires "experiment/internal/wires"
	"github.com/google/wire"
)

// InitializeEvent 声明injector的函数签名
func InitializeEvent(msg string) wires.Event {
	wire.Build(wires.NewEvent, wires.NewGreeter, wires.NewMessage)
	return wires.Event{} //返回值没有实际意义，只需符合函数签名即可
}
