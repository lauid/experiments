package main

func CheckUrl(url string) bool {
	var urlList = [2]string{"learnku.com", "xdcute.com"}
	for v := range urlList {
		if urlList[v] == url {
			return true
		}
	}
	return false
}

func Case1(a int) int {
	return a * 2
}

func main() {

}
