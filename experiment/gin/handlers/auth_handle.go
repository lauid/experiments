package handlers

import (
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
	"log"
	"net/http"
	"time"
	"github.com/gorilla/websocket"
)

// LoginHandler 用户登录处理函数
func LoginHandler(c *gin.Context) {
	// 在这里进行用户凭据的验证，比如检查用户名和密码是否正确

	// 如果验证通过，生成 JWT
	token := jwt.New(jwt.SigningMethodHS256)

	// 设置有效载荷
	claims := token.Claims.(jwt.MapClaims)
	claims["username"] = "user@example.com"
	claims["exp"] = time.Now().Add(time.Hour * 24).Unix() // 设置过期时间

	// 在服务端中保存用于签名的密钥，此处使用示例密钥
	key := []byte("secret_key")

	// 签名并获取完整的 JWT
	signedToken, _ := token.SignedString(key)

	// 将 JWT 返回给客户端
	c.JSON(http.StatusOK, gin.H{"token": signedToken})
}

// ProtectedHandler 受保护的接口处理函数
func ProtectedHandler(c *gin.Context) {
	// 从上下文中获取用户信息
	username := c.MustGet("username").(string)

	// 处理受保护的接口逻辑
	c.JSON(http.StatusOK, gin.H{"message": "您好，" + username})
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		// 允许所有的请求跨域访问
		return true
	},
}

func HandleWebSocket(c *gin.Context) {
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		log.Println("Failed to upgrade connection to WebSocket:", err)
		return
	}
	defer conn.Close()

	for {
		// 读取客户端发来的消息
		_, msg, err := conn.ReadMessage()
		if err != nil {
			log.Println("Failed to read message from WebSocket:", err)
			break
		}

		// 输出接收到的消息
		log.Printf("Received message from client: %s", msg)

		// 回复客户端消息
		err = conn.WriteMessage(websocket.TextMessage, msg)
		if err != nil {
			log.Println("Failed to send message to WebSocket:", err)
			break
		}
	}
}