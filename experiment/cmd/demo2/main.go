package main

import (
	"fmt"
	"github.com/go-redis/redis"
	_ "github.com/mattn/go-sqlite3"
	"log"
	"os"
	"reflect"
	"strconv"
	"text/template"
	"time"
	"unicode/utf8"
)

func CheckUrl(url string) bool {
	var urlList = [2]string{"learnku.com", "xdcute.com"}
	for v := range urlList {
		if urlList[v] == url {
			return true
		}
	}
	return false
}

func Case1(a int) int {
	return a * 2
}

func Case2(inputSlice []int) map[int]*int {
	//slice := []int{0, 1, 2, 3}
	m := make(map[int]*int)

	for key, val := range inputSlice {
		value := val
		m[key] = &value
	}

	//for k, v := range m {
	//	fmt.Println(k, "===>", *v)
	//}
	return m
}

func EqualSlice[T any](a, b []T) bool {
	return reflect.DeepEqual(a, b)
}

// Redis 缓存装饰器
func redisCacheDecorator(fn func(int) string) func(int) string {
	return func(id int) string {
		client := redis.NewClient(&redis.Options{
			Addr: "localhost:6379",
		})
		key := "data:" + strconv.Itoa(id)
		if val, err := client.Get(key).Result(); err == nil {
			fmt.Println("get from redis")
			return val
		}
		fmt.Println("get from db")
		val := fn(id)
		client.Set(key, val, 5*time.Minute)
		return val
	}
}

// 模拟从数据库中获取数据
func getDataFromDB(id int) string {
	return "data from db"
}
func main2() {
	// 使用 Redis 缓存装饰器装饰 getDataFromDB 函数
	getDataWithCache := redisCacheDecorator(getDataFromDB)
	// 第一次获取数据，从数据库中获取
	fmt.Println(getDataWithCache(1))
	// 第二次获取数据，从 Redis 缓存中获取
	fmt.Println(getDataWithCache(1))
	// 第三次获取数据，从数据库中获取
	fmt.Println(getDataWithCache(2))
	// 第四次获取数据，从 Redis 缓存中获取
	fmt.Println(getDataWithCache(2))
}

func main5() {
	str := "我是中国人"
	strRune := []rune(str)
	fmt.Println(string(strRune[2:]))
}

func main3() {
	s := "你好,世界"
	for i, i2 := range s {
		fmt.Println(i)
		fmt.Println(i2)
	}
}
func main4() {
	// Define a template.
	const letter = `
Dear {{.Name}},
{{if .Attended}}
It was a pleasure to see you at the wedding.
{{- else}}
It is a shame you couldn't make it to the wedding.
{{- end}}
{{with .Gift -}}
Thank you for the lovely {{.}}.
{{end}}
Best wishes,
Josie
`

	// Prepare some data to insert into the template.
	type Recipient struct {
		Name, Gift string
		Attended   bool
	}
	var recipients = []Recipient{
		{"Aunt Mildred", "bone china tea set", true},
		{"Uncle John", "moleskin pants", false},
		{"Cousin Rodney", "", false},
	}

	// Create a new template and parse the letter into it.
	t := template.Must(template.New("letter").Parse(letter))

	// Execute the template for each recipient.
	for _, r := range recipients {
		err := t.Execute(os.Stdout, r)
		if err != nil {
			log.Println("executing template:", err)
		}
	}

}

func main11() {
	str := "搜索,hello"
	strRune := []rune(str)
	fmt.Println(string(strRune[:2]))
}

func main1() {
	str := "搜索,hello"
	fmt.Println("字节数：", len(str))
	fmt.Println("字符数：", utf8.RuneCountInString(str))
	bytes := []byte(str)
	fmt.Println(string(bytes))
	fmt.Println(byteToUnicodeString(bytes))
}

func byteToUnicodeString(bytes []byte) string {
	str := ""
	for len(bytes) > 0 {
		r, size := utf8.DecodeRune(bytes)
		str += string(r)
		bytes = bytes[size:]
	}
	return str
}

func main() {
	main4()
}
