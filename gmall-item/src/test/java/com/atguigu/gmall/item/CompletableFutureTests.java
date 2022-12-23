package com.atguigu.gmall.item;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Description: CompletableFuture 异步编排、非阻塞方式获取返回结果集
 *      Future是Java 5添加的类，用来描述一个异步计算的结果。你可以使用`isDone`方法检查计算是否完成，或者使用`get`阻塞住调用线程，直到计算完成返回结果，
 *      你也可以使用`cancel`方法停止任务的执行
 *
 *      虽然`Future`以及相关使用方法提供了异步执行任务的能力，但是对于结果的获取却是很不方便，只能通过阻塞或者轮询的方式得到任务的结果。
 *      阻塞的方式显然和我们的异步编程的初衷相违背，轮询的方式又会耗费无谓的CPU资源，而且也不能及时地得到计算结果
 *
 *      在Java 8中, 新增加了一个包含50个方法左右的类: CompletableFuture，提供了非常强大的Future的扩展功能，可以帮助我们简化异步编程的复杂性，
 *      提供了函数式编程的能力，可以通过回调的方式处理计算结果，并且提供了转换和组合CompletableFuture的方法
 *
 *      CompletableFuture类实现了Future接口，所以你还是可以像以前一样通过`get`方法阻塞或者轮询的方式获得结果，但是这种方式不推荐使用。
 *      CompletableFuture和FutureTask同属于Future接口的实现类，都可以获取线程的执行结果
 *
 *      所有以 Async 结尾的方法都是异步方法, 所有异步方法都有重载带线程池的方法
 *
 * @Author: Guan FuQing
 * @Date: 2022/12/23 10:25
 * @Email: moumouguan@gmail.com
 */
@SpringBootTest
public class CompletableFutureTests {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        /**
         * 初始化方法: 都有重载的带线程池的方法
         *      1. 没有返回结果集
         *          static CompletableFuture<Void> runAsync(Runnable runnable)
         *          static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
         *      2. 有返回结果集
         *          static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
         *          static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
         */
        CompletableFuture.runAsync(() -> {
            System.out.println("通过 CompletableFuture 的 runAsync 初始化了一个多线程程序");
        });

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过 CompletableFuture 的 supplyAsync 初始化了一个多线程程序");
            return "Hello CompletableFuture";
        });

        System.out.println(future.get()); // 阻塞方式获取异步任务的返回结果集

        System.out.println("---------- main -----------");

    }

    @Test
    public void test2() {
        /**
         * 计算完成时方法
         *      1. 不管子任务是否有异常都会执行, 即可以获取返回结果集, 也可以获取异常信息
         *          public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super Throwable> action);
         *          public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action);
         *          public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor);
         *      2. 只有出现异常才会执行
         *          public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn);
         */
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过 CompletableFuture 的 supplyAsync 初始化了一个多线程程序");
//            int i = 1 / 0;
            return "Hello CompletableFuture";
        }).whenCompleteAsync((t, u) -> {
            System.out.println("-------------------------whenComplete----------------------");
            System.out.println("t = " + t); // 子任务的返回结果集
            System.out.println("u = " + u); // 子任务的异常信息 java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
        }).exceptionally(t -> {
            System.out.println("-------------------------exceptionally----------------------");
            System.out.println("t = " + t); // 子任务的异常信息 java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
            return "Hello exceptionally";
        });

        System.out.println("---------- main -----------");

    }

    // 串行执行
    @Test
    public void test3() throws IOException {
        /**
         * 线程串行化方法: 带有Async默认是异步执行的。这里所谓的异步指的是不在当前线程内执行。
         *      1. thenApply 方法：当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
         *      即可以获取上一个任务的返回结果集, 也有自己的返回结果集
         *          public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
         *          public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
         *          public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
         *
         *      2. thenAccept方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
         *      获取上一个任务的返回结果集, 但是没有自己的返回结果集
         *          public CompletionStage<Void> thenAccept(Consumer<? super T> action);
         *          public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
         *          public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
         *
         *      3. thenRun方法：只要上面的任务执行完成，就开始执行thenRun，只是处理完任务后，执行 thenRun的后续操作
         *      即不获取上一个任务的返回结果集, 也没有自己的返回结果集
         *          public CompletionStage<Void> thenRun(Runnable action);
         *          public CompletionStage<Void> thenRunAsync(Runnable action);
         *          public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
         */
        CompletableFuture.supplyAsync(() -> {
            System.out.println("通过 CompletableFuture 的 supplyAsync 初始化了一个多线程程序");
//            int i = 1 / 0;
            return "Hello CompletableFuture";
        }).thenApplyAsync(t -> {
            System.out.println("----------------------- thenApplyAsync ---------------------");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回结果集：" + t);
            return "Hello thenApplyAsync";
        }).thenAcceptAsync(t -> {
            System.out.println("----------------------- thenAcceptAsync ---------------------");
            System.out.println("上一个任务返回结果集2：" + t);
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).thenRunAsync(() -> {
            System.out.println("----------------------- thenRunAsync ---------------------");
            System.out.println("不获取上一个任务的返回结果集, 也没有自己的返回结果集");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("main 方法" + Thread.currentThread().getName());
        System.in.read(); // 阻塞main 线程
    }

    // 并行执行
    @Test
    public void test4() throws IOException {
        CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过 CompletableFuture 的 supplyAsync 初始化了一个多线程程序");
//            int i = 1 / 0;
            return "Hello CompletableFuture";
        });

        async.thenApplyAsync(t -> {
            System.out.println("----------------------- thenApplyAsync ---------------------");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回结果集：" + t);
            return "Hello thenApplyAsync";
        });

        async.thenAcceptAsync(t -> {
            System.out.println("----------------------- thenAcceptAsync ---------------------");
            System.out.println("上一个任务返回结果集2：" + t);
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        async.thenRunAsync(() -> {
            System.out.println("----------------------- thenRunAsync ---------------------");
            System.out.println("不获取上一个任务的返回结果集, 也没有自己的返回结果集");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("---------- main -----------");

        System.in.read();
    }

    @Test
    public void tes5() {
        CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过 CompletableFuture 的 supplyAsync 初始化了一个多线程程序");
//            int i = 1 / 0;
            return "Hello CompletableFuture";
        });

        CompletableFuture<String> future = async.thenApplyAsync(t -> {
            System.out.println("----------------------- thenApplyAsync ---------------------");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回结果集：" + t);
            return "Hello thenApplyAsync";
        });

        CompletableFuture<Void> future2 = async.thenAcceptAsync(t -> {
            System.out.println("----------------------- thenAcceptAsync ---------------------");
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回结果集2：" + t);
        });

        CompletableFuture<Void> future3 = async.thenRunAsync(() -> {
            System.out.println("----------------------- thenRunAsync ---------------------");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("不获取上一个任务的返回结果集, 也没有自己的返回结果集");
        });

        /**
         * 组合任务
         *      1. 所有任务都执行完才放行
         *          public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs);
         *      2. 任何一个任务执行完就放行
         *          public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs);
         */
        CompletableFuture.allOf(future, future2, future3).join(); // join 阻塞当前线程
        // 依然可以通过 then 去串任务
//        CompletableFuture.allOf(future, future2, future3).thenAcceptAsync()
//        CompletableFuture.allOf(future, future2, future3).thenApplyAsync()
//        CompletableFuture.allOf(future, future2, future3).thenRunAsync()

        // 任何一个任务执行完就放行
//        CompletableFuture.anyOf(future, future2, future3).join();

        System.out.println("---------- main -----------");
    }
}
