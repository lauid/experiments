all:
	GOOS=linux GOARCH=arm GOARM=7 go build -o serial-arm *.go

win:
	GOOS=windows GOARCH=amd64 go build -o serial-win.exe *.go

clean:
	rm -rf serial-arm serial-win.*
