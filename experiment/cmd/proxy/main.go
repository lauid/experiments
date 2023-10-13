package main

import (
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
)

func main() {
	// 创建代理处理器函数
	proxyHandler := func(w http.ResponseWriter, r *http.Request) {
		// 解析目标URL
		targetURL, err := url.Parse(r.URL.Scheme + "://" + r.URL.Host)
		if err != nil {
			http.Error(w, "Bad Gateway", http.StatusBadGateway)
			return
		}

		// 创建反向代理实例并设置代理地址
		proxy := httputil.NewSingleHostReverseProxy(targetURL)
		proxy.Transport = &http.Transport{Proxy: http.ProxyFromEnvironment}

		// 修改请求头以支持透明代理
		r.Host = targetURL.Host
		r.URL.Host = targetURL.Host
		r.URL.Scheme = targetURL.Scheme

		// 发送请求到目标服务器，并将响应返回给客户端
		proxy.ServeHTTP(w, r)
	}

	// 创建HTTP服务器并注册代理处理器函数
	http.HandleFunc("/", proxyHandler)

	// 启动服务器并监听请求
	if err := http.ListenAndServe(":8888", nil); err != nil {
		log.Fatal(err)
	}
}
