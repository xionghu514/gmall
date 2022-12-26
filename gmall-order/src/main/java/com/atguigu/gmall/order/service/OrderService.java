package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 02:44
 * @Email: moumouguan@gmail.com
 */
@Service
public class OrderService {

    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        confirmVo.setAddresses(null);
        confirmVo.setItems(null);
        confirmVo.setBounds(null);
        confirmVo.setOrderToken(null);

        return confirmVo;
    }
}
