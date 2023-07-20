package models

import "time"

type User struct {
	ID        uint      `gorm:"column:id;primary_key;type:INT UNSIGNED; not NULL; AUTO_INCREMENT;" json:"id"`
	Name      string    `gorm:"column:name;type:string;default:'';NOT NULL;" json:"name"`
	Age       int       `gorm:"column:age;type:INT UNSIGNED;default:0;NOT NULL;" json:"age"`
	CreatedAt time.Time `gorm:"column:created_at;type:datetime;default:CURRENT_TIMESTAMP;" json:"created_at"`
	UpdatedAt time.Time `gorm:"column:updated_at;type:datetime;default:CURRENT_TIMESTAMP;" json:"updated_at"`
}
