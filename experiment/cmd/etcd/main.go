package main

import (
	"context"
	"fmt"
	"go.etcd.io/etcd/client/v3"
	"go.etcd.io/etcd/client/v3/concurrency"
	_ "go.etcd.io/etcd/client/v3/concurrency"
	"log"
	"time"
)

func main() {
	//lockTest()
	grantTest()
}

// 注册发现
func grantTest() {
	endpoints := []string{"http://localhost:2379"}
	config := clientv3.Config{
		Endpoints:   endpoints,
		DialTimeout: 5 * time.Second,
	}
	client, err := clientv3.New(config)
	if err != nil {
		log.Fatal(err)
	}
	defer client.Close()

	key := "/services/my-service"                        // 注册的服务键
	value := "192.168.0.100:8000"                        //
	leaseTTL := clientv3.NewLease(client)                // 创建租约
	leaseResp, err := leaseTTL.Grant(context.TODO(), 10) // 设置租约失效时间为10秒
	if err != nil {
		log.Fatal(err)
	}

	_, err = client.Put(context.TODO(), key, value, clientv3.WithLease(leaseResp.ID))
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("service register success.")

	// 注册后延续租约，确保服务持续存在
	leaseKeepAliveChan, err := client.KeepAlive(context.TODO(), leaseResp.ID)
	if err != nil {
		log.Fatal(err)
	}

	go func() {
		for {
			select {
			case keepResp := <-leaseKeepAliveChan:
				if keepResp == nil {
					fmt.Println("续约失败")
					return
				}
			}
		}
	}()

	// 模拟服务运行
	time.Sleep(30 * time.Second)

	// 服务退出时撤销租约，将服务信息从 etcd 中删除
	_, err = leaseTTL.Revoke(context.TODO(), leaseResp.ID)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("服务注销成功")
}

// 分布式锁
func lockTest() {
	endpoints := []string{"http://localhost:2379"}
	config := clientv3.Config{
		Endpoints:   endpoints,
		DialTimeout: 5 * time.Second,
	}
	client, err := clientv3.New(config)
	if err != nil {
		log.Fatal(err)
	}
	defer client.Close()

	session, err := concurrency.NewSession(client)
	if err != nil {
		log.Fatal(err)
	}
	defer session.Client()

	mutex := concurrency.NewMutex(session, "/my-lock")

	err = mutex.Lock(context.Background())
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("success get lock.")
	time.Sleep(10 * time.Second)

	err = mutex.Unlock(context.Background())
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("success release lock.")
}
