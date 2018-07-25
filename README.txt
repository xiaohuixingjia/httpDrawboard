出口跳板工程，可以访问该跳板服务进行转发
只需要在http头中设置
http_req_type -- 请求的类型 必传（目前支持的有 get、postBodyData、postParams 下面会对这三种类型进行讲解）
HTTP_PROXY_URL --  通过代理访问的路径  必传
HTTP_CONNECT_TIME -- 连接时间 （默认1秒）
HTTP_READ_TIME -- 读取时间 （默认3秒）
CONTENT_TYPE -- 请求的参数类型 （默认为 text/plain）

get请求的转发只会取 HTTP_PROXY_URL参数值 进行转发，不会取请求的参数进行拼接
postBodyData 直接把请求的参数信息放到http post请求中 不进行格式化
postParams 代表客户端的请求的信息是以表单提交的格式请求过来的，那么再请求真实路径的时候也会解析请求参数以表单提交的格式去请求


如果正常响应 则返回响应报文，如果有异常则返回空字符串，返回的信息类型为：text/plain


httpUtil工具类是一个使用代理访问的样例，具备自动回切功能 他的访问使用的配置参数为：
##代理发送的url
proxyUrl=http://ip:port
##连续错误多少次后使用代理进行发送
maxErrorTimes=20
##代理使用一定次数后回调测试
maxUseProxyTimes=40
##代理是否可用
proxyCanUse=true