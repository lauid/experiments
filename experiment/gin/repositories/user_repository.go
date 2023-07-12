package repositories

import (
	"experiment/gin/models"
	"k8s.io/apimachinery/pkg/util/rand"
	"strconv"
)

type UserRepository struct {
	// 数据库连接等相关信息
}

func NewUserRepository() *UserRepository {
	return &UserRepository{
		// 初始化数据库连接等信息
	}
}

func (r *UserRepository) GetByID(userID string) (*models.User, error) {
	// 执行数据库查询等操作，返回用户信息
	return &models.User{
		ID:   userID,
		Name: "Name" + userID,
	}, nil
}

func (r *UserRepository) Create(newUser models.User) (*models.User, error) {
	// 执行创建用户的数据库插入操作，返回新创建的用户信息
	idStr := strconv.Itoa(rand.IntnRange(10, 20))
	return &models.User{
		ID:   idStr,
		Name: "Name",
	}, nil
}
