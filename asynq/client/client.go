package main

import (
	"asynq/internal/task"
	_ "asynq/internal/task"
	"github.com/hibiken/asynq"
	"log"
	"time"
)

// client.go
// client.go
func main() {
	client := asynq.NewClient(asynq.RedisClientOpt{Addr: "192.168.56.56:6379"})

	fakeUserId := int(time.Now().Unix())
	t1, err := task.NewWelcomeEmailTask(fakeUserId)
	if err != nil {
		log.Fatal(err)
	}

	t2, err := task.NewReminderEmailTask(fakeUserId)
	if err != nil {
		log.Fatal(err)
	}

	// Process the task immediately.
	info, err := client.Enqueue(t1)
	if err != nil {
		log.Fatal(err)
	}
	log.Printf(" [*] Successfully enqueued task: %+v", info)

	// Process the task 24 hours later.
	info, err = client.Enqueue(t2, asynq.ProcessIn(1*time.Minute))
	if err != nil {
		log.Fatal(err)
	}
	log.Printf(" [*] Successfully enqueued task: %+v", info)
}
