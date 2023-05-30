go mod vendor

make docker_main

docker run -p 8080:8080 -it godocker/main:latest  /bin/bash

curl http://192.168.56.12:8080/health