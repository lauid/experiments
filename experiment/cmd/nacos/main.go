/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main

import (
	"fmt"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/naming_client"
	"time"

	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/model"
	"github.com/nacos-group/nacos-sdk-go/v2/util"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
)

func main() {
	//create ServerConfig
	sc := []constant.ServerConfig{
		*constant.NewServerConfig("127.0.0.1", 8848, constant.WithContextPath("/nacos")),
	}

	//create ClientConfig
	cc := *constant.NewClientConfig(
		constant.WithNamespaceId("433c3b86-7cf2-4e2b-8995-c5ef9cf45b7b"),
		constant.WithTimeoutMs(5000),
		constant.WithNotLoadCacheAtStart(true),
		constant.WithLogDir("/tmp/nacos/log"),
		constant.WithCacheDir("/tmp/nacos/cache"),
		constant.WithLogLevel("debug"),
	)

	// create naming client
	client, err := clients.NewNamingClient(
		vo.NacosClientParam{
			ClientConfig:  &cc,
			ServerConfigs: sc,
		},
	)

	if err != nil {
		panic(err)
	}

	//Register
	registerServiceInstance(client, vo.RegisterInstanceParam{
		Ip:          "127.0.0.1",
		Port:        8000,
		ServiceName: "demo.go",
		GroupName:   "group-a",
		ClusterName: "cluster-a",
		Weight:      10,
		Enable:      true,
		Healthy:     true,
		Ephemeral:   true,
		Metadata:    map[string]string{"idc": "shanghai"},
	})

	time.Sleep(30 * time.Second)
	//DeRegister
	deRegisterServiceInstance(client, vo.DeregisterInstanceParam{
		Ip:          "127.0.0.1",
		Port:        8000,
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Cluster:     "cluster-a",
		Ephemeral:   true, //it must be true
	})

	time.Sleep(30 * time.Second)
	//BatchRegister
	batchRegisterServiceInstance(client, vo.BatchRegisterInstanceParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Instances: []vo.RegisterInstanceParam{{
			Ip:          "192.168.16.11",
			Port:        8001,
			Weight:      10,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   true,
			ClusterName: "cluster-a",
			Metadata:    map[string]string{"idc": "shanghai"},
		}, {
			Ip:          "192.168.16.12",
			Port:        8001,
			Weight:      7,
			Enable:      true,
			Healthy:     true,
			Ephemeral:   true,
			ClusterName: "cluster-a",
			Metadata:    map[string]string{"idc": "shanghai"},
		}},
	})

	time.Sleep(6 * time.Second)

	//Get service with serviceName, groupName , clusters
	getService(client, vo.GetServiceParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Clusters:    []string{"cluster-a"},
	})
	time.Sleep(6 * time.Second)

	//SelectAllInstance
	//GroupName=DEFAULT_GROUP
	selectAllInstances(client, vo.SelectAllInstancesParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Clusters:    []string{"cluster-a"},
	})
	time.Sleep(6 * time.Second)

	//SelectInstances only return the instances of healthy=${HealthyOnly},enable=true and weight>0
	//ClusterName=DEFAULT,GroupName=DEFAULT_GROUP
	selectInstances(client, vo.SelectInstancesParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Clusters:    []string{"cluster-a"},
		HealthyOnly: true,
	})

	time.Sleep(6 * time.Second)
	//SelectOneHealthyInstance return one instance by WRR strategy for load balance
	//And the instance should be health=true,enable=true and weight>0
	//ClusterName=DEFAULT,GroupName=DEFAULT_GROUP
	selectOneHealthyInstance(client, vo.SelectOneHealthInstanceParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		Clusters:    []string{"cluster-a"},
	})
	time.Sleep(6 * time.Second)

	//Subscribe key=serviceName+groupName+cluster
	//Note:We call add multiple SubscribeCallback with the same key.
	subscribeParam := &vo.SubscribeParam{
		ServiceName: "demo.go",
		GroupName:   "group-a",
		SubscribeCallback: func(services []model.Instance, err error) {
			fmt.Printf("callback return services:%s \n\n", util.ToJsonString(services))
		},
	}
	subscribe(client, subscribeParam)

	//wait for client pull change from server
	time.Sleep(3 * time.Second)

	updateServiceInstance(client, vo.UpdateInstanceParam{
		Ip:          "10.0.0.11", //update ip
		Port:        8848,
		ServiceName: "demo.go",
		GroupName:   "group-a",
		ClusterName: "cluster-a",
		Weight:      10,
		Enable:      true,
		Healthy:     true,
		Ephemeral:   true,
		Metadata:    map[string]string{"idc": "beijing1"}, //update metadata
	})

	//wait for client pull change from server
	time.Sleep(30 * time.Second)
	// UnSubscribe
	unSubscribe(client, subscribeParam)

	//GeAllService will get the list of service name
	//NameSpace default value is public.If the client set the namespaceId, NameSpace will use it.
	//GroupName default value is DEFAULT_GROUP
	getAllService(client, vo.GetAllServiceInfoParam{
		GroupName: "group-a",
		PageNo:    1,
		PageSize:  10,
	})
}

