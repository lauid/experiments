TARGET=plugin2.so
# CC=arm-linux-gnueabi-gcc
CC=/opt/phytec-yogurt/BSP-Yocto-i.MX6UL-PD19.1.2/sysroots/x86_64-phytecsdk-linux/usr/bin/arm-phytec-linux-gnueabi/arm-phytec-linux-gnueabi-gcc 
all:
	go build -buildmode=plugin -o ${TARGET} plugin2.go
arm:
	CC=${CC} \
	CGO_ENABLED=1 GOOS=linux GOARCH=arm GOARM=7  \
	go build -buildmode=plugin -o ${TARGET} plugin2.go
clean:
	rm -rf ${TARGET}
