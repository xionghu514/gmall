package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 23:22
 * @Email: moumouguan@gmail.com
 */
public interface GmallOmsApi {

    @PostMapping("oms/order/save/{userId}")
    public ResponseVo saveOrder(@RequestBody OrderSubmitVo orderSubmitVo, @PathVariable("userId") Long userId);

    /**
     * 支付 1. 根据订单编号查询订单的接口
     * @param orderToken
     * @return
     */
    @GetMapping("oms/order/token/{orderToken}")
    public ResponseVo<OrderEntity> queryOrderByToken(@PathVariable("orderToken") String orderToken);
}
