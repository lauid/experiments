#### 常用命令安装

sudo apt install vim git wget curl

go install github.com/fullstorydev/grpcui/cmd/grpcui@latest

go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

### 常用命令使用

##### scp

scp -r go-plugin/ root@192.168.3.11:~

##### nc

echo -e '{"method":"HelloServiceName.Hello","params":["hello"],"id":1}' | nc localhost 2345

curl -ks https://127.0.0.1:9200/_cat | jq .

#### linux 离线安装

下载离线安装包
yumdownloader --resolve --destdir=. htop

ubuntu下载离线安装包
sudo apt-get download package_name
dpkg -i

#### ntp

root@node1:~# ntpdate ntp.aliyun.com

#### openjdk

sudo apt install openjdk-11-jdk
sudo apt install openjdk-11-jdk-headless

#### windows nfs

PS C:\Users\lau\code> WinNFSd.exe C:\Users\lau\code\demo\
mount -t nfs 192.168.175.90:/c/Users/lau/code/demo /mnt/demo -o nfsvers=3

#### git提交版本格式化工具

Install commitizen globally, if you have not already.

- npm install -g commitizen

Install your preferred commitizen adapter globally (for example cz-conventional-changelog).

- npm install -g cz-conventional-changelog

Create a .czrc file in your home directory, with path referring to the preferred, globally-installed, commitizen adapter

- echo '{ "path": "cz-conventional-changelog" }' > ~/.czrc

You are all set! Now cd into any git repository and use git cz instead of git commit, and you will find the commitizen
prompt.

Pro tip: You can use all the git commit options with git cz. For example: git cz -a.

#### oh-my-zsh

sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"

#### 命令行颜色

export PATH=$PATH:/usr/local/go/bin:/root/go/bin

export PS1="\[\e[32m\]\u@\h \[\e[33m\]\w\[\e[0m\]\$ "

获取到你需要的镜像名称和版本之后 (参见后面的排查技巧)

#### docker 镜像

docker pull mirrorgooglecontainers/$imageName:$imageVersion
docker tag mirrorgooglecontainers/$imageName:$imageVersion k8s.gcr.io/$imageName:$imageVersion
docker save k8s.gcr.io/$imageName:$imageVersion > $imageName.tar

#### ctr镜像操作

使用ctr命令导入镜像。
ctr image import app.tar #导入本地镜像
ctr images list|grep app #查看导入的镜像
crictl images list|grep app #此命令也可查看

命令介绍：
ctr：是containerd本身的CLI
crictl ：是Kubernetes社区定义的专门CLI工具
