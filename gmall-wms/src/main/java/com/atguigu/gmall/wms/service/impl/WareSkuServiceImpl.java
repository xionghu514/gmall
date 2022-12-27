package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String LOCK_PREFIX = "STOCK:LOCK:";
    private static final String KEY_PREFIX = "STOCK:INFO:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        if (CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException("请选择要购买的商品");
        }

        // 遍历验库存并锁库存
        lockVos.forEach(lockVo -> {
            checkLock(lockVo);
        });

        // 判断是否存在验库存并锁库存失败的商品, 如果存在则把锁定成功的库存解锁
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) {
            // 获取锁定成功的库存, 解锁
            lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList())
                    .forEach(lockVo ->
                            // 解锁时 不需要 加速
                            wareSkuMapper.unlock(
                                    lockVo.getWareSkuId(), lockVo.getCount()
                            )
                    );
            // 锁定失败
            return lockVos;
        }

        // 把库存的锁定信息保存到redis中，以方便将来解锁库存(用户下单不支付, 或者支付成功减库存)
        redisTemplate.opsForValue().set(LOCK_PREFIX + orderToken, JSON.toJSONString(lockVos), 26, TimeUnit.HOURS); // 注意 24 小时不支持就会自动关单 过期时间不得低于 24 小时

        // 如果验库存并锁库存成功, 返回 null
        return null;
    }

    // 使用分布式锁, 验完立刻锁住
    private void checkLock(SkuLockVo lockVo) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockVo.getSkuId());
        lock.lock();

        try {
            // 1. 验库存
            List<WareSkuEntity> wareSkuEntities = wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());
            // 如果满足条件的库存为空, 则验证库存失败
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                // 验库存 锁库存失败
                lockVo.setLock(false);
                lock.unlock(); // 程序返回之前，一定要释放锁
                return;
            }

            // 2. 锁库存: 大数据分析最佳仓库, 这里取第一个仓库
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            if (wareSkuMapper.lock(wareSkuEntity.getId(), lockVo.getCount()) == 1) {
                lockVo.setLock(true);
                // 需要记录锁定仓库 id
                lockVo.setWareSkuId(wareSkuEntity.getId());
            } else {
                lockVo.setLock(false);
            }
        } finally {
            lock.unlock();
        }

    }

}