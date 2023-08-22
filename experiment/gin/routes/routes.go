package routes

import (
	"context"
	"experiment"
	"experiment/gin/handlers"
	"experiment/gin/metrics"
	"experiment/gin/middlewares"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/swaggo/files"
	"github.com/swaggo/gin-swagger"
	"html/template"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"log"
	"net/http"
	"os"
	"sync"
	"time"
)

type User struct {
	Name string `json:"name"`
	Age  int    `json:"age"`
}

func RegisterRoutes(router *gin.Engine) {
	// 设置WebSocket路由
	router.GET("/websocket", handlers.HandleWebSocket)

	// 用户登录路由
	router.POST("/login", handlers.LoginHandler)

	// 使用鉴权中间件保护受保护的接口
	router.GET("/protected", middlewares.AuthMiddleware(), handlers.ProtectedHandler)

	// 用户相关路由
	userRoutes := router.Group("/users")
	{
		userHandler := handlers.NewUserHandler()
		userRoutes.GET("/:id", userHandler.GetUser)
		userRoutes.POST("/", userHandler.CreateUser)
	}
	// 其他路由...
	routers(router)

	// 设置Swagger中间件
	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	routeWeb(router)
}

func routeWeb(router *gin.Engine) {
	//
	web := router.Group("/web")
	{
		contactHandle := handlers.ContactHandle{}
		web.GET("/contact", contactHandle.Contact)
	}

	//
	var html = template.Must(template.New("https").Parse(`
<html>
<head>
  <title>Https Test</title>
  <script src="/assets/app.js"></script>
</head>
<body>
  <h1 style="color:red;">Welcome, Ginner!</h1>
</body>
</html>
`))
	router.SetHTMLTemplate(html)
	router.GET("/push", func(c *gin.Context) {
		if pusher := c.Writer.Pusher(); pusher != nil {
			// 使用 pusher.Push() 做服务器推送
			//if err := pusher.Push("/assets/app.js", nil); err != nil {
			//	log.Printf("Failed to push: %v", err)
			//}
		}
		c.HTML(200, "https", gin.H{
			"status": "success",
		})
	})
}

func routers(router *gin.Engine) {
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
