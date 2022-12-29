package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.interceptors.LoginInterceptor;
import com.atguigu.gmall.payment.pojo.PaymentInfoEntity;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;

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

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    @GetMapping("pay/success")
    public String paySuccess(@RequestParam("out_trade_no") String out_trade_no) {
        // TODO: 获取订单编号查询订单
        System.out.println("同步回调");

        // 页面跳转
        return "paysuccess";
    }

    @PostMapping("pay/ok")
    @ResponseBody
    public Object payOk(PayAsyncVo payAsyncVo) {
        // TODO: 修改订单状态
//        System.out.println("异步回调");

        // 1. 验签: 确保是支付宝发送的
        Boolean flag = alipayTemplate.checkSignature(payAsyncVo);
        if (!flag) {
            // TODO: 验签失败则记录异常日志
            return "failure"; // 支付失败
        }

        // 2. 验签成功后, 按照支付结果异步通知中的描述, 对支付结果中的业务内容进行二次校验
        String app_id = payAsyncVo.getApp_id();
        String out_trade_no = payAsyncVo.getOut_trade_no();
        String total_amount = payAsyncVo.getTotal_amount();
        String payId = payAsyncVo.getPassback_params();
        PaymentInfoEntity paymentInfoEntity = paymentService.queryPaymentInfoById(payId);
        if (!StringUtils.equals(app_id, alipayTemplate.getApp_id()) ||
                !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo()) ||
                new BigDecimal(total_amount).compareTo(paymentInfoEntity.getTotalAmount()) != 0) {
            return "failure"; // 支付失败
        }

        // 3. 校验支付状态
        if (!StringUtils.equals("TRADE_SUCCESS", payAsyncVo.getTrade_status())) {
            return "failure";
        }

        // 4. 正常的支付成功, 记录支付方便对账
        paymentInfoEntity.setTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        paymentInfoEntity.setPaymentStatus(1); // 已支付
        if (paymentService.updatePaymentInfo(paymentInfoEntity) == 1) {
            // 5. 发送消息更新订单状态, 并减库存
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.pay", out_trade_no);
        }

        // 给支付宝成功回执
        return "success";
    }
}
