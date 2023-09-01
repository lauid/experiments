package main

import (
	"experiment/cli"
	"fmt"
	"github.com/spf13/cobra"
	"os"
)

func main() {
	rootCmd := &cobra.Command{
		Use: "experiment-cli",
		PersistentPreRun: func(cmd *cobra.Command, args []string) {
			fmt.Println("persistentPreRun..")
		},
	}

	demoCmd := cli.NewDemoCmd()
	redisCmd := cli.NewRedisCmd()

	rootCmd.AddCommand(demoCmd)
	rootCmd.AddCommand(redisCmd)

	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}
