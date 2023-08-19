package main

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/google/uuid"
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func main() {
	main1()
}

// transaction
func main2() {
	addr := "192.168.56.11:9092"
	// Kafka 服务配置
	config := &kafka.ConfigMap{
		"bootstrap.servers": addr,
		//"group.id":           "my-consumer-group",
		"enable.idempotence": true,
		"transactional.id":   "my-transaction-id",
	}

	producer, err := kafka.NewProducer(config)
	if err != nil {
		log.Fatalf("Failed to create Kafka producer: %s", err)
	}
	defer producer.Close()

	// 等待 Kafka 生产者初始化完成
	producerPollTimeout := 5 * time.Second
	for e := range producer.Events() {
		switch event := e.(type) {
		case kafka.AssignedPartitions, kafka.RevokedPartitions:
			// 忽略重新分配和撤销分配的事件
		case *kafka.Error:
			log.Printf("Kafka error: %v\n", event)
		default:
			log.Printf("Ignored event: %s\n", event)
		}

		if producer.Flush(int(producerPollTimeout/time.Millisecond)) == int(kafka.ErrTimedOut) {
			log.Fatal("Failed to initialize Kafka producer")
		} else {
			break // 生产者已初始化完成，退出循环
		}
	}

	// 开始事务
	err = producer.BeginTransaction()
	if err != nil {
		log.Fatalf("Failed to begin transaction: %s", err)
	}

	// 向主题发送消息
	topic := "my-topic"
	message := &kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          []byte("Hello, Kafka!"),
	}

	err = producer.Produce(message, nil)
	if err != nil {
		log.Fatalf("Failed to produce message: %s", err)
	}

	// 提交事务
	err = producer.CommitTransaction(context.Background())
	if err != nil {
		log.Fatalf("Failed to commit transaction: %s", err)
	}

	fmt.Println("Message sent and transaction committed successfully!")
}

type Goods struct {
	ID    string
	Name  string
	Price float64
}

// product consumer
func main1() {
	defer func() {
		res := recover()
		if res != nil {
			fmt.Println(res)
		}
	}()
	// 配置Kafka生产者和消费者共享的参数
	addr := "192.168.56.11:9092"
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

	uuidX, _ := uuid.NewUUID()

	goods := Goods{ID: uuidX.String(), Name: time.Now().Format(time.RFC3339), Price: 11.22}
	marshal, err := json.Marshal(goods)
	if err != nil {
		return
	}
	// 生产者协程
	go func() {
		defer wg.Done()
		deliveryChan := make(chan kafka.Event)
		for i := 0; i < 100; i++ {
			message := &kafka.Message{
				TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
				Value:          marshal,
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
