package main

import (
	"hello/pkg"
	"io"
	"net/http"
	"net/rpc"
	"net/rpc/jsonrpc"
)

func main() {
	_ = rpc.RegisterName("HelloService", new(pkg.HelloService))

	http.HandleFunc("/jsonrpc", func(w http.ResponseWriter, r *http.Request) {
		var conn io.ReadWriteCloser = struct {
			io.Writer
			io.ReadCloser
		}{
			ReadCloser: r.Body,
			Writer:     w,
		}

		_ = rpc.ServeRequest(jsonrpc.NewServerCodec(conn))
	})

	_ = http.ListenAndServe(":1234", nil)
}
