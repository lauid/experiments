➜  ~ brew install --cask sequel-ace


➜  ~ npm install -g commitizen cz-conventional-changelog

➜  ~ brew install npm


➜  ~ curl -x 127.0.0.1:7890 https://www.google.com/

```
在Nginx中，你可以使用自定义的日志格式以满足你的需求。下面是一个较为全面的Nginx日志格式示例：

log_format detailed '$remote_addr - $remote_user [$time_local] '
                    '"$request" $status $body_bytes_sent '
                    '"$http_referer" "$http_user_agent" '
                    '"$http_x_forwarded_for" '
                    '$request_time $upstream_response_time '
                    '$ssl_protocol $ssl_cipher';

access_log /path/to/access.log detailed;
这个日志格式包含了以下信息：

$remote_addr：客户端IP地址
$remote_user：客户端用户名
$time_local：本地时间，格式为 [dd/mmm/yyyy:hh:mm:ss +0800]
$request：客户端请求的 URL 和协议
$status：响应状态码
$body_bytes_sent：发送给客户端的字节数，不包括响应头
$http_referer：HTTP Referer 请求头字段，表示从哪个页面链接过来的
$http_user_agent：客户端浏览器信息
$http_x_forwarded_for：X-Forwarded-For 请求头字段，代理服务器转发请求时记录原始客户端IP地址
$request_time：请求处理时间，单位为秒，包括读取请求、处理请求和响应的时间
$upstream_response_time：上游服务器（如反向代理或FastCGI）响应时间
$ssl_protocol：SSL/TLS协议版本
$ssl_cipher：SSL/TLS加密算法
你可以根据需要自定义日志格式，并在access_log指令中使用该格式来记录访问日志。确保将/path/to/access.log替换为实际的日志文件路径。

使用这种全面的日志格式，你可以获得更详细的访问日志信息，有助于分析和监控网站的访问情况。请注意，日志格式可能会因Nginx版本和配置而略有差异，建议查阅相关文档以了解更多细节。
```
