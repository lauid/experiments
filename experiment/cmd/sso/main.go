package main

import (
	"fmt"
	"net/http"
	"net/url"
)

//http://192.168.56.11:8000/login
func main() {
	// SSO 认证中心的地址
	idpURL := "http://192.168.56.11:8080/auth"

	// 登录处理函数
	loginHandler := func(w http.ResponseWriter, r *http.Request) {
		// 重定向到 SSO 认证中心进行登录
		redirectURL := idpURL + "?redirect=" + url.QueryEscape("http://192.168.56.11:8000/callback")
		http.Redirect(w, r, redirectURL, http.StatusTemporaryRedirect)
	}

	// 回调处理函数
	callbackHandler := func(w http.ResponseWriter, r *http.Request) {
		// 获取认证中心重定向的登录状态
		values, _ := url.ParseQuery(r.URL.RawQuery)
		username := values.Get("username")

		fmt.Fprintf(w, "Logged in as %s", username)
	}

	http.HandleFunc("/login", loginHandler)
	http.HandleFunc("/callback", callbackHandler)

	http.ListenAndServe(":8000", nil)
}
