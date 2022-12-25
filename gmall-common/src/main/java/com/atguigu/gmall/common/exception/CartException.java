package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/25 22:36
 * @Email: moumouguan@gmail.com
 */
public class CartException extends RuntimeException {
    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }
}
