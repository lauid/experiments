package services

import (
	"experiment/gin/models"
	"experiment/gin/repositories"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type UserService struct {
	userRepository *repositories.UserRepository
}

func NewUserService() *UserService {
	return &UserService{
		userRepository: repositories.NewUserRepository(),
	}
}

func (s *UserService) GetUserInfo(userID uint, c *gin.Context) (*models.User, error) {
	tx := c.MustGet("tx").(*gorm.DB) // 从上下文中获取事务连接

	// 调用UserRepository获取用户信息
	user, err := s.userRepository.GetByID(userID,tx)
	if err != nil {
		return nil, err
	}

	// 执行其他业务逻辑操作...
	return user, nil
}

func (s *UserService) CreateUser(newUser *models.User, c *gin.Context) (*models.User, error) {
	tx := c.MustGet("tx").(*gorm.DB) // 从上下文中获取事务连接

	// 执行创建用户的业务逻辑
	_, err := s.userRepository.Create(newUser, tx)
	if err != nil {
		return nil, err
	}

	// 执行其他业务逻辑操作...

	return newUser, nil
}
