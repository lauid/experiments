package main

import "fmt"

type IStudentStrategy interface {
	do(i, j int) int
}

type Student struct {
	strategyOne IStudentStrategy
}

func (s *Student) SetStrategy(strategyOne IStudentStrategy) {
	s.strategyOne = strategyOne
}

func (s *Student) DoStrategy(i, j int) int {
	return s.strategyOne.do(i, j)
}

type add struct {
}

func (a add) do(i, j int) int {
	return i + j
}

type minus struct {
}

func (m minus) do(i, j int) int {
	return i - j
}

func main() {
	stu := new(Student)
	stu.SetStrategy(&add{})
	resultAdd := stu.DoStrategy(1, 2)
	fmt.Println(resultAdd)

	stu.SetStrategy(&minus{})
	resultMinus := stu.DoStrategy(1, 2)
	fmt.Println(resultMinus)
}
