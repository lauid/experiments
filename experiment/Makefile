MF_DOCKER_IMAGE_NAME_PREFIX ?= experiment
BUILD_DIR = build
BUILD_TAGS = buildTags
#SERVICES = demo1 demo2 gin
SERVICES = demo1 gin
DOCKERS = $(addprefix docker_,$(SERVICES))
DOCKERS_DEV = $(addprefix docker_dev_,$(SERVICES))
CGO_ENABLED ?= 1
GOARCH ?= amd64
VERSION ?= $(shell git describe --abbrev=0 --tags)
COMMIT ?= $(shell git rev-parse HEAD)
TIME ?= $(shell date +%F_%T)

define compile_service
	CGO_ENABLED=$(CGO_ENABLED) GOOS=$(GOOS) GOARCH=$(GOARCH) GOARM=$(GOARM) \
	go build -race -mod=vendor -tags $(BUILD_TAGS) -ldflags "-s -w \
	-X 'experiment.BuildTime=$(TIME)' \
	-X 'experiment.Version=$(VERSION)' \
	-X 'experiment.Commit=$(COMMIT)'" \
	-o ${BUILD_DIR}/experiment-$(1) cmd/$(1)/main.go
endef

define make_docker
	$(eval svc=$(subst docker_,,$(1)))

	docker build \
		--no-cache \
		--build-arg SVC=$(svc) \
		--build-arg GOARCH=$(GOARCH) \
		--build-arg GOARM=$(GOARM) \
		--build-arg VERSION=$(VERSION) \
		--build-arg COMMIT=$(COMMIT) \
		--build-arg TIME=$(TIME) \
		--tag=$(MF_DOCKER_IMAGE_NAME_PREFIX)/$(svc) \
		-f docker/Dockerfile .
endef

all: $(SERVICES)

.PHONY: all $(SERVICES) dockers dockers_dev

clean:
	rm -rf ${BUILD_DIR}

install:
	cp ${BUILD_DIR}/* $(GOBIN)

test:
	#go test -mod=vendor -v -race -count 1 -tags test $(shell go list ./... | grep -v 'vendor\|cmd')
	#go test -v -race -count 1 -tags test $(shell go list ./... | grep -v 'vendor\|cmd')
	#go test -v -race -count 1 -cover -tags test ./...
	go test -race -count 1 -cover -tags test ./...

proto:
	protoc -I. --go_out=. --go_opt=paths=source_relative pkg/messaging/*.proto
	protoc -I. --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative users/policies/*.proto
	protoc -I. --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative things/policies/*.proto

$(SERVICES):
	$(call compile_service,$(@))

$(DOCKERS):
	$(call make_docker,$(@),$(GOARCH))

dockers: $(DOCKERS)

define docker_push
	for svc in $(SERVICES); do \
		docker push $(MF_DOCKER_IMAGE_NAME_PREFIX)/$$svc:$(1); \
	done
endef

changelog:
	git log $(shell git describe --tags --abbrev=0)..HEAD --pretty=format:"- %s"

latest: dockers
	$(call docker_push,latest)

release:
	$(eval version = $(shell git describe --abbrev=0 --tags))
	git checkout $(version)
	$(MAKE) dockers
	for svc in $(SERVICES); do \
		docker tag $(MF_DOCKER_IMAGE_NAME_PREFIX)/$$svc k8s.io/$(MF_DOCKER_IMAGE_NAME_PREFIX)/$$svc:$(version); \
    	docker save k8s.io/$(MF_DOCKER_IMAGE_NAME_PREFIX)/$$svc:$(version) -o ./$$svc.tar; \
	done
	#$(call docker_push,$(version))
