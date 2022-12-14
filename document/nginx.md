## 反向代理

> 我们希望 直接通过域名去访问服务资源. http://xxx.xxx 默认都是 80 端口, 而 80 端口 只有一个
> 将来我们希望多个工程可以使用 域名去访问. 此时就需要借助于 nginx 的反向代理. 所有请求通过nginx
> 转发到对应的服务, 在通过网关路由到具体的服务

![](https://oss.yiki.tech/oss/202212081345690.png)

```shell
cd /usr/local/nginx/conf/

vim nginx.conf
```

```shell
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # 代理网关
    server {
        listen          80; # 监听 80 端口
        server_name     xxx.xxx.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.111:8888; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }

    # 代理后台管理
    server {
        listen          80; # 监听 80 端口
        server_name     xxx.xxx.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.121:1000; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }
}
```

```shell
// 进入 nginx 目录
cd /usr/local/nginx/sbin

// 重新加载
./nginx  -s  reload
```

## Nginx 加入后流程

> 注: 域名会经过 hosts 文件 或者 dns 服务器解析成 具体的 Ip 访问 Nginx, Nginx 通过 请求头 Host 将域名携带过去. nginx 配置文件中的 server_name 通过携带的 域名进行其他操作

![](https://oss.yiki.tech/gmall/202211200218195.png)

> 浏览器输入域名 -> 本机hosts 文件对此域名进行解析 -> 真实发送请求是 ip + 请求路径 -> 根据 ip + 80 端口找到 nginx 服务器 -> nginx 配置了反向代理根据 域名头信息找到 当前 server -> 根据反向代理后的  ip 地址找到网关应用 -> 网关根据 断言配置 路由到对应的 微服务 -> 找到对应的 controller 方法处理 -> 处理完后沿路返回到用户浏览器

![](https://oss.yiki.tech/gmall/202211200219709.png)

## 静态资源部署

```
    # 动静分离
    server {
        listen          80; # 监听 80 端口
        server_name     static.x x x.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                root /opt/static;
        }
    }
```
