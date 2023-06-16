package main

import (
	"context"
	"experiment"
	"fmt"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"os"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
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

	// config := cors.DefaultConfig()
	// config.AllowAllOrigins = true
	// router.Use(cors.New(config))
	router.Use(cors.Default())

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

	router.GET("/pod", func(c *gin.Context) {
		ns, name := podInfo()
		c.JSON(http.StatusOK,
			struct {
				name string
				ns   string
			}{
				name: name,
				ns:   ns,
			})
	})

	router.GET("/health", func(c *gin.Context) {
		healthFunc := experiment.Health("")
		healthFunc(c.Writer, c.Request)
	})

	router.GET("/hello/:name", func(c *gin.Context) {
		var lock sync.Mutex
		lock.Lock()
		defer lock.Unlock()
		time.Sleep(time.Second * 2)
		name := c.Param("name")
		user := User{
			Name: "hello, " + name,
			Age:  30,
		}
		c.JSON(http.StatusOK, user)
	})

	router.Run(":8080")
}

func podInfo() (string, string) {
	// 创建 Kubernetes 客户端
	config, err := rest.InClusterConfig()
	if err != nil {
		panic(err.Error())
	}
	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}
	// 获取当前 Pod 的标识
	pod, err := clientset.CoreV1().Pods("").Get(context.TODO(), os.Getenv("POD_NAME"), metav1.GetOptions{})
	if err != nil {
		panic(err.Error())
	}
	namespace := pod.ObjectMeta.Namespace
	name := pod.ObjectMeta.Name

	return namespace, name
}
