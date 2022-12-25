package com.atguigu.gmall.cart.interceptors;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @Description: 登陆拦截器
 * @Author: Guan FuQing
 * @Date: 2022/12/25 12:35
 * @Email: moumouguan@gmail.com
 */
@Component // 注入到 spring 容器
//@Data
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {

//    private UserInfo userInfo;

    @Autowired
    private JwtProperties properties;

    // 范型中放入的是真正的载荷, 需要将什么东西传递给后续业务
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>(); // 不要对外直接暴露

    /**
     * 前置方法, 在 Controller 方法执行之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("前置方法, 在 Controller 方法执行之前执行");

        // 从 cookie 中获取 token 和 userKey
        String token = CookieUtils.getCookieValue(request, properties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, properties.getUserKey());

        // 不管是否登陆, userKey 都应该存在
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(
                    request, response,
                    properties.getUserKey(), userKey,
                    properties.getExpire()
            );
        }

        // 从 token 中解析出 userId
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, properties.getPublicKey());
            userId = Long.valueOf(map.get("userId").toString());
        }

        // 已经获取了登陆信息
        UserInfo userInfo = new UserInfo(userId , userKey);
        // 把信息放入线程的局部变量
        THREAD_LOCAL.set(userInfo);

        // TODO: 假装已经获取了登陆信息 userId userKey
        // 全局变量: 定义状态字段, 经过拦截器赋值 Controller 中注入拦截器使用.  Component 单例. 用户 A 访问赋值 userInfo, 未执行到 Controller 方法, 用户 B 访问 将 userInfo 覆盖, A 用户使用的是 B 用户的信息
//        userInfo = new UserInfo(1L, UUID.randomUUID().toString()); // 公共变量存在线程安全问题.
        // request: 经过拦截器赋值, 经过拦截器赋值 Controller 中注入 request 使用
//        request.setAttribute("userId", 1L);
//        request.setAttribute("userKey", UUID.randomUUID().toString());
        // ThreadLocal: 是 Thread 局部变量, 用于多线程程序, 对解决多线程程序的并发有一定的启示作用. 它不是一个 Thread, 而是 Thread 的局部变量
//        THREAD_LOCAL.set(new UserInfo(1L, UUID.randomUUID().toString()));

        return true; // true 为放行, false 为拦截
    }

    /**
     * 为 THREAD_LOCAL 封装一个静态方法, 方便后续使用直接调用 避免对外暴露导致的修改
     * 封装了一个获取线程局部变量值的静态方法
     * @return
     */
    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    /**
     * 后置方法, 在 Controller 方法执行之后执行
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("后置方法, 在 Controller 方法执行之后执行");
    }

    /**
     * 完成方法, 在 视图渲染 完成之后执行
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        System.out.println("完成方法, 在 视图渲染 完成之后执行");

        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        THREAD_LOCAL.remove();
    }
}
