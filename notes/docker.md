#### mysql

docker run -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.1.0

#### elasticsearch

docker run -d --name elasticsearch --net somenetwork -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:tag
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --rm elasticsearch:8.8.1
docker exec -it 9a5951e5da21  elasticsearch-reset-password -u elastic -f

# docker run -d --name elasticsearch --net somenetwork -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:tag
# docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:tag


###  dtm

docker run -itd  --name dtm -p 36789:36789 -p 36790:36790  yedf/dtm:latest

#### rabbitmq

docker run -d --hostname my-rabbit  -p 8080:15672 -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password --rm rabbitmq:3-management

#### mongo

docker run -p 27017:27017 mongo:4.4.24-rc0

#### docker导出镜像，ctr导入

docker tag experiment/gin:v0.0.1 k8s.io/experiment/gin:v0.0.1
docker save k8s.io/experiment/gin:v0.0.1 -o ~/gin.tar
ctr -n=k8s.io image import ./gin.tar

### busybox 测试k8s

kubectl run busybox --image=busybox:1.28 --restart=Never -- /bin/sh -c "while true; do echo hello; sleep 10; done"

kubectl run busybox --image=busybox:1.28 -it --restart=Never -- /bin/sh


#### docker常用命令

删除所有的 "none" 镜像
docker rmi $(docker images -f "dangling=true" -q)

清理已退出的容器
docker ps -a --filter "status=exited" | awk '{print $1}'| xargs docker rm
