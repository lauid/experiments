package cli

import (
	"context"
	"fmt"
	"github.com/go-redis/redis/v8"
	"github.com/spf13/cobra"
)

var redisClient *redis.Client

var (
	redisCmd = []cobra.Command{
		{
			Use:   "set",
			Short: "Set a key-value pair in Redis",
			Args:  cobra.ExactArgs(2),
			Run: func(cmd *cobra.Command, args []string) {
				// "set"命令的执行逻辑
				key := args[0]
				value := args[1]

				err := redisClient.Set(context.Background(), key, value, 0).Err()
				if err != nil {
					fmt.Printf("Failed to set key-value pair: %s\n", err)
					return
				}

				fmt.Println("Key-value pair set successfully!")
			},
		},
		{
			Use:   "get",
			Short: "Get the value for a key from Redis",
			Args:  cobra.ExactArgs(1),
			Run: func(cmd *cobra.Command, args []string) {
				// "get"命令的执行逻辑
				key := args[0]

				value, err := redisClient.Get(context.Background(), key).Result()
				if err != nil {
					fmt.Printf("Failed to get value for key: %s\n", err)
					return
				}

				fmt.Printf("Value for key %s: %s\n", key, value)
			},
		},
	}
)

func NewRedisCmd() *cobra.Command {
	redisClient = redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "", // 如果有密码，请在此处设置密码
		DB:       0,  // 默认使用的数据库
	})

	cmd := cobra.Command{
		Use:   "redis-cli",
		Short: "A simple Redis command line interface",
		Long:  `redis cli management`,
		//Run: func(cmd *cobra.Command, args []string) {
		//	// 主命令的执行逻辑
		//	cmd.Help()
		//},
	}

	for i := range redisCmd {
		cmd.AddCommand(&redisCmd[i])
	}

	return &cmd
}
