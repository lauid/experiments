package main

import (
	"database/sql"
	"net/http"
	"wire2/user"
)

func main() {
	db, err := sql.Open("postgres", "")
	if err != nil {
		return
	}

	userHandler := user.Wire(db)
	http.Handle("/user", userHandler.FetchByUsername())
	http.ListenAndServe(":8888", nil)
}
