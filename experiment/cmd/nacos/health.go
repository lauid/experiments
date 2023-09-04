package main

import (
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
)

func main() {
	// 创建Nacos客户端配置
	clientConfig := constant.ClientConfig{
		NamespaceId: "433c3b86-7cf2-4e2b-8995-c5ef9cf45b7b", // 可选，如果有命名空间则填写
		TimeoutMs:   5000,
	}

	// 创建Nacos服务器配置
	serverConfigs := []constant.ServerConfig{
		{
			ContextPath: "/nacos",
			IpAddr:      "127.0.0.1",
			Port:        8848,
			Scheme:      "http",
		},
	}

	// 创建Nacos客户端
	client, err := clients.NewNamingClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfigs,
		})

	if err != nil {
		log.Fatal(err)
	}

	// 关闭Nacos客户端
	defer client.CloseClient()

	serviceName := "my-service"
	ip := "127.0.0.1"
	port := uint64(8080)

	success, err := client.RegisterInstance(vo.RegisterInstanceParam{
		Ip:          ip,
		Port:        port,
		ServiceName: serviceName,
		Weight:      10,
		Enable:      true,
		Healthy:     true, // 设置服务实例为健康状态
		Ephemeral:   true, //ephemeral为true对应的是服务健康检查模式中的 client 模式,即是agent 上报模式， ephemeral为为false对应的是 server 模式，即是服务端主动检测模式。
	})

	if err != nil {
		log.Fatal(err)
	}

	if success {
		log.Println("Service registered successfully")
	} else {
		log.Println("Failed to register service")
	}

	// 模拟服务运行
	for {
		// 注册信号处理函数，以便在收到中断信号时注销服务实例
		signals := make(chan os.Signal, 1)
		signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM)

		select {
		case <-signals:
			// 收到中断信号，注销服务实例
			_, err := client.DeregisterInstance(vo.DeregisterInstanceParam{
				Ip:          ip,
				Port:        port,
				ServiceName: serviceName,
			})

			if err != nil {
				log.Printf("Failed to deregister service: %s\n", err)
			} else {
				log.Println("Service deregistered successfully")
			}
			return

		case <-time.After(time.Minute):
			// 运行服务一分钟后停止
			_, err := client.DeregisterInstance(vo.DeregisterInstanceParam{
				Ip:          ip,
				Port:        port,
				ServiceName: serviceName,
			})

			if err != nil {
				log.Printf("Failed to deregister service: %s\n", err)
			} else {
				log.Println("Service deregistered successfully")
			}
			return
		}
	}
}
