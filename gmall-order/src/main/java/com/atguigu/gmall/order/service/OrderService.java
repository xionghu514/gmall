package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.feign.GmallOmsClient;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 02:44
 * @Email: moumouguan@gmail.com
 */
@Service
public class OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final static String ORDER_PREFIX = "ORDER:TOKEN:";

    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        // 1. 根据当前用户的id 查询收货地址列表
        ResponseVo<List<UserAddressEntity>> addressesResponseVo = umsClient.queryAddressesByUserId(userId);
        List<UserAddressEntity> addressEntities = addressesResponseVo.getData();
        confirmVo.setAddresses(addressEntities);
        
        // 2. 根据当前用户的id 查询已选中的购物车记录: skuId count(最后一次跟用户确认 其他字段应该从数据库实时获取)
        ResponseVo<List<Cart>> cartsResponseVo = cartClient.queryCheckedCartsByUserId(userId);
        List<Cart> carts = cartsResponseVo.getData();

        if (CollectionUtils.isEmpty(carts)) {
            throw new CartException("你没有选中的购物车");
        }

        // 把购物车集合转换成送货清单集合
        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(cart.getSkuId());
            orderItemVo.setCount(cart.getCount());

            // 根据 skuId 查询 sku
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new OrderException("你要下单的商品不存在");
            }
            orderItemVo.setTitle(skuEntity.getTitle());
            orderItemVo.setPrice(skuEntity.getPrice());
            orderItemVo.setWeight(skuEntity.getWeight());
            orderItemVo.setDefaultImage(skuEntity.getDefaultImage());

            // 根据 skuId 查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuEntity.getId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            orderItemVo.setSaleAttrs(skuAttrValueEntities);

            // 是否有货
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsClient.queryWareSkusBySkuId(skuEntity.getId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                orderItemVo.setStore(
                        wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)
                );
            }

            // 查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(skuEntity.getId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            orderItemVo.setSales(itemSaleVos);

            return orderItemVo;
        }).collect(Collectors.toList());

        confirmVo.setItems(items);

        // 查询购物积分
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();

        if (userEntity != null) {
            confirmVo.setBounds(userEntity.getIntegration());
        }

        // 防重唯一标识, 页面上 / redis
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);

        redisTemplate.opsForValue().set(ORDER_PREFIX + orderToken, orderToken, 24, TimeUnit.HOURS);

        return confirmVo;
    }

    public void submit(OrderSubmitVo submitVo) {
        // 1. 防重提交(保证幂等) 校验 redis 中是否存在 orderToken 如果存在立马删除并保证原子性
        String orderToken = submitVo.getOrderToken();

        if (StringUtils.isBlank(orderToken)) {
            throw new OrderException("非法请求 orderToken 不存在");
        }

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = this.redisTemplate.execute(
                new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(ORDER_PREFIX + orderToken), orderToken
        );
        if (!flag) {
            throw new OrderException("您多次提交过快，请稍后再试！");
        }

        // 2. 验价格: 验总价
        List<OrderItemVo> items = submitVo.getItems(); // 送货清单
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderException("请选择购买的商品!");
        }
        BigDecimal totalPrice = submitVo.getTotalPrice(); // 页面总价格
        // 实时总价格
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();

            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(item.getCount()); // 实时小记
            }
            return new BigDecimal(0);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("currentTotalPrice = " + currentTotalPrice + " " + totalPrice);

        if (currentTotalPrice.compareTo(totalPrice) != 0) {
            throw new OrderException("页面已过期, 请刷新页面后重试!");
        }

        // TODO. sku 表限制购买数量. 限购件数验证
        // 3. 验库存并锁库存
        List<SkuLockVo> lockVOS = items.stream().map(item -> {
            SkuLockVo skuLockVO = new SkuLockVo();
            skuLockVO.setSkuId(item.getSkuId());
            skuLockVO.setCount(item.getCount().intValue());
            skuLockVO.setOrderToken(submitVo.getOrderToken());
            return skuLockVO;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> skuLockResponseVo = wmsClient.checkAndLock(lockVOS, orderToken);
        List<SkuLockVo> skuLockVos = skuLockResponseVo.getData();
        if (CollectionUtils.isNotEmpty(skuLockVos)) {
            throw new OrderException("手慢了，商品库存不足：" + JSON.toJSONString(skuLockVos));
        }

        // 4. 创建订单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        try {
            omsClient.saveOrder(submitVo, userId);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: 发送消息给 wms 解锁库存
        }

        /**
         * 5. 删除购物车中对应的记录(可以通过异步的方式进行删除, 1. 购物车删除失败也不影响订单创建, 2. 删除购物车时效性不高 可以使用 MQ 异步 的方式节约一定时间.)
         *      分布式事务问题
         *
         *      发送消息内容
         *          1. 直接发送 userId, 根据 userId 把这个用户已选中的 购物车记录删除. 因为下单时是根据已选中的购物车进行下单的.
         *              存在的问题: 存在偏差, 用户购物车存在多个商品有已选中的 还有未选中的. 一开始下单时可能只选中了其中的某些商品 还有些商品是为选中状态.
         *              到达订单确认页面 可能又会回到购物车页面对未选中的商品 或者已选中的商品重新操作. 真正提交订单可能在这种过程中发送改变.
         *              如果仅仅根据 userId 删除已选中的记录 就会导致真正提交订单的与删除的不一致
         *          2. 发送 userId 跟 skuId.
         *              创建 map, 包含两条数据 userId 与 skuIds
         *
         */
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        // 从送货清单获取全部的 skuId
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        // 为什么要转换成 Json, 因为 rabbitmq 发送消息会把 数据转换成 二进制进行发送. map 中嵌套 List 可能会导致数据过于复杂 在来回转换的过程中可能会导致一些问题产生. 尽量简化处理, 序列化成 JSON 字符串 接收时在反序列化回来
        map.put("skuIds", JSON.toJSONString(skuIds));
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "cart.delete", map);
    }
}
