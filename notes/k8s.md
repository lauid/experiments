## curl跟踪

```curl-format.txt
timelookup: %{time_namelookup}\n
time_connect: %{time_connect}\n
time_appconnect: %{time_appconnect}\n
time_redirect: %{time_redirect}\n
time_pretransfer: %{time_pretransfer}\n
time_starttransfer: %{time_starttransfer}\n
----------\n
time_total: %{time_total}\n
```

curl -w @curl-format.txt -o /dev/null -s -L http://node-exporter.kube-system.svc:9445/metrics

timelookup: 15.522480
time_connect: 15.522615
time_appconnect: 0.000000
time_redirect: 0.000000
time_pretransfer: 15.522649
time_starttransfer: 15.755899
----------
time_total: 15.755997

## 抓包

虽然容器内没有tcpdump命令，但是我们是在宿主机上执行命令来抓取容器内的包，只要宿主机有相应的命令就能够抓取。
抓包命令：
nsenter -n -t 57805 tcpdump -i eth0 -vnn dst port 80
通过nsenter命令，就不需要担心容器里缺少命令工具了，只要宿主机上有相应的命令，就能使用，比如：
[root@test ~]# nsenter -n -t 57805 telnet 192.168.51.206 80




## install containerd
set -x
sudo apt-get update
sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

sudo rm /etc/apt/sources.list.d/docker.list
sudo curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y containerd.io
如果是安装docker则执行：
sudo apt-get install docker-ce docker-ce-cli containerd.io




## Python

apt-get update

apt-key adv --keyserver keyserver.ubuntu.com --recv-keys A4B469963BF863CC

apt-get install python3 python3-pip -y

pip3 install torch torchvision -i https://mirrors.aliyun.com/pypi/simple/

pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple torch


## install kubelet

curl https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | apt-key add -

cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
deb http://mirror.azure.cn/kubernetes/packages/apt/ kubernetes-xenial main
EOF

apt-get update

apt install kubelet=1.22.0-00

lsb_release -sc

ls -l /var/cache/apt/archives


## mirrors

docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kube-apiserver:v1.18.0
docker pull registry.aliyuncs.com/google_containers/kube-apiserver:v1.18.0

docker pull gcr.azk8s.cn/google_containers/hyperkube:v1.12.1
docker pull gcr.azk8s.cn/google_containers/pause-amd64:3.1
