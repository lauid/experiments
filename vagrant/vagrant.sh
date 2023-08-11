#!/bin/bash
set -x

svcs=("mysql" "redis" "php8.2-fpm" "memcached" "nginx" "beanstalkd" "chronyd" "mailhog" "snapd" "influxdb")

for svc in "${svcs[@]}"
do
	sudo systemctl stop "$svc"
	sudo systemctl disable "$svc"
done
