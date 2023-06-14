package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
	"sync"
	"time"
)

type User struct {
	Name string `json:"name"`
	Age  int    `json:"age"`
}

func main() {
	router := gin.Default()

	// 全局中间件
	router.Use(func(c *gin.Context) {
		fmt.Println("before request")
		c.Next()
		fmt.Println("after request")
	})

	// 路由中间件
	router.GET("/hello", func(c *gin.Context) {
		fmt.Println("before hello")
		c.String(http.StatusOK, "Hello")
		fmt.Println("after hello")
	})

	router.GET("/hello/:name", func(c *gin.Context) {
		var lock sync.Mutex
		lock.Lock()
		defer lock.Unlock()
		time.Sleep(time.Second*2)
		name := c.Param("name")
		user := User{
			Name: "hello, " + name,
			Age:  30,
		}
		c.JSON(http.StatusOK, user)
	})

	router.Run(":8080")
}

