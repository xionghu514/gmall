package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/24 02:19
 * @Email: moumouguan@gmail.com
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties properties;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        // 1. 根据登陆名和密码调用 ums 的接口查询用户
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        // 2. 判空, 如果为空说明登陆名或者密码错误则抛出异常. 告诉用户账号或密码错误
        if (userEntity == null) {
            throw new AuthException("您的用户名或者密码错误");
        }

        // 3. 组装载荷
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userEntity.getId());
        map.put("username", userEntity.getUsername());
        // 为了防止盗用, 在载荷中添加登陆用户的 ip 地址
        String ipAddress = IpUtils.getIpAddressAtService(request);
        map.put("ip", ipAddress);

        try {
            // 4. 生成 jwt 类型的 token
            String token = JwtUtils.generateToken(map, properties.getPrivateKey(), properties.getExpire());

            // 5. 放入 cookie
            CookieUtils.setCookie(
                    request, response, properties.getCookieName(),
                    token, properties.getExpire() * 60
            );

            // 6. 昵称展示
            CookieUtils.setCookie(
                    request, response, properties.getUnick(),
                    userEntity.getNickname(), properties.getExpire() * 60
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}