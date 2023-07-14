package main

import (
	"context"
	"experiment/gin/middlewares"
	svc "experiment/internal/server"
	groute "experiment/gin/routes"
	"experiment/jaeger"
	//_ "experiment/cmd/gin/docs" // 导入自动生成的 Swagger 代码
	_ "experiment/docs" // 导入自动生成的 Swagger 代码
	"experiment/gin/metrics"
	"fmt"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func registerMiddlewares(router *gin.Engine) {
	router.Use(gin.LoggerWithFormatter(middlewares.GetLoggerMiddle()))
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
}

// @title           Swagger Example API
// @version         1.0
// @description     This is a sample server celler server.
// @termsOfService  http://swagger.io/terms/

// @contact.name   API Support
// @contact.url    http://www.swagger.io/support
// @contact.email  support@swagger.io

// @license.name  Apache 2.0
// @license.url   http://www.apache.org/licenses/LICENSE-2.0.html

// @host      localhost:8080
// @BasePath  /api/v1

// @securityDefinitions.basic  BasicAuth

// @externalDocs.description  OpenAPI
// @externalDocs.url          https://swagger.io/resources/open-api/
func main() {
	defer func() {
		fmt.Println("exit...............")
	}()
	// 设置 Gin 路由
	router := gin.Default()

	registerMiddlewares(router)

	// 初始化 Jaeger
	tracer, closer := jaeger.InitJaeger("Gin")
	defer closer.Close()
	// 添加 Jaeger 中间件
	router.Use(jaeger.GetJaegerTraceMiddleware(tracer))

	// 打开数据库连接
	svc.NewMysqlConnect()
	defer svc.CloseDB()
	router.Use(svc.WithDB())
	svc.AutoMigrate()

	groute.RegisterRoutes(router)

	// 创建一个带有超时的上下文
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// 创建一个用于接收终止信号的通道
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM) // 监听 SIGINT（Ctrl+C）和 SIGTERM（kill 命令）

	// 启动服务器（非阻塞）
	server := &http.Server{
		Addr:    ":8080",
		Handler: router,
	}

	go func() {
		// 监听终止信号
		<-quit

		// 收到终止信号后开始关闭服务器
		log.Println("Server is shutting down...")

		// 设置超时时间，等待尚未完成的请求处理完毕
		gracefulCtx, cancelShutdown := context.WithTimeout(ctx, 10*time.Second)
		defer cancelShutdown()

		// 关闭服务器并等待已有的连接处理完毕
		if err := server.Shutdown(gracefulCtx); err != nil {
			// 处理一些错误日志
			log.Fatalf("Server forced to shutdown: %v", err)
		}

		// 执行清理操作
		// ...

		log.Println("Server has been shutdown.")
	}()

	// 启动服务器
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		// 处理一些错误日志
		log.Fatalf("Server startup failed: %v", err)
	}

	// 服务器已关闭
	log.Println("Server exiting...")
}