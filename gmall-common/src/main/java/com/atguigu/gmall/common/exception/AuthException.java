package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/24 02:21
 * @Email: moumouguan@gmail.com
 */
public class AuthException extends RuntimeException{
    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }
}
