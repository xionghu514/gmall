package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/29 03:40
 * @Email: moumouguan@gmail.com
 */
@Component
public class StockListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String KEY_PREFIX = "STOCK:INFO:";
    
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("STOCK_UNLOCK_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.failure"}
    ))
    public void unlock(String orderToken, Message message, Channel channel) throws IOException {

        if (StringUtils.isBlank(orderToken)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 1. 根据 orderToken 获取锁定信息的缓存
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);

        // 2. 判空, 如果为空则直接确认消息
        if (StringUtils.isBlank(json)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 3. 不为空, 锁定信息集合, 并遍历解锁库存
        List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
        if (CollectionUtils.isEmpty(skuLockVos)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        skuLockVos.forEach(lockVo -> {
            wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
        });

        // 4. 删除锁定信息缓存
        redisTemplate.delete(KEY_PREFIX + orderToken);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
