package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func main() {
	defer func() {
		res := recover()
		if res != nil {
			fmt.Println(res)
		}
	}()
	// 配置Kafka生产者和消费者共享的参数
	addr := "192.168.31.149:9092"
	config := &kafka.ConfigMap{
		"bootstrap.servers": addr,
	}
	consumerConfig := &kafka.ConfigMap{
		"bootstrap.servers": addr,
		"group.id":          "my-consumer-group",
		"auto.offset.reset": "earliest",
	}

	// 创建Kafka生产者
	producer, err := kafka.NewProducer(config)
	if err != nil {
		log.Fatal(err)
	}
	defer producer.Close()

	// 创建等待组，用于等待协程执行完毕
	var wg sync.WaitGroup
	wg.Add(2) // 两个协程

	topic := "my-topic"

	// 生产者协程
	go func() {
		defer wg.Done()
		deliveryChan := make(chan kafka.Event)
		for i := 0; i < 100; i++ {
			message := &kafka.Message{
				TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
				Value:          []byte(fmt.Sprintf("Message %d", i+1)),
				Key:            []byte("key"),
			}
			err := producer.Produce(message, deliveryChan)
			if err != nil {
				log.Println("Error:", err)
			} else {
				e := <-deliveryChan
				m := e.(*kafka.Message)

				if m.TopicPartition.Error != nil {
					log.Println("Delivery failed:", m.TopicPartition.Error)
				} else {
					fmt.Printf("Produced message: Topic=%s, Partition=%d, Offset=%d\n",
						*m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset)
				}
			}
		}
	}()

	// 消费者协程
	go func() {
		defer wg.Done()
		consumer, err := kafka.NewConsumer(consumerConfig)
		if err != nil {
			log.Fatal(err)
		}
		defer consumer.Close()

		err = consumer.SubscribeTopics([]string{topic}, nil)
		if err != nil {
			log.Fatal(err)
		}

		signals := make(chan os.Signal, 1)
		signal.Notify(signals, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)

		for {
			select {
			case sig := <-signals:
				fmt.Println("get signal ", sig)
				return
			default:
				//msg, err := consumer.ReadMessage(-1)
				msg, err := consumer.ReadMessage(5 * time.Second)
				if err == nil {
					fmt.Printf("Consumed message: Topic=%s, Partition=%d, Offset=%d, Key=%s, Value=%s\n",
						*msg.TopicPartition.Topic, msg.TopicPartition.Partition, msg.TopicPartition.Offset,
						string(msg.Key), string(msg.Value))
				} else {
					fmt.Println("Consumer error:", err)
				}
			}
		}
	}()

	// 等待协程执行完毕
	wg.Wait()
}
