set -x

apt-get update && sudo apt-get install -y apt-transport https ca-certificates curl software-properties-common gnupg2

curl -fsSL https://mirrors.ustc.edu.cn/docker-ce/linux/ubuntu/gpg | sudo apt-key add -

sudo add-apt-repository \
	 "deb [arch=amd64] https://mirrors.ustc.edu.cn/docker-ce/linux/ubuntu/ \
	 $(lsb_release -cs) \
	 stable"

sudo apt-get install docker-ce docker-ce-cli containerd.io -y


cat <<EOF | tee /etc/docker/daemon.json
{
"exec-opts": ["native.cgroupdriver=systemd"],
"log-driver": "json-file",
"log-opts": {
"max-size": "100m"
},
"storage-driver": "overlay2"
}
EOF

systemctl daemon-reload
systemctl enable docker --now


cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
deb http://mirrors.ustc.edu.cn/kubernetes/apt kubernetes-xenial main
EOF

apt-get update 


#apt-key adv --recv-keys --keyserver keyserver.ubuntu.com FEEA9169307EA071

# apt-get install -y kubelet=1.23.1-00 kubeadm=1.23.1-00 kubectl=1.23.1-00
apt-get install -y kubelet=1.25.1-00 kubeadm=1.25.1-00 kubectl=1.25.1-00

apt-mark hold kubelet kubeadm kubectl

swapoff -a

kubeadm init --apiserver-advertise-address 192.168.40.180 --image-repository registry.cn-hangzhou.aliyuncs.com/google_containers --pod-network-cidr=10.244.0.0/16 --ignore-preflight-errors=SystemVerification
