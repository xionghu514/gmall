package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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

    @Autowired
    private DistributedLock distributedLock;

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
     *
     * 基于 redis 实现分布式锁。借助于 setnx 指令 当 key 不存在即设置成功返回 1 当 key 存在即设置失败返回 0(加锁 解锁 重试)
     *      分布式锁特征: 独占排他互斥使用
     *
     *      存在的问题
     *          1. 死锁
     *              一个线程获取到锁 还没有执行到释放锁操作 服务器宕机. 其他线程获取不到锁 即使 服务器重启 这把锁也无法被释放掉. 其他线程一直执行递归操作 最终导致服务器资源耗尽而宕机
     *                  添加过期时间在 set(获取锁) 时去设置过期时间
     *
     *              不可重入可能会导致死锁
     *          2. 防误删
     *              如果业务逻辑的执行时间是7s, A 服务获取锁 业务没有执行完 锁3秒被自动释放, B 服务获取到锁 业务没有执行完 锁3秒被自动释放, C 服务获取锁执行业务逻辑.
     *              A 服务业务执行完成 释放锁, 这时释放的是 C 的锁. 导致 C 业务只执行了 1s 就被别人释放. 最终等于没有锁(可能会释放其他服务器的锁)
     *                  setnx 获取锁时, 设置一个指定的唯一值(例如：uuid); 释放前获取这个值, 判断是否自己的锁(注意删除缺乏原子性)
     *          3. 保证删除的原子性
     *              A 服务执行删除时 查询到 lock 值确实与 uuid 相等. A 服务删除前 lock 刚好过期时间已到 被 redis 释放. B 服务获取了锁 A 服务把 B 服务的锁释放 最终等于没有锁
     *                  判断与删除间也要保证原子性, 使用 lua 脚本保证删除的原子性
     *                      在 redis 中对lua 脚本提供了主动支持: 打印的不是 lua 脚本的 print 而是 lua 脚本的返回值
     *                          eval script numkeys key [key ...] arg [arg ...]
     *                              eval: 指令名称
     *                              script: lua 脚本字符串
     *                              numkeys: key 列表的元素数量. 必须参数
     *                              key: 传递的 key 列表. keys[index] 下标从 1 开始的
     *                              arg: 传递的 arg 列表. 同上
     *                          变量
     *                              全局变量: a = 5 redis 中的 lua 脚本不支持全局变量
     *                              局部变量: local a = 5
     *                      redis 给 lua 脚本提供了一个类库: redis.call()
     *          4. 可重入 hash Map<lockName, Map<uuid, 重入次数>>
     *              可重入加锁
     *                  1. 判断锁是否存在 (exists), 如果不存在 (0) 则直接获取锁 (hset)
     *                  2. 判断是否是自己的锁 (hexists), 如果是 (1) 则重入 (hinrby)
     *                  3. 否则获取锁失败 直接返回 (0) false
     *
     *                  if(redis.call('exists', 'lock') == 0 // 没有人占用该锁
     *                  then
     *                      // hset 锁名称 uuid 可重入次数
     *                      redis.call('hset', 'lock', uuid, 1) // 直接获取锁
     *                      // 设置过期时间
     *                      redis.call('expire', 'lock', 30)
     *                      return 1
     *                   elseif redis.call('hexists', 'lock', uuid) == 1 // 锁存在 是自己的锁
     *                   then
     *                      // 重入
     *                      redis.call('hincrby', 'lock', uuid, 1)
     *                      // 重制过期时间
     *                      redis.call('expire', 'lock', 30)
     *                      return 1
     *                   else
     *                      return 0
     *                   end
     *
     *                  // 简化
     *                  if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1)
     *                  then
     *                      redis.call('hincrby', KEYS[1], ARGV[1], 1);
     *                      redis.call('expire', KEYS[1], ARGV[2]);
     *                      return 1;
     *                  else
     * 	                    return 0;
     *                  end
     *
     *              可重入解锁
     *                  1. 判断自己的锁是否存在 (hexists), 如果不存在 (0), 则返回 nil
     *                  2. 如果自己的锁存在, 则 直接减 1(hincrby -1), 并判断减 1 后的值是否为 0, 为 0 则直接释放锁(del) 返回 1
     *                  3. 直接返回 0
     *
     *                  if redis.call('hexists', lock, uuid) == 0
     *                  then
     *                      return nil
     *                  elseif redis.call('hincrby', lock, uuid, -1) == 0
     *                  then
     *                      return redis.call('del', lock)
     *                  else
     *                      return 0
     *                  end
     */
    public synchronized void testLock() {
        String uuid = UUID.randomUUID().toString();
        // 加锁
        Boolean lock = distributedLock.tryLock("lock", uuid, 30);

        if (lock) {
            String number = redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                redisTemplate.opsForValue().set("number", "1");
            }
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number", String.valueOf(++num));

            // 可重入测试
            testSubLock2(uuid);

            distributedLock.unLock("lock", uuid);
        }
    }

    public synchronized void testLock2() {

        String uuid = UUID.randomUUID().toString();

        /**
         * setIfAbsent 类似与 setNx 当 key 不存在即设置成功 否 则 失败
         *      分布式锁本质就是对 key 的争抢, 谁先设置成功谁就先获取锁
         */
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS); // 解决死锁 添加过期时间在 set(获取锁) 时去设置过期时间

        if (!flag) {
            try {
                // 睡眠一段时间 让抢到锁的线程执行业务逻辑 减少竞争
                Thread.sleep(30);
                // 设置锁(加锁)失败重新调用该方法进行重试
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else { // 避免每重试一次多加一次

            // 添加过期时间 （缺乏原子性：如果在 setnx 和 expire 之间出现异常，锁也无法释放)
//            redisTemplate.expire("lock", 3, TimeUnit.SECONDS);

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

            testSubLock(); // 测试可重入性

            // 判断 redis 中 lock 值是否跟当前 uuid 一致, 如果一致则执行 del 指令
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then " +
                    "   return redis.call('del', KEYS[1])" +
                    "else " +
                    "   return 0 " +
                    "end";

            redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);

//            if (StringUtils.equals(redisTemplate.opsForValue().get("lock"), uuid)) { // 解锁时判断是否是自己的锁
//                // 释放锁
//                redisTemplate.delete("lock");
//            }
        }
    }

    public void testSubLock2(String uuid) {
        distributedLock.tryLock("lock", uuid, 30);
        System.out.println("-----------------");
        distributedLock.unLock("lock", uuid);
    }

    public void testSubLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS); // 解决死锁 添加过期时间在 set(获取锁) 时去设置过期时间

        // 一顿操作
        if (!flag) {
            try {
                // 睡眠一段时间 让抢到锁的线程执行业务逻辑 减少竞争
                Thread.sleep(30);
                // 设置锁(加锁)失败重新调用该方法进行重试
                testSubLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then " +
                "   return redis.call('del', KEYS[1])" +
                "else " +
                "   return 0 " +
                "end";

        redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
    }
}
