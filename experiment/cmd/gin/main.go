package main

import (
	"context"
	"experiment"
	"experiment/gin/metrics"
	groute "experiment/gin/routes"
	"experiment/jaeger"
	"fmt"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"log"
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

func getLoggerMiddle() func(param gin.LogFormatterParams) string {
	return func(param gin.LogFormatterParams) string {
		// 你的自定义格式
		return fmt.Sprintf("%s - [%s] \"%s %s %s %d %s \"%s\" %s\"\n",
			param.ClientIP,
			param.TimeStamp.Format(time.RFC3339),
			param.Method,
			param.Path,
			param.Request.Proto,
			param.StatusCode,
			param.Latency,
			param.Request.UserAgent(),
			param.ErrorMessage,
		)
	}
}

func main() {
	// 初始化 Jaeger
	tracer, closer := jaeger.InitJaeger("Gin")
	defer closer.Close()

	// 设置 Gin 路由
	router := gin.Default()

	// 添加 Jaeger 中间件
	router.Use(jaeger.GetJaegerTraceMiddleware(tracer))
	router.Use(gin.LoggerWithFormatter(getLoggerMiddle()))
	router.Use(gin.Recovery())
	// config := cors.DefaultConfig()
	// config.AllowAllOrigins = true
	// router.Use(cors.New(config))

	router.Use(cors.Default())
	router.Use(metrics.GetPrometheusCounterMiddleware())
	router.Use(metrics.GetPrometheusLatencyMiddleware())

	// 全局中间件
	router.Use(func(c *gin.Context) {
		fmt.Println("before request")
		c.Next()
		fmt.Println("after request")
	})
	groute.RegisterRoutes(router)

	router.GET("/metrics", gin.WrapH(metrics.GetPrometheusHttpHandler()))

	// 路由中间件
	router.GET("/hello", func(c *gin.Context) {
		//span, _ := opentracing.StartSpanFromContext(c.Request.Context(), "hello-HandlerName")
		//defer span.Finish()

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

	router.GET("/pod2", func(c *gin.Context) {
		obj := []string{
			os.Getenv("MY_POD_NAMESPACE"),
			os.Getenv("MY_POD_NAME"),
		}
		c.JSON(http.StatusOK, obj)
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

	router.GET("/async", func(c *gin.Context) {
		cp := c.Copy()
		go func() {
			time.Sleep(2 * time.Second)
			log.Println("----------------------------", cp.Request.URL.Path)
		}()
		c.JSON(http.StatusOK, "hello,async.")
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
	pod, err := clientset.CoreV1().Pods("").Get(context.TODO(), os.Getenv("MY_POD_NAME"), metav1.GetOptions{})
	if err != nil {
		panic(err.Error())
	}
	namespace := pod.ObjectMeta.Namespace
	name := pod.ObjectMeta.Name

	return namespace, name
}
