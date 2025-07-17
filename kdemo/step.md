1. crd GPU 增加一个vendor字段
1.1 类型是Vendor枚举,有huawei,nvidia, 枚举变量大写,枚举值是小写
1.2 项目中用的是jackjson, 所以需要接口中vendor字段返回大写
1.3 kubernetes-client-java 中用的是gson,kubernetes中的cr要求是小写
