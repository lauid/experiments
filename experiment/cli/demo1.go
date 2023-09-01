package cli

import (
	"fmt"
	"github.com/spf13/cobra"
)

var (
	demoCmd = []cobra.Command{
		{
			Use:   "myapp",
			Short: "MyApp is a command line tool",
			Long:  "MyApp is a command line tool that demonstrates the usage of Cobra.",
			Run: func(cmd *cobra.Command, args []string) {
				// 主命令的执行逻辑
				fmt.Println("Hello from MyApp!")
			},
		},
		{
			Use:   "subcommand",
			Short: "A subcommand",
			Long:  "A subcommand of myapp",
			Run: func(cmd *cobra.Command, args []string) {
				// 子命令的执行逻辑
				fmt.Println("Running subcommand...")
			},
		},
	}
)

func NewDemoCmd() *cobra.Command{
	cmd := cobra.Command{
		Use:   "users [create | get | update | token | password | enable | disable]",
		Short: "Users management",
		Long:  `Users management: create accounts and tokens"`,
	}

	for i := range demoCmd {
		cmd.AddCommand(&demoCmd[i])
	}

	return &cmd
}
