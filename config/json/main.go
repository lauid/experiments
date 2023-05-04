package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"strconv"
)

type Config struct {
	DatabaseUrl string `json:"database_url"`
	Port        int    `json:"port"`
}

func main() {
	data, err := ioutil.ReadFile("config.json")
	if err != nil {
		panic(err)
	}
	var jsonData Config
	err = json.Unmarshal(data, &jsonData)
	if err != nil {
		panic(err)
	}
	fmt.Println(jsonData)

	for k, v := range map[string]string{"TERM": ""} {
		fmt.Println(k, "==>", getEnv(k, v))
	}
}

func getEnv(key string, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}

	return fallback
}

func getEnvInt(key string, fallback int) int {
	if value, ok := os.LookupEnv(key); ok {
		i, err := strconv.Atoi(value)
		if err != nil {
			return fallback
		}

		return i
	}

	return fallback
}
