
docker run --rm -p 8080:8080 experiment/gin:latest

http://192.168.40.180:8080/health


go get -u github.com/golang/mock/gomock
go get -u github.com/golang/mock/mockgen


## swagger

docker run -p 6831:6831 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one

swag init --parseVendor -g cmd/gin/main.go
http://192.168.40.180:8080/swagger/index.html



1，gin优雅退出
2，中间件
3，jaeger
4,登录授权


root@node1:~/code/demo# docker run -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.1.0
root@node1:~/code/demo# mysql -h 127.0.0.1 -u root -p

