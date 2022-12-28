package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/29 06:37
 * @Email: moumouguan@gmail.com
 */
@Service
public class PaymentService {

    @Autowired
    private GmallOmsClient omsClient;

    public OrderEntity queryOrderByToken(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = omsClient.queryOrderByToken(orderToken);
        OrderEntity orderEntity = orderEntityResponseVo.getData();

        return orderEntity;
    }
}
