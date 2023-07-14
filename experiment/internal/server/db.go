package server

import (
	"database/sql"
	"experiment/gin/models"
	"github.com/gin-gonic/gin"
	"github.com/opentracing/opentracing-go"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"log"
	"os"
	"time"
)

// 定义全局变量来持有数据库连接
var db *gorm.DB

// NewMysqlConnect 连接数据库
func NewMysqlConnect(){
	//dsn := "user:password@tcp(database-host:port)/database"
	//dsn := "root:@tcp(localhost:3306)/database?charset=utf8mb4&parseTime=True&loc=Local"

	newLogger := logger.New(
		log.New(os.Stdout, "\r\n", log.LstdFlags), // io writer
		logger.Config{
			SlowThreshold:              time.Second,   // Slow SQL threshold
			//LogLevel:                   logger.Silent, // Log level
			LogLevel:                   logger.Info, // Log level
			IgnoreRecordNotFoundError: true,           // Ignore ErrRecordNotFound error for logger
			ParameterizedQueries:      false,           // Don't include params in the SQL log
			Colorful:                  false,          // Disable color
		},
	)
	// 设置数据库连接参数
	dsn := "root:@tcp(localhost:3306)/test?charset=utf8mb4&parseTime=True&loc=Local"
	var err error
	db, err = gorm.Open(mysql.Open(dsn), &gorm.Config{Logger: newLogger})
	if err != nil {
		log.Fatalf("无法连接到数据库：%v", err)
	}
}

// CloseDB 连接数据库
func CloseDB() {
	// 关闭数据库连接
	dbSQL, _ := db.DB()
	defer func(dbSQL *sql.DB) {
		err := dbSQL.Close()
		if err != nil {
			log.Fatal(err)
		}
	}(dbSQL)
}

// WithDB 定义中间件来处理数据库连接和事务
func WithDB() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 开始一个数据库事务
		tx := db.Begin()

		span := opentracing.SpanFromContext(c.Request.Context())
		if span != nil {
			span.SetTag("span.kind", "client")
			span.SetTag("peer.service", "mysql")
			span.SetTag("db.type", "sql")
		}
		defer span.Finish()

		// 将事务连接附加到上下文中
		c.Set("tx", tx)

		c.Next()

		// 检查是否发生错误
		if c.IsAborted() {
			tx.Rollback()
			return
		}

		// 提交事务
		if err := tx.Commit().Error; err != nil {
			log.Printf("事务提交失败：%v", err)
			tx.Rollback()
		}
	}
}

func AutoMigrate()  {
	err := db.AutoMigrate(&models.User{})
	if err != nil {
		log.Fatal(err)
	}
}
