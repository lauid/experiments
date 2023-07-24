package main

import (
	"fmt"
	"github.com/streadway/amqp"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"strconv"
	"sync"
	"syscall"
	"time"
)

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

func main() {
	defer fmt.Println("exit.............")
	// 连接到 RabbitMQ 服务器
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	// 创建一个通道
	ch, err := conn.Channel()
	if err != nil {
		failOnError(err, "Failed to open a channel")
	}
	defer ch.Close()

	// 声明一个队列
	q, err := ch.QueueDeclare(
		"my_queue",
		false,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		failOnError(err, "failed to declare a queue.")
	}

	consumeCtrl(ch, q)

	go publish(ch, q)

	sigChan := make(chan os.Signal, 2)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	<-sigChan
}

func publish(ch *amqp.Channel, q amqp.Queue) {
	// 发送消息到队列
	body := "H"
	counter := 0
	for true {
		counter += 1
		err := ch.Publish(
			"",
			q.Name,
			false,
			false,
			amqp.Publishing{
				ContentType: "text/plain",
				Body:        []byte(body + strconv.Itoa(counter)),
			},
		)
		failOnError(err, "Failed to publish a msg.")
		fmt.Print(">")
		time.Sleep(100 * time.Millisecond)
	}
}

func consumeCtrl(ch *amqp.Channel, q amqp.Queue) {
	// 启动多个消费者来处理消息
	numConsumers := 3
	wg := sync.WaitGroup{}
	wg.Add(numConsumers)
	for i := 0; i < numConsumers; i++ {
		go consume(ch, q, i)
		wg.Done()
	}
	wg.Wait()
}

func consume(ch *amqp.Channel, q amqp.Queue, i int) {
	msgs, err := ch.Consume(
		q.Name, // 队列名
		"c"+strconv.Itoa(i),     // 消费者标签
		false,  // 是否自动应答
		false,  // 是否独占队列
		false,  // 是否阻塞等待
		false,  // 额外的属性
		nil,
	)
	failOnError(err, "Failed to register a consumer")

	//for d := range msgs {
	//	//log.Printf("%d.Received a message: %s", i, d.Body)
	//}
	for d := range msgs {
		fmt.Print(i, ",", string(d.Body), ".")
		if rand.Intn(3) <= 2 {
			time.Sleep(1 * time.Second)
		}
	}
}
