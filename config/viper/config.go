package config

import (
	"fmt"
	"github.com/spf13/viper"
)

type Configuration struct {
	App          AppConfiguration
	Database     DatabaseConfiguration
	EXAMPLE_PATH string
	EXAMPLE_VAR  string
}

type AppConfiguration struct {
	Name    string
	Version string
}

type DatabaseConfiguration struct {
	Host     string
	Port     string
	Username string
	Password string
}

func readConfig() {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		panic(err)
	}

	var configuration Configuration
	if err := viper.Unmarshal(&configuration); err != nil {
		panic(err)
	}
	fmt.Println(configuration)

	viper.SetDefault("EXAMPLE_VAR1", "var1")
	v1 := viper.Get("EXAMPLE_VAR")
	fmt.Println(v1)
	v2 := viper.GetString("EXAMPLE_VAR1")
	fmt.Println(v2)
}
