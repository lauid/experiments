package middlewares

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
	"net/http"
)

// AuthMiddleware 鉴权中间件
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 从请求头中获取 JWT
		tokenString := c.GetHeader("Authorization")
		if tokenString == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "未提供身份验证令牌"})
			c.Abort()
			return
		}

		// 解析 JWT
		token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
			// 验证签名所用的算法是否一致
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("签名方法不匹配")
			}

			// 返回用于验证签名的密钥
			return []byte("secret_key"), nil
		})

		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "无效的身份验证令牌"})
			c.Abort()
			return
		}

		// 验证 JWT 是否有效
		if !token.Valid {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "身份验证令牌已过期"})
			c.Abort()
			return
		}

		// 将用户信息存储到上下文中，方便后续处理函数使用
		claims := token.Claims.(jwt.MapClaims)
		c.Set("username", claims["username"])

		// 继续往下执行
		c.Next()
	}
}
