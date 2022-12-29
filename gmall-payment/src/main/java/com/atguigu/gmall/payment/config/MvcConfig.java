package com.atguigu.gmall.payment.config;

import com.atguigu.gmall.payment.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: 配置拦截器
 * @Author: Guan FuQing
 * @Date: 2022/12/25 12:41
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有路径
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/pay/success", "/pay/ok");
    }
}
