package main

import (
	"encoding/json"
	"log"
	"net/http"
)

type RegisterReq struct {
	Username string `json:"username"`
}

type RegisterRes struct {
	Token string `json:"token"`
}

func main() {
	mux := http.NewServeMux()

	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("ok"))
	})

	mux.HandleFunc("/register", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		var req RegisterReq
		_ = json.NewDecoder(r.Body).Decode(&req)
		// TODO: generate UUID, store user, return token
		res := RegisterRes{Token: "uuid-placeholder"}
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(res)
	})

	mux.HandleFunc("/login", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		w.WriteHeader(http.StatusOK)
	})

	log.Println("Signalix server on :3002")
	log.Fatal(http.ListenAndServe(":3002", mux))
}
