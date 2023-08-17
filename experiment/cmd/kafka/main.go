package main

import (
	"fmt"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"sync"
)

func main() {
	producerConfig := kafka.ConfigMap{"bootstrap.servers": "localhost:9092"}
	consumerConfig := kafka.ConfigMap{
		"bootstrap.servers": "localhost:9092",
		"group.id":          "my-consumer-group",
		"auto.offset.reset": "earliest",
	}

	producer, err := kafka.NewProducer(&producerConfig)
	if err != nil {
		panic(err)
	}
	defer producer.Close()

	consumer, err := kafka.NewConsumer(&consumerConfig)
	if err != nil {
		panic(err)
	}
	defer consumer.Close()

	dataChan := make(chan []byte)

	wg := sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		var topic string
		for {
			select {
			case data := <-dataChan:
				err := producer.Produce(&kafka.Message{
					TopicPartition: kafka.TopicPartition{
						Topic:     &topic,
						Partition: kafka.PartitionAny,
					},
					Key:   []byte("key"),
					Value: data,
					Headers: []kafka.Header{
						{
							Key:   "headerKey",
							Value: []byte("headerValue"),
						},
					},
				}, nil)
				if err != nil {
					fmt.Printf("Failed to produce message: %v\n", err)
				}
			}

		}
	}()

	wg.Add(1)
	go func() {
		defer wg.Done()
		err := consumer.SubscribeTopics([]string{"my-topic"}, nil)
		if err != nil {
			fmt.Printf("subscribe error:%v", err)
			return
		}
		for {
			message, err := consumer.ReadMessage(-1)
			if err != nil {
				fmt.Printf("received message:%s\n", string(message.Value))
			} else {
				fmt.Printf("error: %v\n", err.Error())
			}
		}
	}()

	wg.Wait()
}

//func main() {
//	producerConfig := kafka.ConfigMap{"bootstrap.servers": "localhost:9092"}
//	consumerConfig := kafka.ConfigMap{
//		"bootstrap.servers":  "localhost:9092",
//		"group.id":           "my-consumer-group",
//		"auto.offset.reset":  "earliest",
//	}
//
//	// 创建生产者和消费者
//	producer, err := kafka.NewProducer(&producerConfig)
//	if err != nil {
//		panic(err)
//	}
//	consumer, err := kafka.NewConsumer(&consumerConfig)
//	if err != nil {
//		panic(err)
//	}
//
//	// 创建通道用于协程间的通信
//	dataChan := make(chan []byte)
//	wg := sync.WaitGroup{}
//
//	// 协程：写入数据到Kafka
//	wg.Add(1)
//	go func() {
//		defer wg.Done()
//		for {
//			select {
//			case data := <-dataChan:
//				// 生产者发送消息
//				err := producer.Produce(&kafka.Message{
//					TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
//					Key:            []byte("key"),
//					Value:          data,
//					Headers:        []kafka.Header{{Key: "headerKey", Value: []byte("headerValue")}},
//				}, nil)
//				if err != nil {
//					fmt.Printf("Failed to produce message: %v\n", err)
//				}
//			}
//		}
//	}()
//
//	// 协程：顺序消费Kafka消息
//	wg.Add(1)
//	go func() {
//		defer wg.Done()
//		consumer.SubscribeTopics([]string{"my-topic"}, nil)
//		for {
//			msg, err := consumer.ReadMessage(-1)
//			if err == nil {
//				fmt.Printf("Received message: %s\n", string(msg.Value))
//				// 处理消费信息的逻辑
//			} else {
//				fmt.Printf("Error: %v\n", err.Error())
//			}
//		}
//	}()
//
//	wg.Wait()
//
//	producer.Close()
//	consumer.Close()
//}
