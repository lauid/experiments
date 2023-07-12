package routes

import (
	"experiment/gin/handlers"
	"github.com/gin-gonic/gin"
)

func RegisterRoutes(router *gin.Engine) {
	userHandler := handlers.NewUserHandler()

	// 用户相关路由
	userRoutes := router.Group("/users")
	{
		userRoutes.GET("/:id", userHandler.GetUser)
		userRoutes.POST("/", userHandler.CreateUser)
	}
	// 其他路由...
}
