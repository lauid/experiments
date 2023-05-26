#curl -fsSL https://mirrors.ustc.edu.cn/docker-ce/linux/ubuntu/gpg | sudo apt-key add -
#
#sudo add-apt-repository \
#"deb [arch=amd64] https://mirrors.ustc.edu.cn/docker-ce/linux/ubuntu/ \
#$(lsb_release -cs) \
#stable"
#
#sudo apt-get install docker-ce docker-ce-cli containerd.io -y

#cat <<EOF | tee /etc/docker/daemon.json
#{
#	"exec-opts": ["native.cgroupdriver=systemd"],
#	"log-driver": "json-file",
#	"log-opts": {
#		"max-size": "100m"
#	},
#	"storage-driver": "overlay2"
#}
#EOF


#mkdir -p /etc/systemd/system/docker.service.d
#
#systemctl daemon-reload
#systemctl restart docker
#systemctl enable --now docker


#cat <<EOF > /etc/apt/sources.list.d/kubernetes.list
#deb http://mirrors.ustc.edu.cn/kubernetes/apt kubernetes-xenial main
#EOF

#apt-key adv --keyserver keyserver.ubuntu.com --recv-keys  B53DC80D13EDEF05

#apt-get update && apt-get install -y apt-transport-https curl


#apt-get install -y kubelet=1.23.1-00 kubeadm=1.23.1-00 kubectl=1.23.1-00

#apt-mark hold kubelet kubeadm kubectl

swapoff -a


#master
kubeadm init --apiserver-advertise-address 192.168.56.180 --image-repository registry.cn-hangzhou.aliyuncs.com/google_containers --pod-network-cidr=10.244.0.0/16 --ignore-preflight-errors=SystemVerification

wget http://docs.projectcalico.org/v3.24/manifests/calico.yaml

# k8s vs calico version 
https://docs.tigera.io/archive/v3.24/getting-started/kubernetes/requirements

kubectl get pod --all-namespaces
kubectl get nodes


#test k8s network and coredns
kubectl run busybox --image busybox:1.28 --restart=Never --rm -it busybox -- sh

ping qq.com
nslookup kubernetes.default.svc.cluster.local


#-------------------------
To start using your cluster, you need to run the following as a regular user:

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
https://kubernetes.io/docs/concepts/cluster-administration/addons/

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.40.180:6443 --token 48r2rj.4rxr3fhgua60wdeh \
	--discovery-token-ca-cert-hash sha256:469e299a487ca713b554e4a0f26a5998d0fb84fae71edd55a55e0542cd430de2
