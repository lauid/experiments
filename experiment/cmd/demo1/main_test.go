package main

import (
	"container/list"
	"encoding/json"
	"experiment/internal/utils"
	"fmt"
	"github.com/stretchr/testify/assert"
	"go.uber.org/goleak"
	"log"
	"math/rand"
	"net/http"
	"os/exec"
	"sync"
	"testing"
)

func TestCase3(t *testing.T) {
	check := func(v int) bool {
		return v > (10 - rand.Intn(10))
	}
	fmt.Println(utils.IF(check(5), "A", "B"))
}

func TestCommand3(t *testing.T) {
	// Print Go Version
	cmdOutput, err := exec.Command("go", "version").Output()
	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("%s", cmdOutput)
	assert.NotEmpty(t, cmdOutput)
}

func TestCase1(t *testing.T) {
	testCases := []struct {
		input  int
		output int
	}{
		{1, 2},
		{3, 6},
		{5, 10},
	}

	for _, tc := range testCases {
		result := Case1(tc.input)
		if result != tc.output {
			t.Errorf("Expected %d, but got %d for input %d", tc.output, result, tc.input)
		}
	}
}

func TestCase2(t *testing.T) {
	testCases := []struct {
		input  int
		output int
	}{
		{1, 2},
		{3, 6},
		{5, 10},
	}

	for _, tc := range testCases {
		tc := tc // 创建 tc 的副本以便在子测试中使用
		t.Run(fmt.Sprintf("input=%d", tc.input), func(t *testing.T) {
			result := Case2(tc.input)
			if result != tc.output {
				t.Errorf("Expected %d, but got %d", tc.output, result)
			}
		})
	}
}

func Test2(t *testing.T) {
	defer goleak.VerifyNone(t)
}

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

func TestHttpGet(t *testing.T) {
	url := "https://yesno.wtf/api"
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println(err)
	}
	defer resp.Body.Close()

	var res struct {
		Answer string `json:"answer"`
		Forced bool   `json:"forced"`
		Image  string `json:"image"`
	}
	err = json.NewDecoder(resp.Body).Decode(&res)
	assert.Nil(t, err)
	fmt.Printf("%+v\n", res)
}

func TestHttpGet2(t *testing.T) {
	url := "https://yesno.wtf/api"
	var wg sync.WaitGroup
	type Answer struct {
		Answer string `json:"answer"`
		Forced bool   `json:"forced"`
		Image  string `json:"image"`
	}
	var answers []Answer
	n := 10
	for i := 0; i < n; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			resp, err := http.Get(url)
			if err != nil {
				fmt.Println(err)
			}
			defer resp.Body.Close()

			var answer Answer
			err = json.NewDecoder(resp.Body).Decode(&answer)
			if err != nil {
				fmt.Println(err)
				return
			}
			answers = append(answers, answer)
		}()
	}
	wg.Wait()

	fmt.Printf("%+v\n", answers)
	assert.Equal(t, n, len(answers))
}