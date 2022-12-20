package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/20 19:17
 * @Email: moumouguan@gmail.com
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    /**
     * joinPoint.getArgs(); 获取方法参数
     * joinPoint.getTarget().getClass(); 获取目标类
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 方法对象
        Method method = signature.getMethod();
        // 获取方法上的GmallCache注解对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);

        // 方法形参
        Object[] args = joinPoint.getArgs();
        // 获取缓存前缀
        String prefix = gmallCache.prefix();
        // 缓存的key
        String argString = StringUtils.join(args, ",");
        String key = prefix + argString;

        // 通过布隆过滤器判断数据是否存在，不存在则直接返回空
        if (!bloomFilter.contains(key)){
            return null;
        }

        // 1.先查询缓存，如果缓存中命中则直接返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json, signature.getReturnType());
        }

        // 2.为了防止缓存击穿，添加分布式锁
        String lock = gmallCache.lock() + argString;
        RLock fairLock = this.redissonClient.getFairLock(lock);
        fairLock.lock();

        try {
            // 3.当前请求获取锁的过程中，可能有其他请求已经把数据放入缓存，此时，可以再次查询缓存，如果命中则直接返回
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)){
                return JSON.parseObject(json2, signature.getReturnType());
            }

            // 4.执行目标方法，从数据库中获取数据
            Object result = joinPoint.proceed(args);

            // 5.把数据放入缓存并释放分布式锁
            int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random()); // 可以防止缓存雪崩
            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.MINUTES);

            return result;
        } finally {
            fairLock.unlock();
        }
    }

}
