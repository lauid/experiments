#go install github.com/grpc-ecosystem/grpc-gateway/v2/protoc-gen-grpc-gateway@latest
#protoc --go_out .. --go-grpc_out .. ./hello.proto
PROTOC_VERSION=21.12
PROTOC_GEN_VERSION=v1.28.1
PROTOC_GRPC_VERSION=v1.2.0
setup_protoc() {
    # Execute `go get` for protoc dependencies outside of project dir.
    echo "Setting up protoc..."
    PROTOC_ZIP=protoc-$PROTOC_VERSION-linux-x86_64.zip
    curl -0L https://github.com/google/protobuf/releases/download/v$PROTOC_VERSION/$PROTOC_ZIP -o $PROTOC_ZIP
    unzip -o $PROTOC_ZIP -d protoc3
    sudo mv protoc3/bin/* /usr/local/bin/
    sudo mv protoc3/include/* /usr/local/include/
    rm -f PROTOC_ZIP

    go install google.golang.org/protobuf/cmd/protoc-gen-go@$PROTOC_GEN_VERSION
    go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@$PROTOC_GRPC_VERSION

    export PATH=$PATH:/usr/local/bin/protoc
}

setup_protoc