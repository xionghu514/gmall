package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import com.atguigu.gmall.payment.interceptors.LoginInterceptor;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/29 06:35
 * @Email: moumouguan@gmail.com
 */
@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private GmallOmsClient omsClient;

    @GetMapping("pay.html")
    public String pay(@RequestParam("orderToken") String orderToken, Model model) {

        // 根据订单编号查询订单
        OrderEntity orderEntity = paymentService.queryOrderByToken(orderToken);

        // 订单是否为空
        if (orderEntity == null) {
            throw new OrderException("您要支付的订单不存在!");
        }

        // 校验是否是属于订单所有者
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if (orderEntity.getUserId() != userId) {
            // 不属于订单所有者
            throw new OrderException("该订单不属于你");
        }

        // 判断订单状态是否是未支付状态
        if (orderEntity.getStatus() != 0) {
            throw new OrderException("该订单不可以支付");
        }

        model.addAttribute("orderEntity", orderEntity);

        return "pay";
    }
}
