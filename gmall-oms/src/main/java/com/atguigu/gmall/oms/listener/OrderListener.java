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

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS_FALL_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.failure"}
    ))
    public void failOrder(String orderToken, Message message, Channel channel) throws IOException {

        if (StringUtils.isBlank(orderToken)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

        // 更新订单状态为无效订单
        orderMapper.updateStatus(orderToken, 0, 5);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
