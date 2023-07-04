package main

import (
	"fmt"
	"github.com/smartystreets/goconvey/convey"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestCase1(t *testing.T) {
	testCases := []struct {
		input  int
		output int
	}{
		{1, 2},
		{2, 4},
		{3, 6},
		{4, 8},
	}

	for _, testCase := range testCases {
		result := Case1(testCase.input)
		assert.Equal(t, result, testCase.output)
		//if result != testCase.output {
		//	t.Errorf("Expected %d, but got %d for input %d", tc.output, result, tc.input)
		//}
	}
}

func TestCheckUrl1(t *testing.T) {
	convey.Convey("TestCheckTeachUrl", t, func() {
		ok := CheckUrl("learnku.com")
		convey.So(ok, convey.ShouldBeTrue)
	})
}

func TestCheckUrl(t *testing.T) {
	convey.Convey("TestCheckUrl", t, func() {
		convey.Convey("TestCheckUrl true", func() {
			ok := CheckUrl("learnku.com")
			convey.So(ok, convey.ShouldBeTrue)
		})

		convey.Convey("TestCheckUrl false", func() {
			ok := CheckUrl("xxxxxxx.com")
			convey.So(ok, convey.ShouldBeFalse)
		})
		convey.Convey("TestCheckTeachUrl nil", func() {
			ok := CheckUrl("")
			convey.So(ok, convey.ShouldBeFalse)
		})
	})
}

func TestCase2(t *testing.T) {
	slice := []int{0, 1, 2, 3}
	map1 := Case2(slice)
	for k, v := range map1 {
		fmt.Println(k, "-->", *v)
		assert.Equal(t, *v, slice[k])
	}
}

func TestEqualSlice(t *testing.T) {
	slice1 := []int{1, 2, 3}
	slice2 := []int{1, 2, 3}
	assert.True(t, EqualSlice(slice1, slice2))
}
