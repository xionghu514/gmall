package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.interceptors.LoginInterceptor;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private AlipayTemplate alipayTemplate;

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

    @GetMapping("alipay.html")
    @ResponseBody // html -> xml 以其他视图形式展示方法的返回结果集
    public Object alipay(@RequestParam("orderToken") String orderToken) {
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

        try {
            // TODO: 调用支付宝的支付接口, 打开支付页面
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());

            // 实际上线应该使用实时价格
            // payVo.setTotal_amount(orderEntity.getPayAmount().toString());

            // 测试设置 0.01 即可
            payVo.setTotal_amount("0.01");
            payVo.setSubject("谷粒商城支付平台");

            // 把支付信息保存到数据库. 对账信息
            Long payId = paymentService.savePaymentInfo(payVo);
            payVo.setPassback_params(payId.toString());

            // 跳转到支付页
            return alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new OrderException("支付出错，请刷新后重试！");
        }
    }
}
