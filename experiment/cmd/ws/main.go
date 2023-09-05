package main

import (
	"flag"
	"fmt"
	"github.com/google/uuid"
	"log"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/go-redis/redis"
	"github.com/gorilla/websocket"
)

type Message struct {
	Text string `json:"text"`
}

type Client struct {
	conn   *websocket.Conn
	userID string
	mutex  sync.Mutex
	connID string
	time   time.Time
}

var (
	upgrader = websocket.Upgrader{CheckOrigin: func(r *http.Request) bool {
		return true
	}}
	clientMapKey = "client_map"
	redisClient  = redis.NewClient(&redis.Options{
		Addr: "localhost:6379",
	})
	clientConns map[string]*Client
	addr        string
)

func main() {
	flag.StringVar(&addr, "addr", ":8080", "listen addr")
	flag.Parse()

	clientConns = make(map[string]*Client, 1000)
	http.HandleFunc("/send", func(writer http.ResponseWriter, request *http.Request) {
		// 获取用户标识符
		userID := request.URL.Query().Get("userID")
		msg := request.URL.Query().Get("msg")
		sendMessage(userID, []byte(msg))
		writer.Write([]byte("ok"))
	})

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		fmt.Println("-------------------ws")
		// 升级连接为 WebSocket
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			log.Println(err)
			return
		}
		defer conn.Close()

		// 获取用户标识符
		userID := r.URL.Query().Get("userID")
		connID := uuid.New().String()
		// 创建客户端
		client := &Client{
			conn:   conn,
			userID: userID,
			connID: connID,
			time:   time.Now(),
		}
		clientConns[connID] = client
		defer delete(clientConns, connID)

		// 将客户端添加到 Redis 中
		err = addClientToRedis(userID, connID)
		if err != nil {
			log.Println(err)
			return
		}
		defer removeClientFromRedis(userID)

		for {
			// 读取客户端发送的消息
			_, message, err := conn.ReadMessage()
			if err != nil {
				log.Println("read:", err)
				break
			}
			fmt.Println("rec:", string(message))

			// 处理收到的消息
			//sendMessage(userID, message)
		}

	})

	// 运行 HTTP 服务器
	fmt.Println(addr)
	err := http.ListenAndServe(addr, nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}

func sendMessage(userID string, message []byte) {
	// 根据用户标识符处理消息
	// 这里可以根据需要进行业务逻辑的处理，例如广播消息给其他用户等
	//log.Printf("Received message from user %s: %s\n", userID, string(message))

	// 在 Redis 中查找客户端 WebSocket 连接
	client, err := getClientFromRedis(userID)
	if err != nil {
		log.Println("get client err:",err)
		return
	}
	fmt.Printf("%+v\n", client)
	pos := strings.Index(client, ":")
	connID := client[:pos]
	connAddr := client[pos:]
	if addr != connAddr {
		get, err := http.Get(fmt.Sprintf("http://127.0.0.1"+connAddr+"/send?userID=%s&msg=%s", userID,string(message)))
		if err != nil {
			fmt.Println("http get err:",err)
			return
		}
		fmt.Println("http get req:", get)
	} else if connClient, ok := clientConns[connID]; ok {
		err := connClient.conn.WriteMessage(websocket.TextMessage, message)
		if err != nil {
			fmt.Println("write message err:", err)
			return
		}
	} else {
		fmt.Println("not found conn.")
	}

	//// 发送消息到客户端
	//client.mutex.Lock()
	//defer client.mutex.Unlock()
	//err = client.conn.WriteJSON(Message{Text: string(message)})
	//if err != nil {
	//	log.Println(err)
	//	return
	//}
}

func getClientKey(userID string) string {
	return clientMapKey + ":" + userID
}

func addClientToRedis(userID string, connID string) error {
	// 保存客户端信息到 Redis
	return redisClient.Set(getClientKey(userID), connID+addr, 0).Err()
}

func removeClientFromRedis(userID string) error {
	// 从 Redis 中删除客户端信息
	return redisClient.Del(getClientKey(userID)).Err()
}

func getClientFromRedis(userID string) (string, error) {
	// 从 Redis 中获取客户端信息
	data, err := redisClient.Get(getClientKey(userID)).Bytes()
	if err != nil {
		return "", err
	}

	return string(data), nil
}
