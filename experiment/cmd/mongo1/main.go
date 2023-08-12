package main

import (
	"bytes"
	"context"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"sync"
	"time"

	elasticsearch "github.com/elastic/go-elasticsearch/v8"
	"github.com/elastic/go-elasticsearch/v8/esapi"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func main() {
	// 连接 MongoDB
	mongoClient, err := mongo.Connect(context.Background(), options.Client().ApplyURI("mongodb://localhost:27017"))
	if err != nil {
		log.Fatal(err)
	}
	defer mongoClient.Disconnect(context.Background())


	// 创建 Elasticsearch 客户端
	cfg := elasticsearch.Config{
		Addresses: []string{"https://localhost:9200"},
		Username: "elastic",
		Password: "XD7eytxZ_TfAa27Y0z0q",
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}
	esClient, err := elasticsearch.NewClient(cfg)
	if err != nil {
		log.Fatal(err)
	}

	// 等待组用于协程同步
	var wg sync.WaitGroup
	wg.Add(2)

	// 协程1：生成 MongoDB 假数据并写入
	go func() {
		defer wg.Done()

		mongoCollection := mongoClient.Database("mydb").Collection("products")

		for i := 0; i < 100; i++ {
			product := bson.M{
				"name":        generateProductName(),
				"description": generateProductDescription(),
				"price":       rand.Float64() * 100,
				"created_at":  time.Now(),
			}
			fmt.Println("insert:",product)

			_, err := mongoCollection.InsertOne(context.Background(), product)
			if err != nil {
				log.Println("1,",err)
			}

			time.Sleep(time.Millisecond * 100)
		}
	}()

	// 协程2：从 MongoDB 读取数据并写入 Elasticsearch
	go func() {
		defer wg.Done()

		mongoCollection := mongoClient.Database("mydb").Collection("products")

		esIndex := "products"

		for {
			cursor, err := mongoCollection.Find(context.Background(), bson.M{})
			if err != nil {
				log.Println("2,",err)
				time.Sleep(time.Second)
				continue
			}

			var products []bson.M
			if err := cursor.All(context.Background(), &products); err != nil {
				log.Println("3,",err)
			}

			for _, product := range products {
				product := product
				fmt.Println("product:",product)
				indexReq := esapi.IndexRequest{
					Index:      esIndex,
					DocumentID: product["_id"].(primitive.ObjectID).Hex(),
					Body:       bytes.NewReader(bsonToJSON(product)),
					Refresh:    "true",
				}

				res, err := indexReq.Do(context.Background(), esClient)
				if err != nil {
					log.Println("4,",err)
				}
				fmt.Println(res)

				time.Sleep(time.Millisecond * 100)
			}

			time.Sleep(time.Second * 5)
		}
	}()

	// 等待协程完成
	wg.Wait()
}

// 生成商品名称
func generateProductName() string {
	adjectives := []string{"Amazing", "Fantastic", "Awesome", "Incredible"}
	nouns := []string{"Product", "Item", "Good", "Thing"}

	return adjectives[rand.Intn(len(adjectives))] + " " + nouns[rand.Intn(len(nouns))]
}

// 生成商品描述
func generateProductDescription() string {
	descriptions := []string{"High quality", "Durable", "Trendy", "Fashionable"}

	return descriptions[rand.Intn(len(descriptions))]
}

// 将 BSON 转为 JSON 字符串
func bsonToJSON(data bson.M) []byte{
	doc, err := bson.MarshalExtJSON(data, false, false)
	if err != nil {
		log.Println(err)
		return []byte("")
	}
	fmt.Println(string(json.RawMessage(doc)))
	return doc
}
