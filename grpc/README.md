vagrant@node1:~/code/experiments/grpc$ go run cmd/server/main.go 
2023/04/28 03:07:56 Received: Go World


vagrant@node1:~/code/experiments/grpc$ go run client/main.go 
2023/04/28 03:08:05 Greeting: Hello Go World


vagrant@node1:~/code/experiments/grpc$ grpcurl -plaintext -d '{"name":"avc"}' 127.0.0.1:50051 hello.Greeter.SayHello
{
  "message": "Hello avc"
}


_protoc --go_out=./ --go-grpc_out=./ --grpc-gateway_out ./ hello.proto_