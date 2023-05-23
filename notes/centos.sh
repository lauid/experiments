#!/bin/sh
set -x

#sudo yum update -y
#sudo yum install -y device-mapper-persistent-data lvm2 wget net-tools nfs-utils lrzsz gcc gcc-c++ make cmake libxml2-devel openssl-devel curl curl-devel unzip sudo ntp libaio-devel wget vim ncurses-devel autoconf automake zlib-devel python-devel epel-release openssh-server socat ipvsadm conntrack telnet ipvsadm

#sudo sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config

modprobe br_netfilter
cat > /etc/sysctl.d/k8s.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF
sysctl -p /etc/sysctl.d/k8s.conf

systemctl stop firewalld ; systemctl disable firewalld


#配置国内安装 docker 和 containerd 的阿里云的 repo 源
sudo yum install yum-utils -y
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

#配置安装 k8s 组件需要的阿里云的 repo 源
cat > /etc/yum.repos.d/kubernetes.repo <<EOF
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=0
EOF


ntpdate cn.pool.ntp.org

#安装 containerd 服务
yum install containerd.io-1.6.6 -y
#containerd config default > /etc/containerd/config.toml
#systemctl enable containerd --now
#systemctl restart containerd 
#把 SystemdCgroup = false 修改成 SystemdCgroup = true
#把 sandbox_image = "k8s.gcr.io/pause:3.6"修改成
#sandbox_image="registry.aliyuncs.com/google_containers/pause:3.7"

#修改/etc/crictl.yaml 文件
cat > /etc/crictl.yaml <<EOF
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 10
debug: false
EOF
systemctl restart containerd 


#配置 containerd 镜像加速器，k8s 所有节点均按照以下配置：

#编辑 vim /etc/containerd/config.toml 文件
#找到 config_path = ""，修改成如下目录：
#config_path = "/etc/containerd/certs.d"

mkdir /etc/containerd/certs.d/docker.io/ -p

cat > /etc/containerd/certs.d/docker.io/hosts.toml <<EOF
[host."https://vh3bm52y.mirror.aliyuncs.com",host."https://registry.docker-cn.com"]
capabilities = ["pull"]
EOF

systemctl restart containerd





###docker 也要安装，docker 跟 containerd 不冲突，安装 docker 是为了能基于 dockerfile 构建镜像
yum install docker-ce -y
systemctl enable docker --now
#配置 docker 镜像加速器，k8s 所有节点均按照以下配置
cat > /etc/docker/daemon.json <<EOF
{
	"registry-mirrors":["https://vh3bm52y.mirror.aliyuncs.com","https://registry.docker-cn.com","https://docker.mirrors.ustc.edu.cn","https://dockerhub.azk8s.cn","http://hub-mirror.c.163.com"]
}
EOF

systemctl restart docker


#########安装初始化 k8s 需要的软件包
yum install -y kubelet-1.26.0 kubeadm-1.26.0 kubectl-1.26.0
systemctl enable kubelet
#Kubeadm: kubeadm 是一个工具，用来初始化 k8s 集群的
#kubelet: 安装在集群所有节点上，用于启动 Pod 的，kubeadm 安装 k8s，k8s 控制节点和工作节点的组件，都是基于 pod 运行的，只要 pod 启动，就需要 kubelet
#kubectl: 通过 kubectl 可以部署和管理应用，查看各种资源，创建、删除和更新各种组件

#4、kubeadm 初始化 k8s 集群
#设置容器运行时
crictl config runtime-endpoint unix:///run/containerd/containerd.sock
#使用 kubeadm 初始化 k8s 集群
#kubeadm config print init-defaults > kubeadm.yaml

#master
#kubeadm init --config=kubeadm.yaml --ignore-preflight-errors=SystemVerification

#master 查看加入节点的命令
#kubeadm token create --print-join-command


#可以对 node1 打个标签，显示 work
#[root@master1~]# kubectl label nodes node1 node-role.kubernetes.io/work=work



#安装 kubernetes 网络组件-Calico
#把安装 calico 需要的镜像 calico.tar.gz 传到 xianchaomaster1 和 xianchaonode1 节点，手动 解压：
#注：在线下载配置文件地址是： https://docs.projectcalico.org/manifests/calico.yaml
#[root@master1 ~]# kubectl apply -f calico.yaml
。
