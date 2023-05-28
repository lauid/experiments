#!/bin/bash
set -x

svcs=("mysql" "redis" "php8.2-fpm" "memcached" "nginx" "beanstalkd")

for svc in "${svcs[@]}"
do
	sudo systemctl stop "$svc"
	sudo systemctl disable "$svc"
done
