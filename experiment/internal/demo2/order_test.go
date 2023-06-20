package demo2

import (
	"fmt"
	"github.com/golang/mock/gomock"
	_ "github.com/smartystreets/goconvey/convey"
	"github.com/stretchr/testify/assert"
	"log"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestOrder(t *testing.T) {
	var orderDBI OrderDBI
	orderDBI = new(OrderInfo)
	ret := orderDBI.GetName(1)
	fmt.Println("获取到order名称", ret)
	assert.NotEmpty(t, ret)
}

func TestGetName(t *testing.T) {

	//新建一个mockController
	ctrl := gomock.NewController(t)
	// 断言 DB.GetName() 方法是否被调用
	defer ctrl.Finish()

	//mock接口
	mock := NewMockOrderDBI(ctrl)
	//模拟传入值与预期的返回值
	mock.EXPECT().GetName(gomock.Eq(1225)).Return("xdcutecute")

	//前面定义了传入值与返回值
	//在这里
	if v := mock.GetName(1225); v != "xdcut" {
		t.Fatal("expected xdcute, but got", v)
	} else {
		log.Println("通过mock取到的name：", v)
	}
}


func TestMyHandler(t *testing.T) {
	req := httptest.NewRequest("GET", "/path", nil)
	w := httptest.NewRecorder()
	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		fmt.Fprint(w, "Hello, world!")
	})
	handler.ServeHTTP(w, req)
	if w.Code != http.StatusOK {
		t.Errorf("expected status code %d but got %d", http.StatusOK, w.Code)
	}
	expectedBody := "Hello, world!"
	if w.Body.String() != expectedBody {
		t.Errorf("expected body %q but got %q", expectedBody, w.Body.String())
	}
}
