## 下载loki和promtail：

https://github.com/grafana/loki/releases
### loki配置文件：

https://github.com/grafana/loki/blob/main/cmd/loki/loki-local-config.yaml
### 安装loki：

```shell
cat > /usr/lib/systemd/system/loki.service <<EOF
[Unit]
Description=loki
Documentation=https://github.com/grafana/loki/tree/master
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/loki/loki -config.file=/usr/local/loki/loki-local-config.yaml
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
``` 

### 开机启动：

systemctl enable promtail.service

## 安装promtail：在需要手机日志的节点上安装promtail。

```shell
cat > /usr/lib/systemd/system/promtail.service <<EOF
[Unit]
Description=promtail
Documentation=https://github.com/grafana/loki/tree/master
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/promtail/promtail -config.file=/usr/local/promtail/promtail-local-config.yaml
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
```

开机启动：

systemctl enable loki.service

promtail配置文件：

```shell
server:
  http_listen_port: 9080
  grpc_listen_port: 0
 
positions:
  filename: /tmp/positions.yaml
 
clients:
  - url: http://192.168.199.20:3100/loki/api/v1/push  # loki服务端地址
 
scrape_configs:
- job_name: system
  static_configs:
  - targets:
      - localhost
    labels:
      job: varlogs
      __path__: /var/log/nginx/*log
```

启动服务，在grafana中添加loki数据源即可。