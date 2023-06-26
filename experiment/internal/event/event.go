package event

import (
	"context"
	"fmt"
	"sync"
)

type Event struct {
	Topic string
	Val   interface{}
}

type IObserver interface {
	OnChange(ctx context.Context, e *Event) error
}

// IEventBus 事件总线 EventBus 需要实现 Subscribe 和 Unsubscribe 方法暴露给观察者，用于新增或删除订阅关系，
type IEventBus interface {
	Subscribe(topic string, o IObserver)
	UnSubscribe(topic string, o IObserver)
	Publish(ctx context.Context, e *Event)
}

type BaseObserver struct {
	name string
}

func NewBaseObserver(name string) *BaseObserver {
	return &BaseObserver{
		name: name,
	}
}

func (b *BaseObserver) OnChange(ctx context.Context, e *Event) error {
	fmt.Printf("observer:%s, event key: %s, event val: %v", b.name, e.Topic, e.Val)
	return nil
}

type BaseEventBus struct {
	mux       sync.RWMutex
	observers map[string]map[IObserver]struct{}
}

func NewBaseEventBus() BaseEventBus {
	return BaseEventBus{
		observers: make(map[string]map[IObserver]struct{}),
	}
}

func (b *BaseEventBus) Subscribe(topic string, o IObserver) {
	b.mux.Lock()
	defer b.mux.Unlock()

	_, ok := b.observers[topic]
	if !ok {
		b.observers[topic] = make(map[IObserver]struct{})
	}
	b.observers[topic][o] = struct{}{}
}

func (b *BaseEventBus) UnSubscribe(topic string, o IObserver) {
	b.mux.Lock()
	defer b.mux.Unlock()
	delete(b.observers[topic], o)
}

// SyncEventBus 在同步模式下，EventBus 在接受到变更事件 Event 时，会根据事件类型 Topic 匹配到对应的观察者列表 observers，然后采用串行遍历的方式分别调用 Observer.OnChange 方法对每个观察者进行通知，并对处理流程中遇到的错误进行聚合，放到 handleErr 方法中进行统一的后处理.
type SyncEventBus struct {
	BaseEventBus
}

func NewSyncEventBus() *SyncEventBus {
	return &SyncEventBus{
		BaseEventBus: NewBaseEventBus(),
	}
}

func (s *SyncEventBus) handleErr(ctx context.Context, errs map[IObserver]error) {
	for o, err := range errs {
		// 处理 publish 失败的 observer
		fmt.Printf("observer: %v, err: %v", o, err)
	}
}

func (s *SyncEventBus) Publish(ctx context.Context, e *Event) {
	s.mux.RLock()
	subscribers := s.observers[e.Topic]
	s.mux.RUnlock()

	errs := make(map[IObserver]error)
	for subscriber := range subscribers {
		if err := subscriber.OnChange(ctx, e); err != nil {
			errs[subscriber] = err
		}
	}

	s.handleErr(ctx, errs)
}

type observerWithErr struct {
	o   IObserver
	err error
}

type AsyncEventBus struct {
	BaseEventBus
	errC chan *observerWithErr
	ctx  context.Context
	stop context.CancelFunc
}

func NewAsyncEventBus() *AsyncEventBus {
	aBus := AsyncEventBus{
		BaseEventBus: NewBaseEventBus(),
	}

	aBus.ctx, aBus.stop = context.WithCancel(context.Background())

	go aBus.handleErr()

	return &aBus
}

func (a *AsyncEventBus) Stop() {
	a.stop()
}

func (a *AsyncEventBus) Publish(ctx context.Context, e *Event) {
	a.mux.RLock()
	suscribers := a.observers[e.Topic]
	a.mux.RUnlock()

	for subscriber := range suscribers {
		subscriber := subscriber

		go func() {
			if err := subscriber.OnChange(ctx, e); err != nil {
				select {
				case <-a.ctx.Done():
				case a.errC <- &observerWithErr{o: subscriber, err: err}:
				}
			}
		}()
	}
}

func (a *AsyncEventBus) handleErr() {
	for {
		select {
		case <-a.ctx.Done():
			return
		case resp := <-a.errC:
			fmt.Printf("observer: %v, err: %v", resp.o, resp.err)
		}

	}
}
