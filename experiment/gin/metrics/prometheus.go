package metrics

import (
	"github.com/gin-gonic/gin"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"net/http"
	"strconv"
)

var (
	httpRequestsTotal = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "http_requests_total",
			Help: "Total number of HTTP requests",
		},
		[]string{"code", "method"},
	)
)

func init() {
	prometheus.MustRegister(httpRequestsTotal)
}

func GetPrometheusMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		code := strconv.Itoa(c.Writer.Status())
		method := c.Request.Method
		httpRequestsTotal.WithLabelValues(code, method).Inc()

		c.Next()
	}
}

func GetPrometheusHttpHandler() http.Handler {
	return promhttp.Handler()
}
