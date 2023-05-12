package main

import (
	"fmt"
	"github.com/go-redis/redis"
	redisPool "github.com/gomodule/redigo/redis"
	"log"
	"time"
)

var pool *redisPool.Pool

func init() {
	// 创建一个连接池
	pool = &redisPool.Pool{
		MaxIdle:     2,                 // 最大空闲连接数
		MaxActive:   3,                 // 最大活动连接数
		IdleTimeout: 120 * time.Second, // 空闲连接超时时间
		Dial: func() (redisPool.Conn, error) { // 创建连接的函数
			c, err := redisPool.Dial("tcp", "localhost:6379")
			if err != nil {
				return nil, err
			}
			return c, nil
		},
	}
	fmt.Println("-------------------init pool.")
}

func main() {
	cacheKey := "test_a"
	conn := pool.Get()
	defer conn.Close()
	res, err := redisPool.String(conn.Do("Get", cacheKey))
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("Cache get: ", res)
	return

	select {}
}

func timer() {
	checkTimer := time.NewTicker(time.Second * 2)

	for range checkTimer.C {
		poolFunc()
	}
}

func redisFunc() {
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "", // no password set
		DB:       0,  // use default DB
	})

	err := client.Set("key", "value", 0).Err()
	if err != nil {
		panic(err)
	}

	val, err := client.Get("key").Result()
	if err != nil {
		panic(err)
	}
	fmt.Println("key", val)
}

func poolFunc1() {

	// 从连接池中获取一个连接
	conn := pool.Get()
	defer conn.Close()

	// 执行Redis命令
	_, err := conn.Do("SET", "key", "value")
	if err != nil {
		log.Fatal(err)
	}

	// 从连接池中获取另一个连接
	conn2 := pool.Get()
	defer conn2.Close()

	// 执行Redis命令
	value, err := redisPool.String(conn2.Do("GET", "key"))
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println(value)
}

func poolFunc() {

	// 从连接池中获取一个连接
	conn := pool.Get()
	defer conn.Close()

	// 执行Redis命令
	_, err := conn.Do("SET", "key", "value")
	if err != nil {
		log.Fatal(err)
	}

	// 执行Redis命令
	value, err := redisPool.String(conn.Do("GET", "key"))
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println(value)
}
