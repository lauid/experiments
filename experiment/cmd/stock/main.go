package main

import (
	"context"
	"fmt"
	"log"

	"github.com/go-redis/redis/v8"
)

func main() {
	ctx := context.Background()

	// Connect to Redis
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "", // No password
		DB:       0,  // Default DB
	})

	defer client.Close()

	// Item ID
	goodsID := "goodsId_1"

	// Lua script to decrement stock
	luaScript := `
		local currentStock = redis.call('HGET', KEYS[1], 'stock')
		local quantitySold = tonumber(ARGV[1])
		if tonumber(currentStock) >= quantitySold then
			redis.call('HINCRBY', KEYS[1], 'stock', -quantitySold)
			redis.call('HINCRBY', KEYS[1], 'sold', quantitySold)
			return true
		else
			return false
		end
	`

	// Execute Lua script
	result, err := client.Eval(ctx, luaScript, []string{goodsID}, 2).Result()
	if err != nil {
		log.Fatal(err)
	}

	if result == int64(1) {
		fmt.Println("Stock deducted successfully.")
	} else {
		fmt.Println("Insufficient stock. Deduction failed.")
	}

	// Check updated values
	updatedValues, err := client.HGetAll(ctx, goodsID).Result()
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("Updated values:", updatedValues)
}
