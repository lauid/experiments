package main

import (
	"container/list"
	"fmt"
	"github.com/stretchr/testify/assert"
	"math/rand"
	"testing"
)

func TestList1(t *testing.T) {
	l := list.New()
	e4 := l.PushBack(4)
	e1 := l.PushFront(1)
	l.InsertBefore(3, e4)
	l.InsertAfter(2, e1)

	counter := 0
	for e := l.Front(); e != nil; e = e.Next() {
		counter += 1
		if counter == 3 {
			assert.Equal(t, counter, e.Value, "链表节点值异常")
		}
	}
}

func TestList(t *testing.T) {
	listMark := make(map[int]*list.Element)
	l := list.New()
	elementNumber := 20
	var maxElement *list.Element
	for {
		if len(listMark) == elementNumber {
			break
		}
		v := rand.Intn(elementNumber) + 1
		if _, ok := listMark[v]; ok {
			continue
		}
		printList(l)
		if l.Front() == nil {
			listMark[v] = l.PushFront(v)
			continue
		}

		for e := l.Front(); e != nil; e = e.Next() {
			if e.Value.(int) > v {
				maxElement = e
				break
			}
		}
		if maxElement != nil {
			listMark[v] = l.InsertBefore(v, maxElement)
		} else {
			listMark[v] = l.PushBack(v)
		}
		maxElement = nil
	}
	fmt.Println(listMark)

	seq := 0
	for e := l.Front(); e != nil; e = e.Next() {
		seq += 1
		fmt.Println(e.Value)
		assert.Equal(t, seq, e.Value, "")
	}
}

func printList(l *list.List) {
	for e := l.Front(); e != nil; e = e.Next() {
		fmt.Print(e.Value)
		fmt.Print(".")
	}
	fmt.Println("")
}
