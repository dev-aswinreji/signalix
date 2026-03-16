package main

import "sync"

type KeyBundle struct {
	Username string `json:"username"`
	Identity string `json:"identity"`
	PreKey   string `json:"preKey"`
}

type KeyStore struct {
	mu    sync.Mutex
	store map[string]KeyBundle
}

func NewKeyStore() *KeyStore {
	return &KeyStore{store: make(map[string]KeyBundle)}
}

func (k *KeyStore) Put(b KeyBundle) {
	k.mu.Lock()
	defer k.mu.Unlock()
	k.store[b.Username] = b
}

func (k *KeyStore) Get(user string) (KeyBundle, bool) {
	k.mu.Lock()
	defer k.mu.Unlock()
	b, ok := k.store[user]
	return b, ok
}
