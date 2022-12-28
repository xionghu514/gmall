package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/15 21:20
 * @Email: moumouguan@gmail.com
 */
@Configuration // 声明该类是一个配置类
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct // 构造方法执行之后就会执行, 项目时添加此配置类 调用该类的无参构造方法初始化. 添加该注解构造方法执行之后就会执行设置两个回调
//    @PreDestroy // 对象销毁之前执行
    public void init() {
        // 确认消息是否到达交换机的回调, 不管消息是否到达交换机都会执行
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息已到达交换机");
            } else {
                System.err.println("消息没有达到交换机: 原因 " + cause);
            }
        });

        // 确认消息是否到达队列的回调, 只有消息没有到达队列才会执行
        // 例如 消息没有达到交换机: 原因 channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'spring_test_exchange2' in vhost '/admin', class-id=60, method-id=40)
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) ->
                System.err.println("消息没有到达队列: " + " 交换机 " + exchange + " 路由键 " + routingKey
                        + " 消息内容 " + replyText + " 状态码 " + replyCode + " 消息内容 " + new String(message.getBody()))
        );
    }

    /**
     * 定时关单操作流程
     *      创建订单成功发送消息给交换机, 通过交换机转发消息给延迟队列. 这个队列会有定的延迟时间.
     *      时间到了会把信息转发给死信交换机, 死信交换机转发消息到死信息队列. oms 配置监听执行关单操作 并发送消息给 wms 服务 进行解锁库存
     */

    /**
     * 业务交换机: ORDER_EXCHANGE
     */

    /**
     * 延迟队列: ORDER_TTL_QUEUE
     */
    @Bean
    public Queue ttlQueue() {
        // 返回一个持久化 生存时间是 9 分钟的延迟队列(单位是浩渺)
        return QueueBuilder.durable("ORDER_TTL_QUEUE")
                .ttl(90000)
                // 时间到了经过那个死信交换机进入那个死信队列
                .deadLetterExchange("ORDER_EXCHANGE") // 指定死信交换机名称
                // rk
                .deadLetterRoutingKey("order.dead") // 经过什么 rk 会将消息转发到死信队列
                .build();
    }

    /**
     * 把延迟队列绑定到业务交换机: rk = order.ttl
     */
    @Bean
    public Binding ttlBinding() {
//    public Binding ttlBinding(Queue ttlQueue) {
//        return BindingBuilder
                // 把什么队列绑定到什么交换机
//                .bind(ttlQueue)
//                .to() // 业务交换机 是通过注解方式生成, 此处拿不到.

        // 只能通过构造方法声明, 把 ORDER_TTL_QUEUE 队列通过 order.ttl rk 绑定到 ORDER_EXCHANGE 交换机
        return new Binding("ORDER_TTL_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.ttl", null);
    }

    /**
     * 死信交换机: ORDER_EXCHANGE
     */

    /**
     * 死信队列: ORDER_DEAD_QUEUE
     */
    @Bean
    public Queue deadQueue() {
        // 死信队列本质就是一个普通的队列
        return QueueBuilder.durable("ORDER_DEAD_QUEUE").build();
    }

    /**
     * 把死信队列绑定到死信交换机: order.dead
     */
    @Bean
    public Binding deadBinding() {
        return new Binding("ORDER_DEAD_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.dead", null);
    }
}
