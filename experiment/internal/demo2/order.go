package demo2

import "log"

type OrderDBI interface {
	GetName(orderId int) string
}

type OrderInfo struct {
	orderId int
}

func (order OrderInfo) GetName(orderId int) string {
	log.Println("原本应该连接数据库获取名称")
	return "xd.cute"
}
