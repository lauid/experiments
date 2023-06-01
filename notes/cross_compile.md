
```
GOOS=linux && \ 
GOARCH=arm64 && \
GOARM=7 && \
CGO_ENABLED=1 && \
CC=arm-linux-gnueabihf-gcc && \
CXX=arm-linux-gnueabihf-g++ && \
AR=arm-linux-gnueabihf-ar && \
go build -o xxx *.go
```
GOOS=linux GOARCH=arm64 GOARM=7 go build -o xxx *.go

GOOS=windows GOARCH=ard64 go build -o xxx *.go


go install github.com/mitchellh/gox@latest
