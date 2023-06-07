package main

type Student struct {
	name string
	age  int
}

func NewStudent(age int) func(name string) Student {
	return func(name string) Student {
		return Student{
			name: name,
			age:  age,
		}
	}
}

// 我们可以清晰的看出，我们将年龄作为一个root，然后name是child ，那么就是将 year和name解耦了，我们使用子函数去创建name这child选项。
//
//所以可以看到上文中，我们创建了16岁的红蓝绿，17岁的红蓝绿，和18岁的红蓝绿 我们将year和name解耦了。
func main() {

	// 16岁的分成一组
	student16 := NewStudent(16)
	student16("red")
	student16("green")
	student16("blue")
	// 17岁的分成一组
	student17 := NewStudent(17)
	student17("red")
	student17("green")
	student17("blue")
	// 18岁的分成一组
	student18 := NewStudent(18)
	student18("red")
	student18("green")
	student18("blue")

}
