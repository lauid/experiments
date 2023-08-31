docker run -d --hostname my-rabbit  -p 8080:15672 -p 5672:5672 -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password --rm rabbitmq:3-management

