package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/17 10:24
 * @Email: moumouguan@gmail.com
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:";

    public List<CategoryEntity> queryLvl1Categories() {
        // 通过已有接口直接调用 传参 0 即可查询全部一级分类
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryCategoriesByPid(0L);

        return categoryResponseVo.getData();
    }

    // 一般 自定义前缀 + 请求参数作为 key, 返回结果集作为 value 放入缓存
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        // 1. 先查询缓存, 如果缓存命中则返回
        String json = redisTemplate.opsForValue().get(
                // key, value
                KEY_PREFIX + pid
        );
        // 不为空则
        if (StringUtils.isNotBlank(json)) {
            // 将 Json 字符串数据转换成集合对象
            return JSON.parseArray(json, CategoryEntity.class); // json 字符串, 转换类型
        }

        // 2. 远程调用, 查询数据库 并 放入缓存
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLevel23CategoriesByPid(pid);
        List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

        if (CollectionUtils.isNotEmpty(categoryEntities)) {
            // 正常数据放入缓存 90 天

            /**
             * 缓存雪崩: 由于缓存时间一样, 导致缓存同时失效, 此时大量请求访问这些数据, 请求就会直达数据库, 导致服务器宕机
             * 　    解决方案: 给缓存时间添加随机值 90 + new Random().nextInt(10)
             */
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // k, v
                    90 + new Random().nextInt(10), TimeUnit.DAYS // 缓存时间 90 天
            );
        } else {
            /**
             * 缓存穿透(数据为空): 大量请求访问不存在的数据, 由于数据不存在, redis 中可能没有, 此时大量请求没有到达数据库, 导致服务器宕机
             * 　    基础解决方案: 即使为 null 也缓存, 缓存时间一般不超过 5 分钟
             *       依然存在待解决的问题:  如果每次访问不存在且不重复的数据即使缓存为null 的值 请求依然会直达数据库 应该使用 布隆过滤器
             */
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // k, v
                    5, TimeUnit.MINUTES // 缓存时间 5 分钟
            );
        }

        return categoryEntities;
    }

    Integer i = 1;

    /**
     * 测试. 每次 将 number 值设置为 0 通过 ab 压测工具 ab -n 5000 -c 100 192.168.0.111:8888/index/test/lock 测试高并发下是否出现并发问题(number 未到 5000 即出现并发问题)
     *  ab压测: ab  -n（一次发送的请求数）  -c（请求的并发数） 访问路径
     *      1. 将 number 值设置为 0, 通过浏览器访问 每访问一次 number + 1
     *      2. 通过 ab 压测工具 ab -n 5000 -c 100 192.168.0.111:8888/index/test/lock 测试高并发下是否出现并发问题(number 未到 5000 即出现并发问题)
     *          最终 number 值为 156 出现并发性问题
     *      3. 添加 synchronized jvm 锁 将 number 设置为 0 压测 5000
     *          最终 number 值为 5000, 要注意的是 这只是单机工程
     *      4. copy 2 份实例 将 number 设置为 0 压测 5000
     *          最终 number 值为 2453, 出现并发性问题. 理论值在 5000 / 3 至 5000(极限情况下 3 台服务同时放入一个线程 同时到达 都将 num 转换为某一个数字 ++.)
     */
    public synchronized void testLock() {
        // 查询 redis 中的 num 值
        String number = redisTemplate.opsForValue().get("number");

        // 没有该值设置默认值
        if (StringUtils.isBlank(number)) {
            redisTemplate.opsForValue().set("number", "1");
        }
        // 有值转换成 int
        int num = Integer.parseInt(number);
        // 把 redis 中的 num 值 +1
        redisTemplate.opsForValue().set("number", String.valueOf(++num));
    }
}
