# 发送 sz
Regular expression: rz waiting to receive.\*\*B0100 (注意这里是这样)
Action: Run Silent Coprocess
Parameters: /usr/local/bin/iterm2-send-zmodem.sh
# 接收 rz
Regular expression:\*\*B00000000000000
Action: Run Silent Coprocess
Parameters: /usr/local/bin/iterm2-recv-zmodem.sh

