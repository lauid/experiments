
```
# goctl api go -api bookstore.api -dir .


# goctl rpc template -o add.proto

# goctl rpc protoc add.proto --go_out=. --go-grpc_out=. --zrpc_out=.


# ~/code/experiments/go-zero/bookstore/rpc/add$ go run add.go -f etc/add.yaml

# ~/code/experiments/go-zero/bookstore/rpc/check$ go run check.go -f etc/check.yaml 

# ~/code/experiments/go-zero/bookstore/api$ go run bookstore.go -f etc/bookstore-api.yaml

vagrant@node1:~$ curl -i "http://localhost:8888/add?book=go-zero&price=11"

vagrant@node1:~$ curl -i "http://localhost:8888/check?book=go-zero"

```