package main

import (
	"database/sql"
	"fmt"
	_ "github.com/go-sql-driver/mysql"
	"github.com/gorilla/mux"
	_ "github.com/mattn/go-sqlite3"
	"github.com/teris-io/shortid"
	"log"
	"net/http"
)

type ShortLink struct {
	ID        int
	Original  string
	Shortened string
}

var db *sql.DB

//root@node1:~# curl -X POST -d url=https://qq.com 127.0.0.1:8080/create

func main() {
	// 初始化数据库连接
	initDB()

	// 创建路由
	router := mux.NewRouter()
	router.HandleFunc("/{shortened}", redirectHandler).Methods("GET")
	router.HandleFunc("/create", createHandler).Methods("POST")

	// 启动HTTP服务器
	log.Println("启动短链接服务...")
	http.ListenAndServe(":8080", router)
}

func initDB() {
	// 建立数据库连接
	var err error
	db, err = sql.Open("sqlite3", "shortlinks.db")
	if err != nil {
		log.Fatal(err)
	}

	// 创建shortlinks表（如果不存在）
	_, err = db.Exec(`
		CREATE TABLE IF NOT EXISTS shortlinks (
			id INT AUTO_INCREMENT,
			original VARCHAR(2048) NOT NULL,
			shortened VARCHAR(128) NOT NULL UNIQUE,
			PRIMARY KEY (id)
		)
	`)
	if err != nil {
		log.Fatal(err)
	}

	log.Println("数据库连接成功")
}

func createHandler(w http.ResponseWriter, r *http.Request) {
	// 解析请求体中的原始链接
	r.ParseForm()
	original := r.Form.Get("url")
	fmt.Println(original)

	// 生成短链接
	shortened, err := generateShortLink()
	if err != nil {
		log.Println(err)
		http.Error(w, "无法生成短链接", http.StatusInternalServerError)
		return
	}

	// 将原始链接和短链接保存到数据库中
	_, err = db.Exec("INSERT INTO shortlinks (original, shortened) VALUES (?, ?)", original, shortened)
	if err != nil {
		log.Println(err)
		http.Error(w, "无法保存短链接", http.StatusInternalServerError)
		return
	}

	// 返回短链接给客户端
	shortURL := fmt.Sprintf("http://192.168.56.11:8080/%s", shortened)

	fmt.Fprint(w, shortURL)
}

func redirectHandler(w http.ResponseWriter, r *http.Request) {
	// 获取路径中的短链接
	vars := mux.Vars(r)
	shortened := vars["shortened"]

	// 查询数据库，获取对应的原始链接
	var original string
	err := db.QueryRow("SELECT original FROM shortlinks WHERE shortened = ?", shortened).Scan(&original)
	if err != nil {
		log.Println(err)
		http.NotFound(w, r)
		return
	}

	// 重定向到原始链接
	http.Redirect(w, r, original, http.StatusFound)
}

func generateShortLink() (string, error) {
	// 创建一个全局的短标识符生成器
	idGenerator, err := shortid.New(1, shortid.DefaultABC, 1234)
	if err != nil {
		return "", err
	}

	// 生成一个唯一的短链接
	shortURL, err := idGenerator.Generate()
	if err != nil {
		return "", err
	}

	return shortURL, nil
}
