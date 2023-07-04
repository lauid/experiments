package etcd

import (
	"context"
	"go.etcd.io/etcd/clientv3"
	"go.etcd.io/etcd/clientv3/concurrency"
	"time"
)

func NewEtcdClient() (*clientv3.Client, error) {
	cli, err := clientv3.New(clientv3.Config{
		Endpoints:   []string{"localhost:2379"},
		DialTimeout: 5 * time.Second,
	})
	if err != nil {
		return nil, err
	}
	return cli, nil
}

func Lock(key string, client *clientv3.Client) (*concurrency.Mutex, error) {
	session, err := concurrency.NewSession(client)
	if err != nil {
		return nil, err
	}
	mutex := concurrency.NewMutex(session, key)
	if err := mutex.Lock(context.Background()); err != nil {
		return nil, err
	}
	return mutex, nil
}
