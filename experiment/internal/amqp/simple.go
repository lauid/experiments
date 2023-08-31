package amqp

import (
	"fmt"
	"github.com/streadway/amqp"
	"log"
	"time"
)

//简单队列（Simple Queue）：

func SimpleQueue() {
	//tcp连接
	conn, err := amqp.Dial("amqp://user:password@localhost:5672/")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	//channel
	ch, err := conn.Channel()
	if err != nil {
		log.Fatal(err)
	}
	defer ch.Close()

	q, err := ch.QueueDeclare(
		"simple_queue", // 队列名称
		false,          // 是否持久化
		false,          // 是否自动删除
		false,          // 是否排他性
		false,          // 是否等待服务器完成
		nil,            // 其他参数
	)
	if err != nil {
		log.Fatal(err)
	}

	go consumer(ch, q)
	go publish(ch, q)

	forever := make(chan bool)

	fmt.Println("Waiting for messages...")
	<-forever
}

func consumer(ch *amqp.Channel, q amqp.Queue) {
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

	go func() {
		for d := range msgs {
			log.Printf("Received a message: %s\n", d.Body)
		}
	}()
}

func publish(ch *amqp.Channel, q amqp.Queue) {
	// 发送消息到队列
	body := "H"
	for true {
		err := ch.Publish(
			"",
			q.Name,
			false,
			false,
			amqp.Publishing{
				ContentType: "text/plain",
				Body:        []byte(body + time.Now().Format(time.RFC3339)),
			},
		)
		if err != nil {
			log.Fatalf("publish err: %s", err)
		}
		fmt.Print(">")
		time.Sleep(100 * time.Millisecond)
	}
}
