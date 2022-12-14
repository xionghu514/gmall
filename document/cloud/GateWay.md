## 网关

* 主要作用
  * 负载均衡  过滤请求 验证令牌(统一鉴权) 全局熔断

* 原理
  * 断言（predicates）：满足指定的要求就开始工作；如：PathRoutePredicateFactory，只要请求的路径满足指定要求就开始工作
  * 路由（Route）：满足断言的条件就路由到这个地方；
  * 过滤器（Filter）：到达指定地方前后由所有过滤器一起工作；是实现网关功能的核心,都是需要过滤器来完成其中:我们可以自定义过滤器 常见的过滤器接口 GatewayFilter GlobalFilter

### 执行流程

> **客户端向 Spring Cloud Gateway 发出请求。如果网关处理程序映射确定请求与路由匹配，则将其发送到网关 Web 处理程序。该处理程序通过特定于请求的过滤器链来运行请求。** ***\*筛选器由虚线分隔的原因是，筛选器可以在发送代理请求之前和之后运行逻辑。\******所有 “前置“ 过滤器逻辑均被执行，然后发出代理请求，发出代理请求后，将运行“ 后置 ”过滤器逻辑。**

![](https://oss.yiki.tech/oss/202212300139101.png)

* 总结

  * ### 经过各个过滤器的过滤之后,将满足指定断言规则的请求路由到指定位置

### 配置文件实例

![](https://oss.yiki.tech/oss/202212300143074.png)

![](https://oss.yiki.tech/oss/202212300144673.png)

![](https://oss.yiki.tech/oss/202212300146826.png)

### 流程

![](https://oss.yiki.tech/oss/202212300202503.png)