FROM golang:1.19-alpine AS builder
ARG SVC
ARG GOARCH
ARG GOARM
ARG VERSION
ARG COMMIT
ARG TIME

WORKDIR /experiment
COPY . .
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN GOPROXY="https://goproxy.cn"
RUN apk update \
    && apk add make\
    && make $SVC \
    && mv build/experiment-$SVC /exe

FROM scratch
# Certificates are needed so that mailing util can work.
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
COPY --from=builder /exe /
ENTRYPOINT ["/exe"]