TARGET=demo2
# CC=arm-linux-gnueabi-gcc
#CFLAGS=-O2 -pipe -g -feliminate-unused-debug-types
#CC="/opt/phytec-yogurt/BSP-Yocto-i.MX6UL-PD19.1.2/sysroots/x86_64-phytecsdk-linux/usr/bin/arm-phytec-linux-gnueabi/arm-phytec-linux-gnueabi-gcc ${CFLAGS} -I=/usr/include/gstreamer-1.0 -I=/usr/include/glib-2.0 -I=/usr/lib/glib-2.0/include -L=/usr/lib -lgstreamer-1.0 -lgobject-2.0 -lglib-2.0"
#CXX=/opt/phytec-yogurt/BSP-Yocto-i.MX6UL-PD19.1.2/sysroots/x86_64-phytecsdk-linux/usr/bin/arm-phytec-linux-gnueabi/arm-phytec-linux-gnueabi-g++

# $CC $CFLAGS -I=/usr/include/gstreamer-1.0 -I=/usr/include/glib-2.0 -I=/usr/lib/glib-2.0/include -L=/usr/lib -lgstreamer-1.0 -lgobject-2.0 -lglib-2.0 gst.c -o gst
# ${CFLAGS} -I=/usr/include/gstreamer-1.0 -I=/usr/include/glib-2.0 -I=/usr/lib/glib-2.0/include -L=/usr/lib -lgstreamer-1.0 -lgobject-2.0 -lglib-2.0  
	#$(CC) 


all:
	go build -o demo2 main.go
arm:
	CGO_ENABLED=1 GOOS=linux GOARCH=arm GOARM=7 go build -o ${TARGET} main.go
clean:
	rm -rf ${TARGET} 
