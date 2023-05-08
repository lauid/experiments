package main

import (
	"fmt"
)

func swap[T any](a, b *T) {
	*a, *b = *b, *a
}

func MapKeys[Key comparable, Val any](m map[Key]Val) []Key {
	keys := make([]Key, len(m))

	for k := range m {
		keys = append(keys, k)
	}

	return keys
}

func main1() {
	a, b := 1, 2
	swap(&a, &b)
	fmt.Println("a:", a, " b:", b)

	aa, bb := "a", "b"
	swap(&aa, &bb)
	fmt.Println("aa:", aa, " bb:", bb)
}

func main() {
}
