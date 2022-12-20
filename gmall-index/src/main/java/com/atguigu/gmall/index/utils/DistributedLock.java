package com.atguigu.gmall.index.utils;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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

}
