package com.atguigu.gmall.order;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallOrderApplicationTests {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void contextLoads() {
        // 1. 根据当前用户的id 查询收货地址列表
        // 2. 根据当前用户的id 查询已选中的购物车记录: skuId count(最后一次跟用户确认 其他字段应该从数据库实时获取)
        // 3. 根据skuId查询sku
        // 4. 根据skuId查询销售属性
        // 5. 根据skuId查询营销信息
        // 6. 根据skuId查询库存信息
        // 7. 根据当前用户的id查询用户信息
    }

    // order 1. 根据当前用户的id 查询收货地址列表
    @Test
    public void test() {
        ResponseVo<List<UserAddressEntity>> addressesResponseVo = umsClient.queryAddressesByUserId(3L);
        List<UserAddressEntity> addressEntities = addressesResponseVo.getData();

        System.out.println("addressEntities = " + addressEntities);
    }

    // order 2. 根据当前用户的id 查询已选中的购物车记录
    @Test
    public void test2() {
        ResponseVo<List<Cart>> cartsResponseVo = cartClient.queryCheckedCartsByUserId(4L);
        List<Cart> carts = cartsResponseVo.getData();

        System.out.println("carts = " + carts);
    }

    // order 3. 根据skuId查询sku
    @Test
    public void test3() {
        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(12L);
        SkuEntity skuEntity = skuEntityResponseVo.getData();

        System.out.println("skuEntity = " + skuEntity);
    }

    // order 4. 根据skuId查询销售属性
    @Test
    public void test4() {
        ResponseVo<List<SkuAttrValueEntity>> salesResponseVo = pmsClient.querySaleAttrValuesBySkuId(12L);
        List<SkuAttrValueEntity> skuAttrValueEntities = salesResponseVo.getData();

        System.out.println("skuAttrValueEntities = " + skuAttrValueEntities);
    }

    // order 5. 根据skuId查询营销信息
    @Test
    public void test5() {
        ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(12L);
        List<ItemSaleVo> saleVos = salesResponseVo.getData();

        System.out.println("saleVos = " + saleVos);
    }

    // order 6. 根据skuId查询库存信息
    @Test
    public void test6() {
        ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkusBySkuId(1L);
        List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();

        System.out.println("wareSkuEntities = " + wareSkuEntities);
    }

    // order 7. 根据当前用户的id查询用户信息
    @Test
    public void test7() {
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(4L);
        UserEntity userEntity = userEntityResponseVo.getData();

        System.out.println("userEntity = " + userEntity);
    }

    // 分析提交订单所需要接口
    @Test
    public void test8() {
        // 1. 防重提交(保证幂等) 不需要远程接口, 直接查询 redis 即可
        // 2. 验价格: 验总价(遍历送货清单, 根据每一个 skuId 查询 sku 实时价格 算出实时总价 与 页面比较) 根据 skuId 查询 sku
        // 3. 验库存并锁库存(wms 完成远程接口)
        // 4. 创建订单(创建订单)
        // 5. 删除购物车中对应的记录(可以通过异步的方式进行删除, 1. 购物车删除失败也不影响订单创建, 2. 删除购物车时效性不高 提高一定时间. MQ 异步)
    }
}
