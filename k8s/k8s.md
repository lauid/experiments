## k8s proxy k8s.gcr.io 
上海交通大学 k8s-gcr-io.mirrors.sjtug.sjtu.edu.cn
南京大学 gcr.nju.edu.cn/google-containers
docker proxy k8s.dockerproxy.com
lank8s lank8s.cn

## kubectl top

docker pull k8s.dockerproxy.com/metrics-server/metrics-server:v0.4.1

kubectl apply -f https://raw.githubusercontent.com/pythianarora/total-practice/master/sample-kubernetes-code/metrics-server.yaml

ctr -n=k8s.io images import metrics-server.tar

## 由于k8s.gcr.io 需要连外网才可以拉取到，导致 k8s 的基础容器 pause 经常无法获取。k8s docker 可使用代理服拉取，再利用 docker tag 解决问题

docker pull mirrorgooglecontainers/pause:3.1
docker tag mirrorgooglecontainers/pause:3.1 k8s.gcr.io/pause:3.1

但是我们k8s集群中使用的CRI是containerd。所以只能通过 docker tag 镜像，再使用 ctr 导入镜像.

docker save k8s.gcr.io/pause -o pause.tar
ctr -n k8s.io images import pause.tar

containerd和docker在导入镜像这块是存在一切区别的：
containerd 命令行工具 ctr 特性不如 docker 丰富，如 ctr 1.2 并没有 tag 子命令，直到 1.3 才有
为支持多租户隔离，containerd 有 namespace 概念，不同 namespace 下的 image、container 均不同，直接使用 ctr 操作时，会使用 default namespace



kubectl run busybox --image busybox:1.28 --restart=Never --rm -it busybox -- sh


kubeadm token create --print-join-command
