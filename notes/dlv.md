

$ go install github.com/go-delve/delve/cmd/dlv@latest

#!/bin/bash
dlv debug --headless --listen=:2345 --api-version=2 --accept-multiclient   $GOPATH/src/web.go



```
FROM golang:1.12.12

RUN mkdir /web/

COPY ./dlv /usr/local/bin/
COPY ./web.go /go/src
COPY ./start.sh  /web/
RUN chmod u+x /web/start.sh
WORKDIR /go/src/
ENTRYPOINT ["/web/start.sh"]
```
