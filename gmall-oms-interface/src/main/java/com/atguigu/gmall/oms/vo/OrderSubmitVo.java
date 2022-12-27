package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 06:27
 * @Email: moumouguan@gmail.com
 */
@Data
public class OrderSubmitVo {

    // 用户选中的收货地址
    private UserAddressEntity address; // 收货人信息
    private Integer bounds; // 积分信息
    private String deliveryCompany; // 配送方式
    private List<OrderItemVo> items; // 订单详情信息
    private String orderToken; // 防重
    private Integer payType; // 支付方式
    private BigDecimal totalPrice; // 总价，校验价格变化

    // TODO: 发票信息

    // TODO: 营销信息

}
