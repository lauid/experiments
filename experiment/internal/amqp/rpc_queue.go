package amqp

import (
	"fmt"
	"github.com/google/uuid"
	"log"
	"math/rand"

	"github.com/streadway/amqp"
)

func RpcClient()  {
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
		"rpc_queue", // 队列名称
		false,       // 是否持久化
		false,       // 是否自动删除
		false,       // 是否排他性
		false,       // 是否等待服务器完成
		nil,         // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	msgs, err := ch.Consume(
		q.Name, // 队列名称
		"",     // 消费者名称
		false,  // 是否自动应答
		false,  // 是否排他性
		false,  // 是否阻塞
		false,  // 其他参数
		nil,
	)
	if err != nil {
		log.Fatal(err)
	}

	corrID := randomString(32)

	err = ch.Publish(
		"",        // 默认交换机
		"rpc_queue", // 队列名称
		false,     // 是否立即发送
		false,     // 其他参数
		amqp.Publishing{
			ContentType:   "text/plain",
			CorrelationId: corrID,
			ReplyTo:       q.Name,
			Body:          []byte("Request message " + uuid.New().String()),
		},
	)
	if err != nil {
		log.Fatal(err)
	}

	for d := range msgs {
		if corrID == d.CorrelationId {
			log.Printf("Received a response: %s", d.Body)
			d.Ack(false)
			break
		}
	}

	log.Println("RPC call completed")
}

func randomString(length int) string {
	const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	b := make([]byte, length)
	for i := range b {
		b[i] = charset[rand.Intn(len(charset))]
	}
	return string(b)
}

func RpcServer()  {
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
		"rpc_queue", // 队列名称
		false,       // 是否持久化
		false,       // 是否自动删除
		false,       // 是否排他性
		false,       // 是否等待服务器完成
		nil,         // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	err = ch.Qos(
		1,     // 每次处理的消息数量
		0,     // 处理消息的预取权值
		false, // 全局预取
	)
	if err != nil {
		log.Fatal(err)
	}

	msgs, err := ch.Consume(
		q.Name, // 队列名称
		"",     // 消费者名称
		false,  // 是否自动应答
		false,  // 是否排他性
		false,  // 是否阻塞
		false,  // 其他参数
		nil,
	)
	if err != nil {
		log.Fatal(err)
	}

	for d := range msgs {
		log.Println("recv msg:", string(d.Body))
		response := "Response message"
		err = ch.Publish(
			"",          // 默认交换机
			d.ReplyTo,   // 队列名称
			false,       // 是否立即发送
			false,       // 其他参数
			amqp.Publishing{
				ContentType:   "text/plain",
				CorrelationId: d.CorrelationId,
				Body:          []byte(response),
			},
		)
		if err != nil {
			log.Fatal(err)
		}

		d.Ack(false)
	}

	log.Println("RPC server stopped")
}



func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

func RpcQueue() {
	// 连接到 RabbitMQ 服务器
	conn, err := amqp.Dial("amqp://user:password@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	// 创建一个通道
	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	// 声明一个队列，用于接收请求消息
	q, err := ch.QueueDeclare(
		"rpc_queue", // 队列名称
		false,       // 持久化
		false,       // 自动删除
		false,       // 独占
		false,       // 不阻塞
		nil,         // 额外参数
	)
	failOnError(err, "Failed to declare a queue")

	// 注册消费者，用于接收请求并发送响应
	msgs, err := ch.Consume(
		q.Name, // 队列名称
		"",     // 消费者标识符
		false,  // 自动应答
		false,  // 独占
		false,  // 不阻塞
		false,  // 不等待服务器响应
		nil,    // 额外参数
	)
	failOnError(err, "Failed to register a consumer")

	forever := make(chan bool)

	// 处理请求消息并发送响应
	go func() {
		for d := range msgs {
			n, err := fmt.Sscan(string(d.Body))
			failOnError(err, "Failed to parse request body")

			response := fib(n)

			// 发送响应消息到指定的回调队列
			err = ch.Publish(
				"",        // 交换机名称
				d.ReplyTo, // 回调队列名称
				false,     // 不强制保存消息
				false,     // 不立即发送消息
				amqp.Publishing{
					ContentType:   "text/plain",
					CorrelationId: d.CorrelationId,
					Body:          []byte(fmt.Sprintf("%d", response)),
				},
			)
			failOnError(err, "Failed to publish a response")

			// 手动确认收到消息
			d.Ack(false)
		}
	}()

	log.Printf(" [*] Waiting for RPC requests...")
	<-forever
}

func fib(n int) int {
	if n <= 1 {
		return n
	}
	return fib(n-1) + fib(n-2)
}
