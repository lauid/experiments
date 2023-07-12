package jaeger

import (
	"github.com/gin-gonic/gin"
	"github.com/opentracing/opentracing-go"
	"github.com/opentracing/opentracing-go/ext"
	"github.com/uber/jaeger-client-go"
	jaegercfg "github.com/uber/jaeger-client-go/config"
	"io"
	"log"
)

/**
root@node3:~# docker run -p 6831:6831 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one
*/

func InitJaeger(serviceName string) (opentracing.Tracer, io.Closer) {
	cfg := jaegercfg.Configuration{
		ServiceName: serviceName,
		Sampler: &jaegercfg.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
		Reporter: &jaegercfg.ReporterConfig{
			QueueSize:           2,
			BufferFlushInterval: 1,
			LogSpans:            true,
			//LocalAgentHostPort: "localhost:6831", // Jaeger Agent 地址
			CollectorEndpoint: "http://127.0.0.1:14268/api/traces",
		},
	}

	tracer, closer, err := cfg.NewTracer(jaegercfg.Logger(jaeger.StdLogger))
	if err != nil {
		log.Fatal("Could not initialize jaeger tracer:", err.Error())
	}

	opentracing.SetGlobalTracer(tracer)

	return tracer, closer
}

// GinMiddleware 是一个用于 Gin 的 Jaeger 链路跟踪中间件
func GinMiddleware(tracer opentracing.Tracer) gin.HandlerFunc {
	return func(c *gin.Context) {
		span := tracer.StartSpan(c.Request.URL.Path)
		defer span.Finish()

		ctx := opentracing.ContextWithSpan(c.Request.Context(), span)
		c.Request = c.Request.WithContext(ctx)

		// 设置请求的 Tags
		ext.HTTPMethod.Set(span, c.Request.Method)
		ext.HTTPUrl.Set(span, c.Request.URL.String())

		// 调用下一个处理程序
		c.Next()
	}
}

func GetJaegerTraceMiddleware(tracer opentracing.Tracer) gin.HandlerFunc {
	return func(c *gin.Context) {
		spanContext, _ := tracer.Extract(opentracing.HTTPHeaders, opentracing.HTTPHeadersCarrier(c.Request.Header))
		span := tracer.StartSpan(c.Request.URL.Path, ext.RPCServerOption(spanContext))
		defer span.Finish()

		// 将span的上下文设置回gin的上下文
		ctx := opentracing.ContextWithSpan(c.Request.Context(), span)
		c.Request = c.Request.WithContext(ctx)

		// 设置请求的 Tags
		ext.HTTPMethod.Set(span, c.Request.Method)
		ext.HTTPUrl.Set(span, c.Request.URL.String())

		// 继续处理请求
		c.Next()

		// 在响应头中添加Trace ID
		traceID := span.Context().(jaeger.SpanContext).TraceID().String()
		c.Header("Trace-ID", traceID)
	}
}
