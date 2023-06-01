package main

import (
	"fmt"
	"strconv"
)

func MapStrToStr(str []string, fn func(str string) string) []string {
	var ma []string
	for _, value := range str {
		ma = append(ma, fn(value))
	}

	return ma
}

func MapStrToInt(str []string, fn func(str string) int) []int {
	var ma []int
	for _, value := range str {
		ma = append(ma, fn(value))
	}

	return ma
}

func ReduceSum(str []string, fn func(str string) int) int {
	sum := 0
	for _, value := range str {
		sum += fn(value)
	}

	return sum
}

func Filter(str []string, fn func(str string) bool) []string {
	var ma []string

	for _, value := range str {
		if fn(value) {
			ma = append(ma, value)
		}
	}

	return ma
}

func main() {
	fmt.Println(MapStrToStr([]string{"A", "B"}, func(str string) string {
		return str + ";"
	}))

	fmt.Println(MapStrToInt([]string{"99", "88"}, func(str string) int {
		i, _ := strconv.ParseInt(str, 10, 0)
		return int(i)
	}))

	fmt.Println(ReduceSum([]string{"99", "88"}, func(str string) int {
		i, _ := strconv.ParseInt(str, 10, 0)
		return int(i)
	}))

	fmt.Println(Filter([]string{"99", "88", "77"}, func(str string) bool {
		i, _ := strconv.ParseInt(str, 10, 0)
		return int(i)%2 > 0
	}))
}
