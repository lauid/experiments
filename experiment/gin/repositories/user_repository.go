package repositories

import (
	"experiment/gin/models"
	"fmt"
	"gorm.io/gorm"
)

type UserRepository struct {
	// 数据库连接等相关信息
}

func NewUserRepository() *UserRepository {
	return &UserRepository{
		// 初始化数据库连接等信息
	}
}

func (r *UserRepository) GetByID(userID uint, db *gorm.DB) (*models.User, error) {
	// 执行数据库查询等操作，返回用户信息
	var user models.User
	db.Find(&user,userID)
	fmt.Println(user)

	return &user, nil

	//return &models.User{
	//	ID:   userID,
	//	Name: "Name" + strconv.Itoa(int(userID)),
	//}, nil
}

func (r *UserRepository) Create(newUser *models.User, db *gorm.DB) (int64, error) {
	result := db.Create(newUser)
	return result.RowsAffected,nil
	// 执行创建用户的数据库插入操作，返回新创建的用户信息
	//idStr := rand.IntnRange(10, 20)
	//return &models.User{
	//	ID:   uint(idStr),
	//	Name: "Name",
	//}, nil
}
