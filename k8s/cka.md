## 1,RBAC

```
root@master1:~# kubectl create clusterrole deployment-clusterrole --verb=create --resource=deployments,statefulsets,daemonsets
root@master1:~# kubectl create serviceaccount cicd-token -n app-team1
root@master1:~# kubectl create rolebinding cicd-token-binding -n app-team1 --clusterrole=deployment-clusterrole --serviceaccount=app-team1:cicd-token
```

## 2,节点维护 

```
root@master1:~# kubectl cordon node1
node/node1 cordoned
root@master1:~# kubectl get nodes
NAME      STATUS                     ROLES           AGE     VERSION
master1   Ready                      control-plane   2d16h   v1.25.1
node1     Ready,SchedulingDisabled   <none>          2d16h   v1.25.1
root@master1:~# kubectl drain node1 --delete-emptydir-data --ignore-daemonsets --force
node/node1 already cordoned
Warning: ignoring DaemonSet-managed Pods: kube-system/calico-node-6z69k, kube-system/kube-proxy-h7h7s
node/node1 drained
root@master1:~# kubectl get nodes
NAME      STATUS                     ROLES           AGE     VERSION
master1   Ready                      control-plane   2d16h   v1.25.1
node1     Ready,SchedulingDisabled   <none>          2d16h   v1.25.1
root@master1:~# kubectl uncordon node1
node/node1 uncordoned
root@master1:~# kubectl get nodes
NAME      STATUS   ROLES           AGE     VERSION
master1   Ready    control-plane   2d16h   v1.25.1
node1     Ready    <none>          2d16h   v1.25.1
```


## 3,k8s upgrade

```
root@master1:~# kubectl cordon master1
root@master1:~# kubectl drain master1 --delete-emptydir-data --ignore-daemonsets --force

root@master1:~# apt-cache show kubeadm| grep 1.25.2
Version: 1.25.2-00
Filename: pool/kubeadm_1.25.2-00_amd64_5f3996559255107d2ad690fdded29e76bb6b550154622ccf22444753d2ab2272.deb
root@master1:~# kubectl get nodes
NAME      STATUS                     ROLES           AGE     VERSION
master1   Ready,SchedulingDisabled   control-plane   2d16h   v1.25.1
node1     Ready                      <none>          2d16h   v1.25.1
root@master1:~# apt-get update

root@master1:~# kubeadm upgrade plan
root@master1:~# kubeadm upgrade apply v1.25.2 --etcd-upgrade=false
root@master1:~# kubeadm version
kubeadm version: &version.Info{Major:"1", Minor:"25", GitVersion:"v1.25.2", GitCommit:"5835544ca568b757a8ecae5c153f317e5736700e", GitTreeState:"clean", BuildDate:"2022-09-21T14:32:18Z", GoVersion:"go1.19.1", Compiler:"gc", Platform:"linux/amd64"}

root@master1:~# apt-get install kubelet=1.25.2-00
root@master1:~# kubelet --version
Kubernetes v1.25.2
root@master1:~# apt-get install kubectl=1.25.2-00
root@master1:~# kubectl version
WARNING: This version information is deprecated and will be replaced with the output from kubectl version --short.  Use --output=yaml|json to get the full version.
Client Version: version.Info{Major:"1", Minor:"25", GitVersion:"v1.25.2", GitCommit:"5835544ca568b757a8ecae5c153f317e5736700e", GitTreeState:"clean", BuildDate:"2022-09-21T14:33:49Z", GoVersion:"go1.19.1", Compiler:"gc", Platform:"linux/amd64"}
Kustomize Version: v4.5.7
Server Version: version.Info{Major:"1", Minor:"25", GitVersion:"v1.25.2", GitCommit:"5835544ca568b757a8ecae5c153f317e5736700e", GitTreeState:"clean", BuildDate:"2022-09-21T14:27:13Z", GoVersion:"go1.19.1", Compiler:"gc", Platform:"linux/amd64"}
root@node1:~# kubectl uncordon master1
node/master1 uncordoned
root@node1:~# kubectl get nodes
NAME      STATUS   ROLES           AGE     VERSION
master1   Ready    control-plane   2d16h   v1.25.2
node1     Ready    <none>          2d16h   v1.25.1
```


### kubeadm部署的etcd没有etcdctl命令，需要下载etcd二进制包。
下载地址: https://github.com/etcd-io/etcd/releases/download/v3.5.0/etcd-v3.5.0-linux-amd64.tar.gz

