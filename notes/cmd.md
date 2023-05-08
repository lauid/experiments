scp -r go-plugin/ root@192.168.3.11:~




echo -e '{"method":"HelloServiceName.Hello","params":["hello"],"id":1}' | nc localhost 2345