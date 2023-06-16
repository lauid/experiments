docker tag experiment/gin:v0.0.1 k8s.io/experiment/gin:v0.0.1

docker save k8s.io/experiment/gin:v0.0.1 -o ~/gin.tar

ctr -n=k8s.io image import ./gin.tar

kubectl run busybox --image=busybox:1.28 --restart=Never -- /bin/sh -c "while true; do echo hello; sleep 10; done"

kubectl run busybox --image=busybox:1.28 -it --restart=Never -- /bin/sh

kubectl run <pod-name> --image=<image-name> --restart=Never --dry-run=client -o yaml -- /bin/sh -c "sleep infinity" | kubectl apply -f -

kubectl exec -it <pod-name> -c <container-name> -- /bin/sh

kubectl run <new-pod-name> --image=<image-name> --restart=Never --attach --rm --overrides='{"apiVersion":"v1","spec":{"nodeName":"<node-name>","containers":[{"name":"<container-name>","image":"<image-name>"}]}}' -- /bin/sh


kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.0.0/deploy/static/provider/cloud/deploy.yaml

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.0/deploy/static/provider/cloud/deploy.yaml


kubectl delete -A ValidatingWebhookConfiguration ingress-nginx-admission
