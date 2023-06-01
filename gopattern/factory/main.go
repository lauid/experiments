package main

import (
	"net/http"
	"net/http/httptest"
)

type IPeople interface {
	Do(req *http.Request) (*http.Response, error)
}

type People struct {
	name string
	year int
}

func (p *People) Do(req *http.Request) (*http.Response, error) {
	rec := httptest.NewRecorder()
	return rec.Result(), nil
}

// 可以看到 new方法返回的是一个接口类型，当然了return的是一个实现了接口的structure，只不过这里有一个隐含的类型转换。
//
//这样做的好处是什么呢，比如现在有另一个new，newStudent ,它当然也定义了一个Do
func NewPeople(name string, year int) IPeople {
	return &People{
		name: name,
		year: year,
	}
}


func main() {

}
