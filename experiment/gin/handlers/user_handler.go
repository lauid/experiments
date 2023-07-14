// Package classification User API.

// The purpose of this service is to provide an application
// that is using plain go code to define an API

//      Host: localhost
//      Version: 0.0.1

// swagger:meta
package handlers

import (
	"experiment/gin/models"
	"experiment/gin/services"
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
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
	userID, _:= strconv.Atoi(c.Param("id"))

	// 调用UserService获取用户信息
	user, err := h.userService.GetUserInfo(uint(userID), c)
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
	fmt.Println(newUser)

	// 调用UserService创建用户
	user, err := h.userService.CreateUser(&newUser, c)
	if err != nil {
		// 错误处理
		return
	}

	// 返回创建的用户信息
	c.JSON(http.StatusCreated, user)
}

// ShowAccount godoc
// @Summary      Show an account
// @Description  get string by ID
// @Tags         accounts
// @Accept       json
// @Produce      json
// @Param        id   path      int  true  "Account ID"
// @Success      200  {object}  models.User
// @Router       /accounts/{id} [get]
func (c *UserHandler) ShowAccount(ctx *gin.Context) {
	id := ctx.Param("id")
	// 模拟一个错误
	if id == "0" {
		NewError(ctx, http.StatusBadRequest, fmt.Errorf("Invalid ID"))
		return
	}
	// 模拟正常返回数据
	account := map[string]interface{}{
		"id":   id,
		"name": "John Doe",
	}
	ctx.JSON(http.StatusOK, account)
}

func NewError(ctx *gin.Context, statusCode int, err error) {
	ctx.JSON(statusCode, gin.H{
		"error": err.Error(),
	})
}