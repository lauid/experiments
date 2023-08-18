docker run -d --name zookeeper -p 2181:2181  wurstmeister/zookeeper
docker run -d --name kafka -p 9092:9092 -e KAFKA_BROKER_ID=0 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 --link zookeeper -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.1.60(机器IP):9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -t wurstmeister/kafka
docker run -d --name kafka -p 9092:9092 -e KAFKA_BROKER_ID=0 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 --link zookeeper -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.31.149:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -t wurstmeister/kafka


## 进入到kafka的docker容器中

docker exec -it kafka sh


## 运行消费者,进行消息的监听

kafka-console-consumer.sh --bootstrap-server 192.168.31.149:9092 --topic kafeidou --from-beginning

## 打开一个新的ssh窗口,同样进入kafka的容器中,执行下面这条命令生产消息

kafka-console-producer.sh --broker-list 192.168.31.149:9092 --topic kafeidou
