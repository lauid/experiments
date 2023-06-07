package main

import (
	"crypto/tls"
	"time"
)

type Server struct {
	Addr string
	Port int
	Protocol string
	Timeout time.Duration
	Maxconns int
	TLS *tls.Config
}

type Options func(s *Server)

func Timeout(timeout time.Duration)  Options{
	return func(s *Server) {
		s.Timeout = timeout
	}
}

func Stl(stl *tls.Config)  Options{
	return func(s *Server) {
		s.TLS = stl
	}
}

func NewServer(addr string, port int, options ...Options) {
	s := &Server{
		Addr: addr,
		Port: port,
		Protocol: "tcp",
		Maxconns: 10,
	}

	for _, option := range options {
		option(s)
	}
}

func main()  {
	NewServer("127.0.0.1", 8080, Timeout(5*time.Second), Stl(nil))
}
