# kubectl apply -f serviceaccount.yaml

# kubectl create clusterrolebinding nfs-provisioner-clusterrolebinding --clusterrole=cluster-admin --serviceaccount=default:nfs-provisioner

# 创建一个nfs provisioner
# kubectl apply -f nfs-deployment.yaml

# 创建一个storageclass,指定provisioner
# kubectl apply -f nfs-storageclass.yaml

# 创建pvc,根据storageclass，动态生成pv
# kubectl apply -f claim.yaml


# 创建pod，使用通过storageclass生成的pvc
# kubectl apply -f read-pod.yaml
