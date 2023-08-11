package main

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"github.com/elastic/go-elasticsearch/v8"
	"github.com/elastic/go-elasticsearch/v8/esapi"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"golang.org/x/sync/errgroup"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"strconv"
	"syscall"
	"time"
)

type Product struct {
	ID       string    `bson:"_id,omitempty"`
	Name     string    `bson:"name"`
	CreateAt time.Time `bson:"create_at"`
	UpdateAt time.Time `bson:"update_at"`
}

type SVC struct {
	mongoClient *mongo.Client
	esClient    *elasticsearch.Client
}

var svc *SVC

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	g, ctx := errgroup.WithContext(ctx)

	defer func() {
		if err := recover(); err != nil {
			fmt.Println(err)
		}
	}()
	defer fmt.Println("aaaaaaaaaaaa.")
	defer time.Sleep(time.Second * 1)
	defer cancel()

	svc = &SVC{}

	//mongo
	clientOptions := options.Client().ApplyURI("mongodb://192.168.56.11:27017")
	mongoClient, err := mongo.Connect(context.TODO(), clientOptions)
	if err != nil {
		log.Fatal(err)
	}
	err = mongoClient.Ping(context.TODO(), nil)
	if err != nil {
		log.Fatal(err)
	}
	svc.mongoClient = mongoClient
	defer func() {
		if err := mongoClient.Disconnect(context.TODO()); err != nil {
			log.Fatal(err)
		}
	}()

	//es
	cfg := elasticsearch.Config{
		Addresses: []string{
			"https://192.168.56.11:9200",
		},
		Username: "elastic",
		Password: "VOFdLYFHAjlDSCSEO6c=",
	}
	client, err := elasticsearch.NewClient(cfg)
	if err != nil {
		log.Fatalf("error creating the client:%s", err)
		return
	}
	svc.esClient = client

	g.Go(func() error {
		return syncEsTimer(ctx)
	})

	g.Go(func() error {
		ticker := time.NewTicker(2 * time.Second)
		for range ticker.C {
			fmt.Println(time.Now().Format(time.RFC3339))
			collection := svc.mongoClient.Database("lauid").Collection("myCollection")
			var docs []interface{}
			for i := 0; i < 20; i++ {
				docs = append(docs, Product{Name: "name" + strconv.Itoa(rand.Intn(20)), CreateAt: time.Now(), UpdateAt: time.Now()})
			}
			fmt.Println(docs)
			_, err = collection.InsertMany(context.TODO(), docs)
			if err != nil {
				log.Fatal(err)
				return err
			}
		}
		return nil
	})

	g.Go(func() error {
		return StopSignalHandler(ctx, cancel)
	})

	if err := g.Wait(); err != nil {
		fmt.Printf("Authentication service terminated: %s", err)
	}
}

func StopSignalHandler(ctx context.Context, cancel context.CancelFunc) error {
	var err error
	c := make(chan os.Signal, 2)
	signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
	select {
	case sig := <-c:
		defer cancel()
		fmt.Printf("service shutdown by signal: %s", sig)
		return err
	case <-ctx.Done():
		return nil
	}
}

func getDocs() <-chan Product {
	pChan := make(chan Product, 2)
	go func() {
		defer close(pChan)

		collection := svc.mongoClient.Database("lauid").Collection("myCollection")

		filter := bson.M{"create_at": bson.M{"$gte": time.Date(2023, 1, 1, 0, 0, 0, 0, time.UTC)}}
		cursor, err := collection.Find(context.TODO(), filter)
		if err != nil {
			log.Fatal(err)
		}
		defer cursor.Close(context.TODO())

		for cursor.Next(context.TODO()) {
			var result Product
			err := cursor.Decode(&result)
			if err != nil {
				log.Fatal(err)
			}
			fmt.Println(result)
			pChan <- result
		}

		if err := cursor.Err(); err != nil {
			log.Fatal(err)
		}
	}()

	return pChan
}

func syncEsTimer(ctx context.Context) error {
	t := time.Tick(1 * time.Second)
	for {
		select {
		case <-ctx.Done():
			fmt.Println("operation cancel.")
			return nil
		case <-t:
			fmt.Println("sync...")
			//syncEs()
		}
	}

	return nil
}

func syncEs() {
	//curl -ks https://127.0.0.1:9200/_cat/health -u elastic:VOFdLYFHAjlDSCSEO6c=

	indexName := "product_index"
	createIndex(svc.esClient, indexName)
	pChan := getDocs()

	for doc := range pChan {
		indexDocument(svc.esClient, indexName, doc)
	}
}

func createIndex(client *elasticsearch.Client, index string) {
	request := esapi.IndicesCreateRequest{
		Index: index,
	}
	res, err := request.Do(context.Background(), client)
	if err != nil {
		return
	}
	defer res.Body.Close()
	if res.IsError() {
		log.Fatalf("Error creating index: %s", res.Status())
	}

	var r map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&r); err != nil {
		log.Fatalf("Error parsing the response body:%s", err)
	}

	fmt.Println("index created:", r)
}

// 写入文档
func indexDocument(es *elasticsearch.Client, indexName string, doc Product) {
	// 序列化文档数据
	docBytes, err := json.Marshal(doc)
	if err != nil {
		log.Fatalf("Error encoding document: %s", err)
	}

	// 创建索引请求
	req := esapi.IndexRequest{
		Index:      indexName,
		DocumentID: doc.ID,
		Body:       bytes.NewReader(docBytes),
		Refresh:    "true",
	}

	// 发送请求
	res, err := req.Do(context.Background(), es)
	if err != nil {
		log.Fatalf("Error indexing document: %s", err)
	}
	defer res.Body.Close()

	// 检查响应状态
	if res.IsError() {
		log.Fatalf("Error indexing document: %s", res.Status())
	}

	// 解析响应
	var r map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&r); err != nil {
		log.Fatalf("Error parsing the response body: %s", err)
	}

	// 输出响应结果
	fmt.Printf("Indexed document ID: %s\n", r["_id"])
}
