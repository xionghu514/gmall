package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 05:54
 * @Email: moumouguan@gmail.com
 */
public class OrderException extends RuntimeException {

    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
