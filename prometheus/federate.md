- job_name: 'prometheus-federate-2.101'
    scrape_interval: 10s
    honor_labels: true
    metrics_path: '/federate'
    params:
    'match[]': 
        - '{job="prometheus"}' 
        - '{__name__=~"job:.*"}' 
        - '{__name__=~"node.*"}'
    static_configs: 
        - targets: ["192.168.40.181:9090"]