## 3,etcd backup
root@master1:~# ETCDCTL_API=3 etcdctl --endpoints="https://127.0.0.1:2379" --cacert=/etc/kubernetes/pki/etcd/ca.crt --cert=/etc/kubernetes/pki/apiserver-etcd-client.crt --key=/etc/kubernetes/pki/apiserver-etcd-client.key snapshot save /srv/data/etcd-snapshot.db
{"level":"info","ts":1685150208.1283264,"caller":"snapshot/v3_snapshot.go:68","msg":"created temporary db file","path":"/srv/data/etcd-snapshot.db.part"}
{"level":"info","ts":1685150208.1517189,"logger":"client","caller":"v3/maintenance.go:211","msg":"opened snapshot stream; downloading"}
{"level":"info","ts":1685150208.1518269,"caller":"snapshot/v3_snapshot.go:76","msg":"fetching snapshot","endpoint":"https://127.0.0.1:2379"}
{"level":"info","ts":1685150208.4551034,"logger":"client","caller":"v3/maintenance.go:219","msg":"completed snapshot read; closing"}
{"level":"info","ts":1685150208.7449608,"caller":"snapshot/v3_snapshot.go:91","msg":"fetched snapshot","endpoint":"https://127.0.0.1:2379","size":"6.0 MB","took":"now"}
{"level":"info","ts":1685150208.745261,"caller":"snapshot/v3_snapshot.go:100","msg":"saved","path":"/srv/data/etcd-snapshot.db"}
Snapshot saved at /srv/data/etcd-snapshot.db


### etcd restore
root@master1:~# ETCDCTL_API=3 etcdctl --endpoints="https://127.0.0.1:2379" --cacert=/etc/kubernetes/pki/etcd/ca.crt --cert=/etc/kubernetes/pki/apiserver-etcd-client.crt --key=/etc/kubernetes/pki/apiserver-etcd-client.key snapshot restore /srv/data/etcd-snapshot.db
Deprecated: Use `etcdutl snapshot restore` instead.

2023-05-27T01:22:30Z	info	snapshot/v3_snapshot.go:251	restoring snapshot	{"path": "/srv/data/etcd-snapshot.db", "wal-dir": "default.etcd/member/wal", "data-dir": "default.etcd", "snap-dir": "default.etcd/member/snap", "stack": "go.etcd.io/etcd/etcdutl/v3/snapshot.(*v3Manager).Restore\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdutl/snapshot/v3_snapshot.go:257\ngo.etcd.io/etcd/etcdutl/v3/etcdutl.SnapshotRestoreCommandFunc\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdutl/etcdutl/snapshot_command.go:147\ngo.etcd.io/etcd/etcdctl/v3/ctlv3/command.snapshotRestoreCommandFunc\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdctl/ctlv3/command/snapshot_command.go:128\ngithub.com/spf13/cobra.(*Command).execute\n\t/home/remote/sbatsche/.gvm/pkgsets/go1.16.3/global/pkg/mod/github.com/spf13/cobra@v1.1.3/command.go:856\ngithub.com/spf13/cobra.(*Command).ExecuteC\n\t/home/remote/sbatsche/.gvm/pkgsets/go1.16.3/global/pkg/mod/github.com/spf13/cobra@v1.1.3/command.go:960\ngithub.com/spf13/cobra.(*Command).Execute\n\t/home/remote/sbatsche/.gvm/pkgsets/go1.16.3/global/pkg/mod/github.com/spf13/cobra@v1.1.3/command.go:897\ngo.etcd.io/etcd/etcdctl/v3/ctlv3.Start\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdctl/ctlv3/ctl.go:107\ngo.etcd.io/etcd/etcdctl/v3/ctlv3.MustStart\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdctl/ctlv3/ctl.go:111\nmain.main\n\t/tmp/etcd-release-3.5.1/etcd/release/etcd/etcdctl/main.go:59\nruntime.main\n\t/home/remote/sbatsche/.gvm/gos/go1.16.3/src/runtime/proc.go:225"}
2023-05-27T01:22:30Z	info	membership/store.go:141	Trimming membership information from the backend...
2023-05-27T01:22:31Z	info	membership/cluster.go:421	added member	{"cluster-id": "cdf818194e3a8c32", "local-member-id": "0", "added-peer-id": "8e9e05c52164694d", "added-peer-peer-urls": ["http://localhost:2380"]}
2023-05-27T01:22:31Z	info	snapshot/v3_snapshot.go:272	restored snapshot	{"path": "/srv/data/etcd-snapshot.db", "wal-dir": "default.etcd/member/wal", "data-dir": "default.etcd", "snap-dir": "default.etcd/member/snap"}



