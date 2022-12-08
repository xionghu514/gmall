## 跨域

> 跨域：浏览器对于 `javascript` 的同源策略的限制, **域名和端口都相同，但是请求路径不同**，不属于跨域。` 跨域不一定都会有跨域问题`, 因为`跨域问题是浏览器对于ajax请求的一种安全限制`：**一个页面发起的ajax请求，只能与当前页域名相同的路径**，这能有效的阻止跨站攻击。以下情况都属于跨域：

| 跨域原因说明       | 示例                                   |
| ------------------ | -------------------------------------- |
| 域名不同           | `www.jd.com` 与 `www.taobao.com`       |
| 域名相同，端口不同 | `www.jd.com:8080` 与 `www.jd.com:8081` |
| 二级域名不同       | `item.jd.com` 与 `miaosha.jd.com`      |
| 协议不同           | `https://jd.com` 和 `http://jd.com`    |

![](https://oss.yiki.tech/gmall/202211200240503.png)

![](https://oss.yiki.tech/gmall/202211200242851.png)

* 解决方案
    * jsonp: 利用 xml 标签解决跨域问题(动态数据两端添加标签)
        * 前后端开发人员协调好
        * 只能解决 get 请求的跨域问题
    * nginx: 代理为不跨域(逃避式的解决方案)
        * 配置 Cors 规范: 违背了 devops 思想
    * cors 规范: 增加服务器端的访问压力
        * 两次请求
            * 预检请求 OPTIONS
                * `Access-Control-Allow-Origin`：可接受的域，是一个具体域名或者*（代表任意域名）
                * `Access-Control-Allow-Credentials`：是否允许携带cookie，默认情况下，cors不会携带cookie，除非这个值是true
                * `Access-Control-Allow-Methods`：允许访问的方式
                * `Access-Control-Allow-Headers`：允许携带的头
                * `Access-Control-Max-Age`：本次许可的有效时长，单位是秒，**过期之前的ajax请求就无需再次进行预检了**
            * 真正的请求

* 实现
    * 服务端可以通过拦截器统一实现，不必每次都去进行跨域判定的编写

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 初始化 一个 cors 配置类对象
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许跨域访问的域名. * 代表所有域名. 不推荐 1. 存在安全问题 2. 不能携带 cookie
        configuration.addAllowedOrigin("http://xxx.xxx.com");
        // 允许那些请求方式跨域访问 * 允许所有
        configuration.addAllowedMethod("*");
        // 允许携带的头信息 * 允许所有
        configuration.addAllowedHeader("*");
        // 允许 cookie 跨域访问, 需要满足两点 1. AllowedOrigin 不能写 * 2. 此处需要设置为 true
        configuration.setAllowCredentials(true);

        // 初始化 cors 配置源
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        // 注册 cors 配置. /** 针对所有路径 做 cors 配置验证.
        configurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(configurationSource);
    }
}
```