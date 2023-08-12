package main

import (
	"bytes"
	"context"
	"crypto/tls"
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
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"syscall"
	"time"
)

type Product struct {
	ID       string    `bson:"_id,omitempty" json:"id,omitempty"`
	Name     string    `bson:"name" json:"name,omitempty"`
	CreateAt time.Time `bson:"create_at" json:"create_at,omitempty"`
	UpdateAt time.Time `bson:"update_at" json:"update_at,omitempty"`
}

type SVC struct {
	mongoClient *mongo.Client
	esClient    *elasticsearch.Client
}

func (s *SVC) stopServer() error {
	if err := s.mongoClient.Disconnect(context.TODO()); err != nil {
		log.Fatal(err)
		return err
	}

	return nil
}

var svc *SVC

func main() {
	//defer func() {
	//	if err := recover(); err != nil {
	//		fmt.Println(err)
	//	}
	//}()
	defer fmt.Println("exited...")

	ctx, cancel := context.WithCancel(context.Background())
	g, ctx := errgroup.WithContext(ctx)
	fmt.Println("1")

	svc = &SVC{}
	defer svc.stopServer()
	err := mongoSetUp(svc)
	if err != nil {
		log.Fatal(err)
		return
	}
	err = esSetUp(svc)
	if err != nil {
		log.Fatal(err)
		return
	}
	fmt.Println(svc)

	g.Go(func() error {
		return syncEsTimer(ctx)
	})

	g.Go(func() error {
		return batchInsertMongo(ctx)
	})

	g.Go(func() error {
		return StopSignalHandler(ctx, cancel, svc)
	})

	if err := g.Wait(); err != nil {
		fmt.Printf("Authentication service terminated: %s\n", err)
	}
	fmt.Println("exit..........")
}
func esSetUp(svc *SVC) error {
	defer fmt.Println("esSetup exit.")
	fmt.Println("esSetup")
	//es
	cfg := elasticsearch.Config{
		Addresses: []string{
			"https://192.168.56.11:9200",
		},
		Username: "elastic",
		Password: "kzY8a5gFiN=*QhVpjPX2",
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}
	client, err := elasticsearch.NewClient(cfg)
	if err != nil {
		log.Fatalf("error creating the client:%s", err)
		return err
	}
	svc.esClient = client
	return nil
}

func mongoSetUp(svc *SVC) error {
	defer fmt.Println("mongosetup end")
	fmt.Println("mongosetup")
	//mongo
	clientOptions := options.Client().ApplyURI("mongodb://192.168.56.11:27017/?connect=direct")
	mongoClient, err := mongo.Connect(context.TODO(), clientOptions)
	if err != nil {
		log.Fatal(err)
		return err
	}
	err = mongoClient.Ping(context.TODO(), nil)
	if err != nil {
		log.Fatal(err)
		return err
	}
	svc.mongoClient = mongoClient
	return nil
}

func batchInsertMongo(ctx context.Context) error {
	ticker := time.NewTicker(2 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			fmt.Println("mongo operation cancel.")
			return ctx.Err()

		case <-ticker.C:
			fmt.Println("ticker.insert..")
			fmt.Println(time.Now().Format(time.RFC3339))
			collection := svc.mongoClient.Database("lauid").Collection("myCollection")
			var docs []interface{}
			for i := 0; i < 20; i++ {
				docs = append(docs, Product{Name: "name" + strconv.Itoa(rand.Intn(20)), CreateAt: time.Now(), UpdateAt: time.Now()})
			}
			fmt.Println("insertMany:", docs)
			_, err := collection.InsertMany(context.TODO(), docs)
			if err != nil {
				log.Fatal(err)
				return err
			}
		}
	}
}

func StopSignalHandler(ctx context.Context, cancel context.CancelFunc, svc *SVC) error {
	c := make(chan os.Signal, 1)
	signal.Notify(c, syscall.SIGINT, syscall.SIGTERM, syscall.SIGABRT)
	select {
	case sig := <-c:
		fmt.Println("service get signal: ", sig)
		defer cancel()
		return fmt.Errorf("service shutdown by signal: %v", sig)
	case <-ctx.Done():
		return ctx.Err()
	}
}

func getDocs(ctx context.Context) <-chan Product {
	pChan := make(chan Product, 2)
	go func() {
		defer close(pChan)

		collection := svc.mongoClient.Database("lauid").Collection("myCollection")

		filter := bson.M{"create_at": bson.M{"$gte": time.Date(2023, 1, 1, 0, 0, 0, 0, time.UTC)}}
		cursor, err := collection.Find(context.TODO(), filter)
		if err != nil {
			log.Fatal(err)
			return
		}
		defer cursor.Close(ctx)

		for cursor.Next(ctx) {
			var result Product
			err := cursor.Decode(&result)
			if err != nil {
				log.Fatal(err)
			}
			//fmt.Println(result)
			pChan <- result

			if ctx.Err() != nil{
				break
			}
		}

		if err := cursor.Err(); err != nil {
			log.Fatal(err)
		}
	}()

	return pChan
}

func syncEsTimer(ctx context.Context) error {
	fmt.Println("syncTimer")
	t := time.NewTicker(2 * time.Second)
	defer t.Stop()

	for {
		select {
		case <-ctx.Done():
			fmt.Println("es operation cancel.")
			return ctx.Err()
		case <-t.C:
			fmt.Println("sync...")
			err := syncEs(ctx)
			if err != nil {
				return err
			}
		}
	}
}

func syncEs(ctx context.Context) error {
	//curl -ks https://127.0.0.1:9200/_cat/health -u elastic:VOFdLYFHAjlDSCSEO6c=

	indexName := "product_index1"
	err := createIndex(svc.esClient, indexName)
	if err != nil {
		return err
	}
	pChan := getDocs(ctx)

	fmt.Println("sync es")
	for doc := range pChan {
		err := indexDocument(svc.esClient, indexName, doc)
		if err != nil {
			return err
		}
	}

	return nil
}

func createIndex(client *elasticsearch.Client, index string) error {
	existRequest := esapi.IndicesExistsRequest{
		Index: []string{index},
	}
	do, err := existRequest.Do(context.Background(), client)
	if err != nil {
		return err
	}
	defer do.Body.Close()
	if do.IsError() {
		log.Printf("Error exist index: %s\n", do.Status())
	}
	log.Println("exist res:", do)
	if do.StatusCode == http.StatusOK {
		return nil
	}

	request := esapi.IndicesCreateRequest{
		Index: index,
	}
	res, err := request.Do(context.Background(), client)
	if err != nil {
		log.Fatal(err)
		return err
	}
	defer res.Body.Close()
	if res.IsError() {
		log.Fatalf("Error creating index: %s", res.Status())
	}

	var r map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&r); err != nil {
		log.Fatalf("Error parsing the response body:%s", err)
		return err
	}

	fmt.Println("index created:", r)
	return nil
}

// 写入文档
func indexDocument(es *elasticsearch.Client, indexName string, doc Product) error {
	// 序列化文档数据
	docBytes, err := json.Marshal(doc)
	if err != nil {
		log.Fatalf("Error encoding document: %s", err)
		return err
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
		return err
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
		return err
	}
	return nil
}
