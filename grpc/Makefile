test:
	go test -v -race -count 1 -tags test $(shell go list ./... | grep -v 'vendor\|cmd')

proto:
	protoc -I. --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative pkg/grpc/pb/*.proto

