# 商铺点评项目

## 简介

> 来自黑马程序员机构的项目，该项目主要用于 redis 学习，后端代码本人对部分进行了修改
>
> 黑马资料：https://pan.baidu.com/s/181_KnpttLOAbaoxrU4VwGA    7934
>
> 修改后源码：https://github.com/tyc1210/hmdp.git



## 项目部署

### 后端

​			修改数据库 与 redis 配置信息启动即可

### 前端

​			将 web 文件夹 放到 nginx html 文件夹下

​			配置 nginx.conf

```
 server {
        listen       8088;
        server_name  localhost;
        # 指定前端项目所在的位置
        location / {
            root   html/web;
            index  index.html index.htm;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }


        location /api {  
            default_type  application/json;
            #internal;  
            keepalive_timeout   30s;  
            keepalive_requests  1000;  
            #支持keep-alive  
            proxy_http_version 1.1;  
            rewrite /api(/.*) $1 break;  
            proxy_pass_request_headers on;
            #more_clear_input_headers Accept-Encoding;  
            proxy_next_upstream error timeout;  
            #后端服务地址
            proxy_pass http://127.0.0.1:8081;
        }
    }
```

​			

### 



