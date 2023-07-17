$ curl -L -o kubebuilder https://go.kubebuilder.io/dl/latest/$(go env GOOS)/$(go env GOARCH) && chmod +x kubebuilder && mv kubebuilder /usr/local/bin/


kubebuilder init --domain my.domain --repo my.domain/tutorial


kubebuilder create api --group tutorial --version v1 --kind Foo

