二、搭建NFS Server
# master-0
## 1.安装 NFS服务器端
sudo apt-get install nfs-kernel-server 

## 2.把/nfs目录设置为共享目录共享目录
sudo cat /etc/exports
# * 表示允许任何网段 IP 的系统访问该 NFS 目录
/nfs *(rw,sync,no_root_squash) 

sudo mkdir /nfs
sudo chmod -R 777 /nfs
## vinson为当前用户
sudo chown -R vinson:vinson /nfs/

## 3.启动NFS服务
sudo /etc/init.d/nfs-kernel-server restart

## 4.测试,在/nfs目录下创建文件
echo "test" >> /nfs/test.txt

三、NFS Client配置
## slave-0、slave-1
# 1.安装 NFS客户端
sudo apt-get install nfs-common         
sudo mount -t nfs 192.168.204.129:/nfs /mnt -o nolock

# 2.开机自动挂载，在/etc/fstab里添加
192.168.204.129:/nfs   /mnt   nfs  rw 0 0

# 3. 测试：在Clietn端cat一下/mnt/test.txt,可以看到文件说明可以了