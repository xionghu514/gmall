package com.atguigu.gmall.sms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
//@EnableDiscoveryClient // 通过该注解可以将服务注册到注册中心中, 可以省略. 默认就会将该服务注册到服务中心中
@EnableFeignClients // 开启 feign 功能
@EnableSwagger2 // 启用 Swagger
@MapperScan("com.atguigu.gmall.*.mapper") // 需要指定具体目录. mapper 扫描到接口就会动态代理提供实现. 例如扫描到 service 接口会有两个实现类 注入就会报错
public class GmallSmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSmsApplication.class, args);
    }

}
