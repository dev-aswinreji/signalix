package main

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"log"
	"net/http"
	"sync"
)

type User struct {
	Username string `json:"username"`
	Token    string `json:"token"`
}

type RegisterReq struct {
	Username string `json:"username"`
}

type RegisterRes struct {
	Token string `json:"token"`
}

type LoginReq struct {
	Username string `json:"username"`
	Token    string `json:"token"`
}

var (
	usersMu sync.Mutex
	users   = make(map[string]User)
	keys    = NewKeyStore()
)

func newToken() string {
	b := make([]byte, 16)
	_, _ = rand.Read(b)
	return hex.EncodeToString(b)
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
		if req.Username == "" {
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		usersMu.Lock()
		defer usersMu.Unlock()
		if _, exists := users[req.Username]; exists {
			w.WriteHeader(http.StatusConflict)
			return
		}
		token := newToken()
		users[req.Username] = User{Username: req.Username, Token: token}
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(RegisterRes{Token: token})
	})

	mux.HandleFunc("/login", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		var req LoginReq
		_ = json.NewDecoder(r.Body).Decode(&req)
		usersMu.Lock()
		defer usersMu.Unlock()
		user, ok := users[req.Username]
		if !ok || user.Token != req.Token {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		w.WriteHeader(http.StatusOK)
	})

	mux.HandleFunc("/keys/upload", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		var b KeyBundle
		_ = json.NewDecoder(r.Body).Decode(&b)
		if b.Username == "" {
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		keys.Put(b)
		w.WriteHeader(http.StatusOK)
	})

	mux.HandleFunc("/keys/get", func(w http.ResponseWriter, r *http.Request) {
		user := r.URL.Query().Get("u")
		if user == "" {
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		if b, ok := keys.Get(user); ok {
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode(b)
			return
		}
		w.WriteHeader(http.StatusNotFound)
	})

	log.Println("Signalix server on :3002")
	log.Fatal(http.ListenAndServe(":3002", mux))
}
