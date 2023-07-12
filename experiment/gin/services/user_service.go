package services

import (
	"experiment/gin/models"
	"experiment/gin/repositories"
)

type UserService struct {
	userRepository *repositories.UserRepository
}

func NewUserService() *UserService {
	return &UserService{
		userRepository: repositories.NewUserRepository(),
	}
}

func (s *UserService) GetUserInfo(userID string) (*models.User, error) {
	// 调用UserRepository获取用户信息
	user, err := s.userRepository.GetByID(userID)
	if err != nil {
		return nil, err
	}

	// 执行其他业务逻辑操作...

	return user, nil
}

func (s *UserService) CreateUser(newUser models.User) (*models.User, error) {
	// 执行创建用户的业务逻辑
	user, err := s.userRepository.Create(newUser)
	if err != nil {
		return nil, err
	}

	// 执行其他业务逻辑操作...

	return user, nil
}
