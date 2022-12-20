package com.atguigu.gmall.index.utils;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description: 分布式锁
 * @Author: Guan FuQing
 * @Date: 2022/12/20 14:27
 * @Email: moumouguan@gmail.com
 */
@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Boolean tryLock(String lockName, String uuid, Integer expire) {
        String script = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                "   redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                "   redis.call('expire', KEYS[1], ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";

        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());

        if (!flag) {
            // 获取锁失败, 重试
            try {
                Thread.sleep(40);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 自动续期
        renewTime(lockName, uuid, expire);

        // 获取到锁, 返回true
        return true;
    }

    public void unLock(String lockName, String uuid) {
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 " +
                "then" +
                "    return nil" +
                "elseif redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0" +
                "then" +
                "    return redis.call('del', KEYS[1])" +
                "else" +
                "    return 0" +
                "end";
        // 这里之所以没有跟加锁一样使用 Boolean ,这是因为解锁 lua 脚本中，三个返回值含义如下：
        // 1 代表解锁成功，锁被释放
        // 0 代表可重入次数被减 1
        // null 代表其他线程尝试解锁，解锁失败
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Lists.newArrayList(lockName), uuid);
        // 如果未返回值，代表尝试解其他线程的锁
        if (result == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by lockName: "
                    + lockName + " with request: "  + uuid);
        }
    }

    /**
     * 锁延期
     * 线程等待超时时间的2/3时间后,执行锁延时代码,直到业务逻辑执行完毕,因此在此过程中,其他线程无法获取到锁,保证了线程安全性
     * @param lockName
     * @param expire 单位：毫秒
     */
    private void renewTime(String lockName, String uuid, Integer expire){
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 1" +
                "then" +
                "   redis.call('expire', KEYS[1], ARGV[2])" +
                "   return 1" +
                "else " +
                "   return 0" +
                "end";

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());

                if (flag) { // 如果续期失败 锁对象已被删除
                    // 如果续期成功, 递归调用开启下一次续期
                    renewTime(lockName, uuid, expire);
                }
            }
        }, expire * 1000 / 3, expire * 1000 / 3); // 延迟三分之一的过期时间, 每隔三分之一时间执行
    }

    public static void main(String[] args) {
        System.out.println("定时任务的初始时间 " + System.currentTimeMillis());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("定时器定时任务: " + System.currentTimeMillis());
            }
        }, 5, 10000);

//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
//        System.out.println("定时任务的初始时间 " + System.currentTimeMillis());
//        scheduledExecutorService.scheduleAtFixedRate(() -> {
//            System.out.println("juc 中的定时任务: " + System.currentTimeMillis());
//        }, 5, 10, TimeUnit.SECONDS);
    }

}
