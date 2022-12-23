package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/24 02:11
 * @Email: moumouguan@gmail.com
 */
public class JwtTest {
    // 别忘了创建 /rsa 目录
    private static final String pubKeyPath = "/Users/admin/Documents/project/learn/gmall/gmall-auth/src/main/resources/rsa/rsa.pub";
    private static final String priKeyPath = "/Users/admin/Documents/project/learn/gmall/gmall-auth/src/main/resources/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    // 生成公私钥, 这一步使用前需要把 BeforeEach 注释掉
    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    // 生成 token
    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    // 解析 token
    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2NzE4MTk0Nzh9.a-OGL_wgt9OE81ATJPTQQyXHVHq-7oriOs0xlULEnWPaSJs-Nyk3oMsCleDzViEKpJRjXU1VlGzaTZ_7H7xDCRDi74p3Ckip5iCr3ORz6lKB1LOdypi-HH9ZlhM5tkYhm2fM3F_dlw4qOEn07slJuV6Z-m-Ma-F23cz4xAy72wAxCLcuNU8okl5UxI6cREXOCEGJXmfXdAt9zlxsmiJvzyrptyJJfCQMmh6vNxljVOAmmzMg9TpJdvekbrDNwXPt828nw3X-4wn5575HO6hHCe7c-scK-D-00s1-4iuwpIp7Aou10gOOd0sw95aa0uPGi9lL6o7ihQjtn5AhN6KcLg";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
