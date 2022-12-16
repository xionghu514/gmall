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

## 一些常见问题

### 502 Bad Gateway

> 请求到达nginx 未到达网关

![](https://oss.yiki.tech/oss/202212161114488.png)

### 404

> 请求到达nginx并转发给网关, 网关没有找到服务或者对应的方法导致

![](https://oss.yiki.tech/oss/202212161114735.png)

#### 网关根据域名路由注意

> 域名不带路径访问首先会解析成 ip 地址访问 nginx, nginx 把请求转发给网关. nginx 是通过 ip:port 转发给网关.
>
> 网关无法拿到域名, 也就进入不了对应的路由进而无法到达 对应的服务. 

![](https://oss.yiki.tech/oss/202212161114788.png)

![](https://oss.yiki.tech/oss/202212161114708.png)

![](https://oss.yiki.tech/gmall/202211232132960.png)

> 解决: 请求到达 nginx, nginx 是可以通过 host 头信息拿到域名的. nginx 转发时 头信息默认是没有携带的.
>
> 所以在 server_name 下 location 上添加 proxy_set_header Host $host; 代理设置头信息, 反向代理时把头信息一并携带过去. 把 host 通过 Host 头携带过去.
>
> 网关可以获取域名就可以正常路由到相应的服务了

![](https://oss.yiki.tech/oss/202212161114770.png)

![](https://oss.yiki.tech/oss/202212161118058.png)

### 无法访问此网站

> nginx 不存在

![](https://oss.yiki.tech/oss/202212161117286.png)