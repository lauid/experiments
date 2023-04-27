# 生成动态库

gcc -c -fPIC -o hi.o hi.c
gcc -shared -o libhi.so hi.o

> gcc -fPIC -shared -o libhi.so hi.c

> export LD_LIBRARY_PATH=/home/vagrant/code/demo/cmd/cgo2:$LD_LIBRARY_PATH

> go run main.go 