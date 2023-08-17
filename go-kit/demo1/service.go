package demo1

import (
	"context"
	"time"
)

type Service interface {
	Status(ctx context.Context) (string, error)
	Get(ctx context.Context) (string, error)
	Validate(ctx context.Context, date string) (bool, error)
}

type dateService struct {
}

func NewService() dateService {
	return dateService{}
}

func (s dateService) Status(ctx context.Context) (string, error) {
	return "ok", nil
}

func (s dateService) Get(ctx context.Context) (string, error) {
	now := time.Now()
	return now.Format(time.RFC3339), nil
}

func (s dateService) Validate(ctx context.Context, date string) (bool, error) {
	_, err := time.Parse(time.RFC3339, date)
	if err != nil {
		return false, err
	}
	return true, nil
}
