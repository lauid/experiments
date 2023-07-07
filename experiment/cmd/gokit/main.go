package main

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/go-kit/kit/endpoint"
	"github.com/go-kit/kit/log"
	kithttp "github.com/go-kit/kit/transport/http"
)

type Logger interface {
	Log(keyvals ...interface{}) error
}

func LoggingMiddleware(logger Logger) endpoint.Middleware {
	return func(next endpoint.Endpoint) endpoint.Endpoint {
		return func(ctx context.Context, request interface{}) (interface{}, error) {
			// 执行前置操作，例如记录请求信息
			logger.Log("msg", "Received a request")

			// 调用下一个端点
			response, err := next(ctx, request)

			// 执行后置操作，例如记录响应信息
			logger.Log("msg", "Returned a response")

			return response, err
		}
	}
}

// 定义服务接口
type HelloService interface {
	SayHello(name string) string
}

// 实现服务接口
type helloService struct{}

func (s *helloService) SayHello(name string) string {
	return fmt.Sprintf("Hello, %s!", name)
}

// 定义请求和响应结构体
type helloRequest struct {
	Name string `json:"name"`
}

type helloResponse struct {
	Message string `json:"message"`
}

// 创建服务的endpoint
func makeHelloEndpoint(svc HelloService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		req := request.(helloRequest)
		msg := svc.SayHello(req.Name)
		return helloResponse{Message: msg}, nil
	}
}

// 创建HTTP Handler
func main() {
	logger := log.NewLogfmtLogger(log.NewSyncWriter(os.Stderr))
	var svc HelloService
	svc = &helloService{}

	helloHandler := kithttp.NewServer(
		LoggingMiddleware(logger)(makeHelloEndpoint(svc)),
		decodeHelloRequest,
		encodeHelloResponse,
	)

	http.Handle("/hello", helloHandler)
	logger.Log("msg", "HTTP", "addr", ":8080")
	logger.Log("err", http.ListenAndServe(":8080", nil))
}

// 解码请求
func decodeHelloRequest(ctx context.Context, r *http.Request) (interface{}, error) {
	if r.Method != http.MethodPost {
		return nil, fmt.Errorf("invalid request method")
	}

	var request helloRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}
	return request, nil
}

// 编码响应
func encodeHelloResponse(ctx context.Context, w http.ResponseWriter, response interface{}) error {
	return json.NewEncoder(w).Encode(response)
}
