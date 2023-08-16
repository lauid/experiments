package main

import (
	"context"
	"fmt"
	"github.com/go-redis/redis/v8"
	"log"
	"strconv"
	"strings"
	"time"
)

func main() {
	rdc := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "",
		DB:       0,
	})
	defer func(rdc *redis.Client) {
		err := rdc.Close()
		if err != nil {
			log.Println("redis close err:", err)
		}
	}(rdc)
	streamName := "mystream"

	// 添加消息到 Stream，并自定义消息的 ID
	go func() {
		counter := 0
		ticker := time.NewTicker(100 * time.Millisecond)
		defer ticker.Stop()
		for range ticker.C {
			counter += 1
			streamAddWithCustomID(rdc, streamName, "custom-message-id", "Hello, World!"+strconv.Itoa(counter))
		}
	}()

	go func() {
		//streamRead(rdc, "mystream", "0")
		consumerGroup(rdc, streamName)
	}()

	//go func() {
	//	//streamRead(rdc, "mystream", "0")
	//	streamReader(rdc, streamName, "0")
	//}()

	select {}
}

func streamReader(rdc *redis.Client, streamName, startId string) error {
	// 创建一个流式阅读器
	streams := make(map[string]string) // 用于记录各个 Stream 的偏移量
	// 为每个 Stream 设置起始位置
	streams[streamName] = startId
	for {

		// 读取 Stream 数据
		result, err := rdc.XRead(context.Background(), &redis.XReadArgs{
			Streams: []string{streamName, streams[streamName]},
			Block:   0, // 阻塞时间，0 表示一直等待新数据到达
		}).Result()
		if err != nil {
			log.Println("Error reading stream:", err)
			break
		}

		// 处理返回的 Stream 数据
		for _, stream := range result {
			for _, message := range stream.Messages {
				//log.Println("Stream:", stream.Stream)
				//log.Println("ID:", message.ID)
				//log.Println("Fields:", message.Values)
				log.Println(message.ID, message.Values)
				time.Sleep(time.Second * 3)
				_, err2 := rdc.XDel(context.Background(), streamName, message.ID).Result()
				if err2 != nil {
					log.Println("message del err:", err2)
				}

				// 更新偏移量
				streams[stream.Stream] = message.ID
			}
		}
	}

	return nil
}

func streamRead(rdc *redis.Client, streamName, startId string) error {
	streams, err := rdc.XRead(context.Background(), &redis.XReadArgs{
		Streams: []string{streamName, startId},
		Count:   10,
		Block:   time.Second * 0,
	}).Result()
	if err != nil {
		return err
	}

	for _, stream := range streams {
		for _, message := range stream.Messages {
			log.Println(message.ID, message.Values)
			_, err2 := rdc.XDel(context.Background(), streamName, message.ID).Result()
			if err2 != nil {
				log.Println("message del err:", err2)
			}
		}
	}

	return nil
}

func streamAddWithCustomID(rdc *redis.Client, streamName, id, message string) error {
	_, err := rdc.XAdd(
		context.Background(),
		&redis.XAddArgs{
			Stream: streamName,
			//ID:     id,
			Values: map[string]interface{}{
				"message": message,
			},
		}).Result()

	if err != nil {
		return err
	}
	//log.Println("Message added to stream with custom ID:", res)

	return nil
}

func consumerGroup(client *redis.Client, streamName string) {
	// 创建消费者组并订阅消息
	consumerGroupName := "myConsumerGroup"
	consumerName := "myConsumer"

	_, err := client.XGroupCreateMkStream(context.Background(), streamName, consumerGroupName, "$").Result()
	if err != nil {
		if !strings.HasPrefix(err.Error(),"BUSYGROUP Consumer Group name already exists"){
			log.Fatal(err)
		}
	}

	for {
		// 从 Redis Stream 中读取新消息
		streams, err := client.XReadGroup(context.Background(), &redis.XReadGroupArgs{
			Group:    consumerGroupName,
			Consumer: consumerName,
			Streams:  []string{streamName, ">"},
			Count:    10,               // 一次读取的消息数量
			Block:    time.Second * 10, // 阻塞时间，等待新消息
		}).Result()
		if err != nil {
			log.Println(err)
			continue
		}
		fmt.Println(len(streams))

		for _, stream := range streams {
			streamName := stream.Stream

			// 处理消息
			for _, message := range stream.Messages {
				messageID := message.ID
				messageValues := message.Values

				fmt.Printf("Received message %s: %v\n", messageID, messageValues)

				// 在这里执行你的业务逻辑
				fmt.Println("done..........")
				// ...

				// 确认消息已被消费
				_, err := client.XAck(context.Background(), streamName, consumerGroupName, messageID).Result()
				if err != nil {
					log.Println(err)
				}
			}
		}
	}
}
