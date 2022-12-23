package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 线程池配置类: 一个工程只有一个线程池才能控制线程数
 * @Author: Guan FuQing
 * @Date: 2022/12/23 11:50
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 线程池
     *      corePoolSize 核心线程数
     *      maximumPoolSize 最大可扩展线程数
     *      keepAliveTime 生存时间
     *      unit 时间单位
     *      workQueue 阻塞队列, 创建一个固定大小阻塞队列
     *      threadFactory 线程工厂, 默认
     *      handler 拒绝策略
     *          AbortPolicy 中止策略 默认: 当触发拒绝策略时，直接抛出拒绝执行的异常，中止策略的意思也就是打断当前执行流程
     *              要正确处理抛出的异常
     *          CallerRunsPolicy 调用者运行策略: 当触发拒绝策略时，只要线程池没有关闭，就由提交任务的当前线程处理
     *              一般在不允许失败的、对性能要求不高、并发量较小的场景下使用，因为线程池一般情况下不会关闭，也就是提交的任务一定会被运行，但是由于是调用者线程自己执行的，当多次提交任务时，就会阻塞后续任务执行，性能和效率自然就慢了
     *          DiscardPolicy 丢弃策略: 直接静悄悄的丢弃这个任务，不触发任何动作
     *              如果你提交的任务无关紧要，你就可以使用它 。因为它就是个空实现，会悄无声息的吞噬你的的任务。所以这个策略基本上不用了
     *          DiscardOldestPolicy 弃老策略: 如果线程池未关闭，就弹出队列头部的元素，然后尝试执行
     *              这个策略还是会丢弃任务，丢弃时也是毫无声息，但是特点是丢弃的是老的未执行的任务，而且是待执行优先级较高的任务。基于这个特性，我能想到的场景就是，发布消息，和修改消息，当消息发布出去后，还未执行，此时更新的消息又来了，这个时候未执行的消息的版本比现在提交的消息版本要低就可以被丢弃了。因为队列中还有可能存在消息版本更低的消息会排队执行，所以在真正处理消息的时候一定要做好消息的版本比较
     *
     *      工作流程:
     *          1. 初始化一个线程池, 线程数为 0
     *          2. execute 或者 submit 向线程池提交一个任务, 做出以下判断
     *              1. 核心线程数是否已满, 未满创建线程 处理该任务, 已满查看阻塞队列是否已满
     *                  1. 阻塞队列 未满 放入阻塞队列
     *                  2. 阻塞队列 已满 查看最大可扩展线程数 是否已满
     *                      1. 最大可扩展线程数 未满 创建线程 处理该任务
     *                      2. 最大可扩展线程数 已满 查看拒绝策略
     *          3. 任务处理完 会从阻塞队列 获取下一个任务处理
     *          4. 任务都处理完 线程空闲时间超过生存时间, 并且线程数大于核心线程数的情况下 则销毁当前线程 直至 核心线程数
     */
    @Bean
    public ExecutorService executorService(
            // 读取配置文件配置
            @Value("${threadPool.corePoolSize}") Integer corePoolSize,
            @Value("${threadPool.maximumPoolSize}") Integer maximumPoolSize,
            @Value("${threadPool.keepAliveTime}") Integer keepAliveTime,
            @Value("${threadPool.blockQueueSize}") Integer blockQueueSize
    ) {
        return new ThreadPoolExecutor(
                // 核心线程数, 最大可扩展线程数, 过期时间, 时间单位
                corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                // 阻塞队列
                new ArrayBlockingQueue<>(blockQueueSize)
        );
    }

}
