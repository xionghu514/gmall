package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/29 03:26
 * @Email: moumouguan@gmail.com
 */
@Component
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS_FALL_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.failure"}
    ))
    public void failOrder(String orderToken, Message message, Channel channel) throws IOException {

        if (StringUtils.isBlank(orderToken)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 更新订单状态为无效订单
        orderMapper.updateStatus(orderToken, 0, 5);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 绑定已经在配置文件中绑定好了, 此处只用监听死信队列即可
     * @param orderToken
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = {"ORDER_DEAD_QUEUE"})
    public void closeOrder(String orderToken, Message message, Channel channel) throws IOException {

        if (StringUtils.isBlank(orderToken)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        /**
         * 更新订单状态为关闭状态
         */
        if (orderMapper.updateStatus(orderToken, 0, 4) == 1) {
            // 如果关闭订单成功, 则发送消息给 wms 解锁库存
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderToken);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 可能用户已经在个人找到 该订单并支付支付
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS_PAY_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken, Message message, Channel channel) throws IOException {

        if (StringUtils.isBlank(orderToken)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 更新订单状态为无效订单
        if (orderMapper.updateStatus(orderToken, 0, 1) == 1) {
            // 发送消息给 wms 减库存
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.minus", orderToken);
        } else {
            // 发送消息给 支付工程, 退款
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
