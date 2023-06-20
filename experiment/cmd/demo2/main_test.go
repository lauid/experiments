package main

import (
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
