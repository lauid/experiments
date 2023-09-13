package main

import (
	"context"
	"fmt"
	"go.etcd.io/etcd/clientv3"
	"time"
)

type EtcdMutex struct {
	cli        *clientv3.Client
	lease      clientv3.LeaseID
	cancelFunc context.CancelFunc
	isLocked   bool
	lockKey    string
	lockValue  string
}

// NewEtcdMutex 初始化EtcdMutex
func NewEtcdMutex(endpoints []string, lockKey, lockValue string) (*EtcdMutex, error) {
	cli, err := clientv3.New(clientv3.Config{
		Endpoints:   endpoints,
		DialTimeout: 5 * time.Second,
	})
	if err != nil {
		return nil, err
	}

	return &EtcdMutex{
		cli:       cli,
		lockKey:   lockKey,
		lockValue: lockValue,
	}, nil
}

// TryLock 尝试获取锁
func (m *EtcdMutex) TryLock() error {
	resp, err := m.cli.Grant(context.TODO(), 10)
	if err != nil {
		return err
	}

	ctx, cancelFunc := context.WithCancel(context.Background())
	defer cancelFunc()

	keepAliveChan, err := m.cli.KeepAlive(ctx, resp.ID)
	if err != nil {
		return err
	}

	// 存储租约 ID 和取消函数，用于后续释放锁
	m.lease = resp.ID
	m.cancelFunc = cancelFunc

	// 创建一个事务
	txn := m.cli.Txn(context.TODO())
	// 尝试将锁的键值关联到租约
	txn.If(clientv3.Compare(clientv3.CreateRevision(m.lockKey), "=", 0)).
		Then(clientv3.OpPut(m.lockKey, m.lockValue, clientv3.WithLease(resp.ID))).
		Else(clientv3.OpGet(m.lockKey))

	// 事务提交
	respTxn, err := txn.Commit()
	if err != nil {
		return err
	}

	// 判断是否获取到锁
	if !respTxn.Succeeded {
		return fmt.Errorf("failed to acquire lock")
	}

	// 启动续约协程
	go func() {
		for {
			select {
			case _, ok := <-keepAliveChan:
				if !ok {
					return
				}
			}
		}
	}()

	// 锁已成功获取
	m.isLocked = true
	return nil
}

// Unlock 释放锁
func (m *EtcdMutex) Unlock() error {
	if !m.isLocked {
		return nil
	}

	// 停止续约协程
	m.cancelFunc()

	// 撤销租约
	_, err := m.cli.Revoke(context.TODO(), m.lease)
	if err != nil {
		return err
	}

	// 释放成功
	m.isLocked = false
	return nil
}

func main() {
	// etcd 客户端连接配置
	endpoints := []string{"http://localhost:2379"}

	// 创建 EtcdMutex 实例
	mutex, err := NewEtcdMutex(endpoints, "/my-lock", "my-value")
	if err != nil {
		fmt.Println("Failed to create EtcdMutex:", err)
		return
	}

	// 尝试获取锁
	err = mutex.TryLock()
	if err != nil {
		fmt.Println("Failed to acquire lock:", err)
		return
	}

	// 执行互斥操作

	// 释放锁
	err = mutex.Unlock()
	if err != nil {
		fmt.Println("Failed to release lock:", err)
		return
	}

	fmt.Println("Lock released")
}
