TARGET=plugin2.so
all:
	go build -buildmode=plugin -o ${TARGET} plugin2.go
arm1:	
	CC=arm-linux-gnueabihf-gcc CGO_ENABLED=1 GOARCH=arm GOARM=7 go build -buildmode=plugin -o plugins.so plugin2.go
arm:
	CGO_ENABLED=0 GOOS=linux GOARCH=arm GOARM=7 go build -buildmode=plugin -o ${TARGET} plugin2.go
clean:
	rm -rf ${TARGET}
