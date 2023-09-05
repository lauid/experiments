package main

import (
	"fmt"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"math/rand"
	"strconv"
	"time"
)

type Product struct {
	gorm.Model
	Code  string
	Price uint
}

type User struct {
	ID        uint `gorm:"primarykey"`
	Name      string
	Age       uint
	CreatedAt time.Time // 在创建时，如果该字段值为零值，则使用当前时间填充
	UpdatedAt int       // 在创建时该字段值为零值或者在更新时，使用当前时间戳秒数填充
	//Updated   int64 `gorm:"autoUpdateTime:nano"` // 使用时间戳纳秒数填充更新时间
	Updated int64 `gorm:"autoUpdateTime:milli"` // 使用时间戳毫秒数填充更新时间
	Created int64 `gorm:"autoCreateTime"`       // 使用时间戳秒数填充创建时间
}

var db *gorm.DB

func init() {
	var err error
	db, err = gorm.Open(sqlite.Open("test.db"), &gorm.Config{})
	if err != nil {
		panic("failed to connect database")
	}
}

func main() {
	//main1()
	db.AutoMigrate(&User{})
	user1 := User{Name: "小马" + strconv.Itoa(rand.Intn(100)), Age: uint(100 - rand.Intn(100))}
	db.Create(&user1)
	fmt.Printf("after create: %+v\n", user1)

	var user2 User
	db.Find(&user2, "age = ? AND Name= ?", 47, "小马41")
	fmt.Printf("after find: %+v\n", user2)
}

func main1() {

	// 迁移 schema
	db.AutoMigrate(&Product{})

	// Create
	db.Create(&Product{Code: "D42", Price: 100})

	// Read
	var product Product
	db.First(&product, 1) // 根据整型主键查找
	fmt.Printf("%+v\n", product)

	db.First(&product, "code = ?", "D42") // 查找 code 字段值为 D42 的记录
	fmt.Printf("%+v\n", product)

	// Update - 将 product 的 price 更新为 200
	db.Model(&product).Update("Price", 200)
	fmt.Printf("%+v\n", product)

	// Update - 更新多个字段
	db.Model(&product).Updates(Product{Price: 200, Code: "F42"}) // 仅更新非零值字段
	db.Model(&product).Updates(map[string]interface{}{"Price": 200, "Code": "F42"})
	fmt.Printf("%+v\n", product)

	// Delete - 删除 product
	db.Delete(&product, 1)
	fmt.Printf("%+v\n", product)
}
