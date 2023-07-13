package handlers

import (
	"experiment/gin/models"
	"experiment/gin/services"
	"github.com/gin-gonic/gin"
	"net/http"
)

type UserHandler struct {
	userService *services.UserService
}

func NewUserHandler() *UserHandler {
	return &UserHandler{
		userService: services.NewUserService(),
	}
}

// @Summary 根据ID获取用户信息
// @Description 根据用户ID，获取用户的详细信息
// @Tags 用户管理
// @Accept json
// @Produce json
// @Param id path int true "用户ID"
// @Router /users/{id} [get]
func (h *UserHandler) getUserByID(c *gin.Context) {
	// 实际处理逻辑
}
func (h *UserHandler) GetUser(c *gin.Context) {
	// 从URL参数中获取用户ID
	userID := c.Param("id")

	// 调用UserService获取用户信息
	user, err := h.userService.GetUserInfo(userID)
	if err != nil {
		// 错误处理
		return
	}

	// 返回用户信息
	c.JSON(http.StatusOK, user)
}

func (h *UserHandler) CreateUser(c *gin.Context) {
	// 解析请求体数据
	var newUser models.User
	if err := c.ShouldBindJSON(&newUser); err != nil {
		// 错误处理
		return
	}

	// 调用UserService创建用户
	user, err := h.userService.CreateUser(newUser)
	if err != nil {
		// 错误处理
		return
	}

	// 返回创建的用户信息
	c.JSON(http.StatusCreated, user)
}
