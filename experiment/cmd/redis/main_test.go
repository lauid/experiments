package main

import (
	"context"
	"fmt"
	"github.com/go-redis/redis/v8"
	"github.com/google/uuid"
	"log"
	"testing"
	"time"
)

func keepExpireTime(rdc *redis.Client, lockKey string, expiration time.Duration, exitChan chan struct{}) {
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			err := rdc.Expire(context.Background(), lockKey, expiration).Err()
			if err != nil {
				fmt.Printf("续期失败：%v\n", err)
				return
			}

			// 这里可以添加其他的处理逻辑，表示续期成功后执行的操作
			fmt.Println("续期成功")
		case <-exitChan:
			return
		}
	}
}

func redisLock(rdc *redis.Client) {
	exitChan := make(chan struct{})
	defer close(exitChan)
	ctx := context.Background()
	lockKey := "myLock"
	uuidV, _ := uuid.NewUUID()
	lockValue := uuidV.String()
	expire := 10 * time.Second
	_, err := rdc.Set(ctx, lockKey, lockValue, expire).Result()
	if err != nil {
		log.Fatalln("lock fail ", err)
	} else {
		// 成功获取到锁，执行业务逻辑
		log.Println("get lock success.")
		go keepExpireTime(rdc, lockKey, expire, exitChan)

		//time.Sleep(1 * time.Second)

		//释放锁
		// 定义 Lua 脚本
		luaScript := `
		if redis.call('GET', KEYS[1]) == ARGV[1] then
			redis.call('DEL', KEYS[1])
			return 1
		else
			return 0
		end
	`
		// 执行 Lua 脚本
		result, err := rdc.Do(ctx, "EVAL", luaScript, 1, lockKey, lockValue).Result()
		if err != nil {
			fmt.Println("执行 Lua 脚本出错:", err)
			return
		}

		// 处理执行结果
		if reply, ok := result.(int64); ok {
			if reply == 1 {
				fmt.Println("键删除成功")
			} else {
				fmt.Println("键不存在或值不匹配")
			}
		} else {
			fmt.Println("无效的返回值")
		}
	}
}

func BenchmarkRedisLock(b *testing.B) {
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

	for i := 0; i < b.N; i++ {
		redisLock(rdc)
	}
}
