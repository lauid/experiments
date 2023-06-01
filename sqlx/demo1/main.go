package main

import (
	_ "database/sql"
	"fmt"
	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
	"log"
)

func main() {
	db, err := sqlx.Connect("postgres", "user=mainflux password=mainflux dbname=test host=192.168.175.206 port=15432 sslmode=disable")
	if err != nil {
		log.Fatalln(err)
	}
	defer db.Close()

	// 批量插入数据
	//data := []struct {
	//	Name string
	//	Age  int
	//}{
	//	{"Tom", 18},
	//	{"Bob", 20},
	//	{"Charlie", 22},
	//}

	values := []int{18, 20, 22}
	// build query
	query, args, err := sqlx.In("DELETE FROM users WHERE id IN (?)", values)
	if err != nil {
		panic(err)
	}
	fmt.Println(query)

	query = db.Rebind(query)
	fmt.Println(query)
	// execute query
	_, err = db.Exec(query, args...)
	if err != nil {
		panic(err)
	}
}

func main2() {
	db, err := sqlx.Connect("postgres", "user=mainflux password=mainflux dbname=test host=192.168.175.206 port=15432 sslmode=disable")
	if err != nil {
		log.Fatalln(err)
	}
	defer db.Close()

	createTable := `CREATE TABLE IF NOT EXISTS users (
              id SERIAL PRIMARY KEY,
              name VARCHAR(50),
              age INT
          )`
	db.MustExec(createTable)
	// 批量插入数据
	data := []struct {
		Name string
		Age  int
	}{
		{"Tom", 18},
		{"Bob", 20},
		{"Charlie", 22},
	}
	query := `INSERT INTO users (name, age) VALUES (:name, :age)`
	res, err := db.NamedExec(query, data)
	if err != nil {
		panic(err)
	}
	log.Println(res)
}

func main1() {
	// 连接数据库
	db, err := sqlx.Open("sqlite3", "./test.db")
	if err != nil {
		log.Fatalln(err)
	}
	defer db.Close()
	// 建表
	createTable := `CREATE TABLE IF NOT EXISTS users (
              id SERIAL PRIMARY KEY,
              name VARCHAR(50),
              age INT
          )`
	db.MustExec(createTable)

	// 批量插入数据
	data := []struct {
		Name string
		Age  int
	}{
		{"Alice", 18},
		{"Bob", 20},
		{"Charlie", 22},
	}
	query := `INSERT INTO users (name, age) VALUES (:name, :age)`
	db.NamedExec(query, data)
}
