package user

import (
	"net/http"
	"wire2/domain"
)

type handler struct {
	svc domain.UserService
}

func (h *handler) FetchByUsername() http.HandlerFunc {
	panic("implement me")
}
