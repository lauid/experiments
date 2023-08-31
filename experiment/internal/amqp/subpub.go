package amqp

import (
	"fmt"
	"github.com/streadway/amqp"
	"log"
	"time"
)

//发布/订阅队列（Publish/Subscribe Queue）：


func Pub() {
	conn, err := amqp.Dial("amqp://user:password@localhost:5672/")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatal(err)
	}
	defer ch.Close()

	err = ch.ExchangeDeclare(
		"logs",   // 交换机名称
		"fanout", // 交换机类型
		true,     // 是否持久化
		false,    // 是否自动删除
		false,    // 是否内部使用
		false,    // 是否等待服务器完成
		nil,      // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	timer := time.After(20 *time.Second)
	for true {
		select {
		case <-timer:
			fmt.Println("timer........")
			return
		default:
			time.Sleep(1 * time.Second)
		}

		body := "Hello, RabbitMQ!" + time.Now().Format(time.RFC3339)
		err = ch.Publish(
			"logs", // 交换机名称
			"",     // 路由键
			false,  // 是否立即发送
			false,  // 其他参数
			amqp.Publishing{
				ContentType: "text/plain",
				Body:        []byte(body),
			},
		)
		if err != nil {
			log.Fatal(err)
		}

		log.Printf("Sent a message: %s", body)
	}
}

func Sub() {
	conn, err := amqp.Dial("amqp://user:password@localhost:5672/")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatal(err)
	}
	defer ch.Close()

	q, err := ch.QueueDeclare(
		"",    // 随机队列名称
		false, // 是否持久化
		false, // 是否自动删除
		true,  // 是否排他性
		false, // 是否等待服务器完成
		nil,   // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	err = ch.QueueBind(
		q.Name, // 队列名称
		"",     // 路由键
		"logs", // 交换机名称
		false,  // 是否立即发送
		nil,    // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	msgs, err := ch.Consume(
		q.Name, // 队列名称
		"",     // 消费者名称
		true,   // 是否自动应答
		false,  // 是否排他性
		false,  // 是否阻塞
		false,  // 其他参数
		nil,
	)
	if err != nil {
		log.Fatal(err)
	}

	forever := make(chan bool)

	go func() {
		for d := range msgs {
			log.Printf("Received a message: %s", d.Body)
		}
	}()

	fmt.Println("Waiting for messages...")
	<-forever
}