func registerServiceInstance(client naming_client.INamingClient, param vo.RegisterInstanceParam) {
	success, err := client.RegisterInstance(param)
	if !success || err != nil {
		panic("RegisterServiceInstance failed!" + err.Error())
	}
	fmt.Printf("RegisterServiceInstance,param:%+v,result:%+v \n\n", util.ToJsonString(param), success)
}

func batchRegisterServiceInstance(client naming_client.INamingClient, param vo.BatchRegisterInstanceParam) {
	success, err := client.BatchRegisterInstance(param)
	if !success || err != nil {
		panic("BatchRegisterServiceInstance failed!" + err.Error())
	}
	fmt.Printf("BatchRegisterServiceInstance,param:%+v,result:%+v \n\n", util.ToJsonString(param), success)
}

func deRegisterServiceInstance(client naming_client.INamingClient, param vo.DeregisterInstanceParam) {
	success, err := client.DeregisterInstance(param)
	if !success || err != nil {
		panic("DeRegisterServiceInstance failed!" + err.Error())
	}
	fmt.Printf("DeRegisterServiceInstance,param:%+v,result:%+v \n\n", util.ToJsonString(param), success)
}

func updateServiceInstance(client naming_client.INamingClient, param vo.UpdateInstanceParam) {
	success, err := client.UpdateInstance(param)
	if !success || err != nil {
		panic("UpdateInstance failed!" + err.Error())
	}
	fmt.Printf("UpdateServiceInstance,param:%+v,result:%+v \n\n", util.ToJsonString(param), success)
}

func getService(client naming_client.INamingClient, param vo.GetServiceParam) {
	service, err := client.GetService(param)
	if err != nil {
		panic("GetService failed!" + err.Error())
	}
	fmt.Printf("GetService,param:%+v, result:%+v \n\n", util.ToJsonString(param), util.ToJsonString(service))
}

func selectAllInstances(client naming_client.INamingClient, param vo.SelectAllInstancesParam) {
	instances, err := client.SelectAllInstances(param)
	if err != nil {
		panic("SelectAllInstances failed!" + err.Error())
	}
	fmt.Printf("SelectAllInstance,param:%+v, result:%+v \n\n", param, util.ToJsonString(instances))
}

func selectInstances(client naming_client.INamingClient, param vo.SelectInstancesParam) {
	instances, err := client.SelectInstances(param)
	if err != nil {
		panic("SelectInstances failed!" + err.Error())
	}
	fmt.Printf("SelectInstances,param:%+v, result:%+v \n\n", param, util.ToJsonString(instances))
}

func selectOneHealthyInstance(client naming_client.INamingClient, param vo.SelectOneHealthInstanceParam) {
	instances, err := client.SelectOneHealthyInstance(param)
	if err != nil {
		panic("SelectOneHealthyInstance failed!")
	}
	fmt.Printf("SelectOneHealthyInstance,param:%+v, result:%+v \n\n", param, util.ToJsonString(instances))
}

func subscribe(client naming_client.INamingClient, param *vo.SubscribeParam) {
	client.Subscribe(param)
}

func unSubscribe(client naming_client.INamingClient, param *vo.SubscribeParam) {
	client.Unsubscribe(param)
}

func getAllService(client naming_client.INamingClient, param vo.GetAllServiceInfoParam) {
	service, err := client.GetAllServicesInfo(param)
	if err != nil {
		panic("GetAllService failed!")
	}
	fmt.Printf("GetAllService,param:%+v, result:%+v \n\n", param, util.ToJsonString(service))
}
