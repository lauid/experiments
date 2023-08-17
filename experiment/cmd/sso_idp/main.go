package main

import (
	"fmt"
	"github.com/gorilla/sessions" // gorilla/sessions 提供了会话管理器和会话存储的实现
	"net/http"
)

var store = sessions.NewCookieStore([]byte("secret-key"))

//http://192.168.56.11:8080/login?username=user1&password=password1
func main() {
	// 用户凭据存储
	users := map[string]string{
		"user1": "password1",
		"user2": "password2",
	}

	// 登录处理函数
	loginHandler := func(w http.ResponseWriter, r *http.Request) {
		// 从请求中获取用户名和密码
		username := r.FormValue("username")
		password := r.FormValue("password")

		// 验证用户名和密码
		storedPassword, ok := users[username]
		if !ok || storedPassword != password {
			http.Error(w, "Invalid credentials", http.StatusUnauthorized)
			return
		}

		// 在用户会话中保存认证状态
		session, _ := store.Get(r, "sso-session")
		session.Values["authenticated"] = true
		session.Values["username"] = username
		session.Save(r, w)

		fmt.Fprintf(w, "Login successful")

	}

	// 注销处理函数
	logoutHandler := func(w http.ResponseWriter, r *http.Request) {
		session, _ := store.Get(r, "sso-session")
		session.Values["authenticated"] = false
		session.Values["username"] = ""
		session.Save(r, w)

		fmt.Fprintf(w, "Logout successful")
	}

	// 验证是否已登录的处理函数
	authHandler := func(w http.ResponseWriter, r *http.Request) {
		session, _ := store.Get(r, "sso-session")
		if session.Values["authenticated"] != true {
			http.Error(w, "Not authenticated", http.StatusUnauthorized)
			return
		}

		username := session.Values["username"].(string)

		redirect := r.FormValue("redirect")
		if redirect != "" {
			http.Redirect(w, r, redirect+"?"+"username="+username, http.StatusTemporaryRedirect)
		}

		fmt.Fprintf(w, "Authenticated as %s", username)
	}

	http.HandleFunc("/login", loginHandler)
	http.HandleFunc("/logout", logoutHandler)
	http.HandleFunc("/auth", authHandler)

	http.ListenAndServe(":8080", nil)
}
