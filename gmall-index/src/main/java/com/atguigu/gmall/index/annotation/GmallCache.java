package com.atguigu.gmall.index.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/20 19:11
 * @Email: moumouguan@gmail.com
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 设置缓存的有效时间
     * 单位：分钟
     * @return
     */
    int timeout() default 5;

    /**
     * 防止雪崩设置的随机值范围
     * @return
     */
    int random() default 10;

    /**
     * 防止击穿，分布式锁的key
     * @return
     */
    String lock() default "gmall:lock:";
}
