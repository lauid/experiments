package metrics

import (
	"github.com/gin-gonic/gin"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"net/http"
	"strconv"
	"time"
)

var (
	httpRequestsTotal = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "http_requests_total",
			Help: "Total number of HTTP requests",
		},
		[]string{"code", "method"},
	)
	httpLatency = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "http_latency",
			Help:    "HTTP request latency in milliseconds",
			Buckets: []float64{100, 200, 300, 400, 500},
		},
		[]string{"code", "method"},
	)
)

func init() {
	prometheus.MustRegister(httpRequestsTotal)
	prometheus.MustRegister(httpLatency)
}

func GetPrometheusCounterMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		code := strconv.Itoa(c.Writer.Status())
		method := c.Request.Method
		httpRequestsTotal.WithLabelValues(code, method).Inc()

		c.Next()
	}
}

func GetPrometheusLatencyMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 开始计时
		start := time.Now()

		// 处理请求

		// 结束计时，并计算延迟时间
		latency := float64(time.Since(start).Milliseconds())

		// 更新指标数据
		code := strconv.Itoa(c.Writer.Status())
		method := c.Request.Method
		httpLatency.WithLabelValues(code, method).Observe(latency)
		httpRequestsTotal.WithLabelValues(code, method).Inc()
	}
}

func GetPrometheusHttpHandler() http.Handler {
	return promhttp.Handler()
}
