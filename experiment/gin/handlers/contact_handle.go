package handlers

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

type ContactHandle struct {
}

func (h ContactHandle) Contact(c *gin.Context) {
	c.HTML(http.StatusOK, "contact.tmpl", gin.H{
		"title": "Main website",
	})
}
