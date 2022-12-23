package com.atguigu.gmall.item;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 17:27
 * @Email: moumouguan@gmail.com
 */
@SpringBootTest
public class ThreadTests {

    @Test
    public void test() {
        MyThread myThread = new MyThread();
        myThread.start();

        System.out.println("main 方法 " + Thread.currentThread().getName());
    }

    @Test
    public void test2() {
        new Thread(() -> {
            System.out.println("使用 lambda 实现 runnable 接口 实现多线程 " + Thread.currentThread().getName());
        }).start();

        System.out.println("main 方法 " + Thread.currentThread().getName());
    }

    @Test
    public void test3() {
        /**
         * 做桥梁作用
         *      class FutureTask<V> implements RunnableFuture<V> // 本质是 Runnable
         *          interface RunnableFuture<V> extends Runnable, Future<V>
         *              Future 可以获取子任务的返回结果集, 定义一个未来任务
         */
        FutureTask futureTask = new FutureTask<>(new MyCallable()); // 把 Callable 作为参数使用
        new Thread(futureTask).start();

        while (!futureTask.isDone()) { // 轮询的方式查看子任务的执行状态
            System.out.println("子任务没有执行完成");
        }

        try {
            // 在某一个时刻 可以通过 Future 获取子任务的结果集
            System.out.println("获取了子任务的结果集" + futureTask.get()); // get() 阻塞方式获取子任务的返回结果集
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("main 方法 " + Thread.currentThread().getName());
    }

    @Test
    public void test4() {
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
         *              一般在不允许失败的、对性能要求不高、并发量较小的场景下使用，因为线程池一般情况下不会关闭，也就是提交的任务一定会被运行
         *              但是由于是调用者线程自己执行的，当多次提交任务时，就会阻塞后续任务执行，性能和效率自然就慢了
         *          DiscardPolicy 丢弃策略: 直接静悄悄的丢弃这个任务，不触发任何动作
         *              如果你提交的任务无关紧要，你就可以使用它 。因为它就是个空实现，会悄无声息的吞噬你的的任务。所以这个策略基本上不用了
         *          DiscardOldestPolicy 弃老策略: 如果线程池未关闭，就弹出队列头部的元素，然后尝试执行
         *              这个策略还是会丢弃任务，丢弃时也是毫无声息，但是特点是丢弃的是老的未执行的任务，而且是待执行优先级较高的任务。
         *              基于这个特性，我能想到的场景就是，发布消息，和修改消息，当消息发布出去后，还未执行，此时更新的消息又来了，这个时候未执行的消息的版本比现在提交的消息版本要低就可以被丢弃了。
         *              因为队列中还有可能存在消息版本更低的消息会排队执行，所以在真正处理消息的时候一定要做好消息的版本比较
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
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 15, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(20), Executors.defaultThreadFactory());

        executorService.execute(() -> {
            System.out.println("这是线程池初始化了程序：" + Thread.currentThread().getName());
        });
    }

}

class MyCallable implements Callable<String> { // 范型指定了返回结果类型
    @Override
    public String call() throws Exception { // 可以抛异常
        System.out.println("使用 Callable 接口实现了多线程程序" + Thread.currentThread().getName());
        return "Hello Callable";
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("使用 继承 Thread 基类实现多线程 " + Thread.currentThread().getName());
    }
}