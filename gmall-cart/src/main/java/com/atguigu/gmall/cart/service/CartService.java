package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/25 22:04
 * @Email: moumouguan@gmail.com
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    /**
     * 前缀
     */
    private static final String KEY_PREFIX = "CART:INFO:";

    public void saveCart(Cart cart) {

        // 1. 获取登陆状态
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 首先获取 userKey, 赋值给 userId. 判断 userId 是否为空. 如果不为空则 把真正的 userId 赋值给 userId 变量. 保证用户信息用不为空
//        String userId = userInfo.getUserKey(); // 不管是否登陆 userKey 使用存在
//        if (userInfo.getUserId() == null) { // 判断 userInfo 中的 userId 是否为空
//            userId = userInfo.getUserId().toString();
//        }

        String userId = getUserId(); // 获取登陆状态

        /**
         * 2. 判断当前购物车是否包含该商品
         *      如果使用 opsForHash(). 不够方便, 每次都需要 外层的 key + 内层的 key 操作
         *          查询所有购物车 通过 .entries() 传入外层的 key 即可: Map<HK, HV> entries(H var1);
         *          获取购物车的某一个数据 通过 .get() 传入外层的 key + 内层的 key 即可: HV get(H var1, Object var2);
         *          判断是否包含某一条记录 通过 .hasKey 传入外层的 key + 内层的 key 判断: Boolean hasKey(H var1, Object var2);
         *              判断该用户是否包含该记录
         *          新增一条购物车记录 通过 .put() 传入 外层的 key + 内层的 key + 内层的 value 即可: void put(H var1, HK var2, HV var3);
         *      使用 boundHashOps() 比 opsForHash() 简单
         *          public <HK, HV> BoundHashOperations<K, HK, HV> boundHashOps(K key) 指定外层的 key 获取内层的 map
         *              .values() 获取内层购物车集合
         *              .hasKey() 通过内层的 key 就可以判断当前用户是否包含这条购物车记录
         *              .put() 通过内层的 key 内层的 value 即可新增购物车记录
         *
         *  数据结构
         *      Map<userId/userKey, Map<skuId, cartJson>
         */
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);// 指定外层的 key 获取内层的 map

        // 本次新增数量 + 该数据 可能会被覆盖掉提前提取出来
        BigDecimal count = cart.getCount();

        String skuId = cart.getSkuId().toString();
        // 判断 该用户购物车是否存在该条商品 相当于 已经拿到 内层 Map<skuId, cartJson>
        if (hashOps.hasKey(skuId)) { // 此处不会空指针 因为 boundHashOps 底层直接 new DefaultBoundHashOperations 对象. 购物车可能为空 但 hashOps 不可能为空
            // 包含更新数量
            String cartJson = hashOps.get(skuId).toString(); // 获取对应的购物车记录

            // 反序列化为购物车对象
            cart = JSON.parseObject(cartJson, Cart.class); // 覆盖掉参数中的购物车对象
            cart.setCount(cart.getCount().add(count)); // 数据库中的数量累加新增的数量

            // 更新到数据库 redis
//            hashOps.put(skuId, JSON.toJSONString(cart));

            // 更新到数据库 mysql. 更新那个用户的哪条商品的购物车
            cartMapper.update(
                    cart, new UpdateWrapper<Cart>()
                            .eq("user_id", userId)
                            .eq("sku_id", skuId)
            );
        } else {
            // 不包含 新增记录, 此时购物车中只有两个参数 1. sku_id 2. count 其他参数需要调用远程接口进行设置 在保存到数据库
            cart.setUserId(userId);
            cart.setCheck(true); // 新增商品到购物车 默认设置为选中状态

            // 1. 根据 skuId 查询 sku
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            // 由于是 get 请求, 用户可能会把连接放入 地址栏中进行访问, 导致我们查询的数据为空
            SkuEntity skuEntity = skuEntityResponseVo.getData();

            if (skuEntity == null) {
                throw new CartException("您要添加购物车的商品不存在!");
            }

            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());

            // 2. 根据 skuId 查询当前 sku 的销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            // 序列化后放入
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 3. 根据 skuId 查询库存
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            // 如果库存不为空
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {

                // 序列化后放入
                cart.setStore(
                        wareSkuEntities.stream()
                                .anyMatch(
                                        wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                )
                );
            }

            // 4. 根据 skuId 查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            // 序列化后放入
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 保存到数据库 redis
//            hashOps.put(skuId, JSON.toJSONString(cart));

            // 保存到数据库 mysql
            cartMapper.insert(cart);
        }
        // 不管是更新还是新增都会执行该方法 提取出来
        hashOps.put(skuId, JSON.toJSONString(cart));
    }


    public Cart queryCartBySkuId(Long skuId) {
        // 1. 获取登陆状态
        String userId = getUserId();

        // 获取内层 map<skuId, cartJson>
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("您的购物车中没有该商品!");
        }

        String cartJson = hashOps.get(skuId.toString()).toString();

        return JSON.parseObject(cartJson, Cart.class);
    }

    // 不管是更新 还是 新增 还是回显 我们都会用到该方法 提取出来一个方法
    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 首先取 userkey, 不管 userId 是否存在 userKey 是存在的
        String userId = userInfo.getUserKey();

        // 判断 userId 是否为空
        if (userInfo.getUserId() != null) {
            // 如果 userId 不为空 使用 userId
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }

    @Async // 标记异步调用方法
//    public String executor1() {
//    public Future<String> executor1() { // Future<真正要返回的数据类型>
//    public ListenableFuture<String> executor1() {
    public void executor1() {
        try {
            System.out.println("executor1方法开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor1方法结束执行。。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //        return "Hello executor2";

//        return AsyncResult.forValue("executor1");
    }

    @Async // 标记异步调用方法
//    public String executor1() {
//    public Future<String> executor2() {
//    public ListenableFuture<String> executor2() {
    public void executor2() {
        try {
            System.out.println("executor2方法开始执行");
            TimeUnit.SECONDS.sleep(4);

            int i = 1 / 0;

            System.out.println("executor2方法结束执行。。。");

//            return AsyncResult.forValue("Hello executor2"); // 正常响应
        } catch (InterruptedException e) {
            e.printStackTrace();

//            return AsyncResult.forExecutionException(e); // 异常响应
        }

        //        return "Hello executor2";

//        return AsyncResult.forValue("executor2");
    }
}
