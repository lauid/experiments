package main

import (
	"context"
	"experiment"
	"fmt"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/opentracing/opentracing-go"
	"io"
	"log"
	"os"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"net/http"
	"sync"
	"time"

	jaegercfg "github.com/uber/jaeger-client-go/config"
	"github.com/uber/jaeger-lib/metrics/prometheus"

	"github.com/uber/jaeger-client-go"
	jaegerlog "github.com/uber/jaeger-client-go/log"
)


type User struct {
	Name string `json:"name"`
	Age  int    `json:"age"`
}

func main() {
	router := gin.Default()

	router.Use(gin.LoggerWithFormatter(func(param gin.LogFormatterParams) string {

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
	}))
	router.Use(gin.Recovery())

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

func initJaeger(serviceName string) (opentracing.Tracer, io.Closer) {
	cfg := &jaegercfg.Configuration{
		Sampler: &jaegercfg.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
		Reporter: &jaegercfg.ReporterConfig{
			LogSpans:           true,
			LocalAgentHostPort: "localhost:6831", // 根据实际情况更改主机端口
		},
		ServiceName: serviceName,
	}

	jMetricsFactory := prometheus.New()

	tracer, closer, err := cfg.NewTracer(
		jaegercfg.Metrics(jMetricsFactory),
		jaegercfg.Logger(jaegerlog.StdLogger),
	)
	if err != nil {
		log.Fatal(err)
	}

	return tracer, closer
}
