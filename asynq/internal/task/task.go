package task

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/hibiken/asynq"
)

// A list of task types.
const (
	TypeWelcomeEmail  = "email:welcome"
	TypeReminderEmail = "email:reminder"
)

// Task payload for any email related tasks.
type emailTaskPayload struct {
	// ID for the email recipient.
	UserID int
}

func NewWelcomeEmailTask(id int) (*asynq.Task, error) {
	payload, err := json.Marshal(emailTaskPayload{UserID: id})
	if err != nil {
		return nil, err
	}
	return asynq.NewTask(TypeWelcomeEmail, payload), nil
}

func NewReminderEmailTask(id int) (*asynq.Task, error) {
	payload, err := json.Marshal(emailTaskPayload{UserID: id})
	if err != nil {
		return nil, err
	}
	return asynq.NewTask(TypeReminderEmail, payload), nil
}

func HandleWelcomeEmailTask(ctx context.Context, t *asynq.Task) error {
	startTime := time.Now()
	defer func() {
		fmt.Printf("time duraiton:%v\n", time.Since(startTime))
	}()
	var p emailTaskPayload
	if err := json.Unmarshal(t.Payload(), &p); err != nil {
		return err
	}
	//resp, err := http.Get("https://yesno.wtf/api")
	req, err := http.NewRequest("GET", "https://yesno.wtf/api", nil)
	if err != nil {
		return err
	}
	resp, cleanup, err := httpRequest(req)
	if err != nil {
		return err
	}
	defer cleanup()

	//resp, err := http.Get("https://httpbin.org/get")
	//if err != nil {
	//	return err
	//}
	//defer resp.Body.Close()
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	fmt.Println(string(body))
	log.Printf(" [*] Send Welcome Email to User %d", p.UserID)
	return nil
}

func HandleReminderEmailTask(ctx context.Context, t *asynq.Task) error {
	var p emailTaskPayload
	if err := json.Unmarshal(t.Payload(), &p); err != nil {
		return err
	}
	log.Printf(" [*] Send Reminder Email to User %d", p.UserID)

	body := strings.NewReader(`{"a":1,"b":2}`)
	req, err := http.NewRequest("POST", "https://httpbin.org/post", body)
	if err != nil {
		return err
	}
	resp, f, err := httpRequest(req)
	if err != nil {
		return err
	}
	defer f()
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	fmt.Println(string(respBody))

	return nil
}

func httpRequest(req *http.Request) (*http.Response, func(), error) {
	// 创建一个自定义的 HTTP 客户端
	client := &http.Client{
		Timeout: time.Second * 10, // 设置超时时间为 10 秒
	}

	// 使用自定义的客户端发起请求
	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("请求失败:", err)
		return nil, nil, err
	}
	cleanup := func() {
		defer resp.Body.Close()
	}

	return resp, cleanup, nil
}
