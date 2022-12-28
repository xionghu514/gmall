package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallSmsClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemMapper itemMapper;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveOrder(OrderSubmitVo submitVo, Long userId) {

        // 1.保存订单表
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(submitVo.getOrderToken());
        orderEntity.setUserId(userId);
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(submitVo.getTotalPrice());
        orderEntity.setPayAmount(submitVo.getTotalPrice());
        orderEntity.setPayType(submitVo.getPayType());
        orderEntity.setSourceType(1);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(submitVo.getDeliveryCompany());
        UserAddressEntity address = submitVo.getAddress();
        if (address != null) {
            orderEntity.setReceiverRegion(address.getRegion());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverAddress(address.getAddress());
        }
        orderEntity.setConfirmStatus(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setUseIntegration(submitVo.getBounds());
        save(orderEntity);
        Long orderId = orderEntity.getId();

        // 2.保存订单详情表
        List<OrderItemVo> items = submitVo.getItems();
        items.forEach(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderId(orderId);
            orderItemEntity.setOrderSn(submitVo.getOrderToken());

            // 设置sku信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();

            orderItemEntity.setSkuId(skuEntity.getId());
            orderItemEntity.setSkuName(skuEntity.getName());
            orderItemEntity.setSkuPrice(skuEntity.getPrice());
            orderItemEntity.setSkuPic(skuEntity.getDefaultImage());
            orderItemEntity.setSkuQuantity(item.getCount().intValue());
            orderItemEntity.setCategoryId(skuEntity.getCategoryId());

            // 销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuEntity.getId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));

            // 查询品牌
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            orderItemEntity.setSpuBrand(brandEntity.getName());

            // 查询 spu
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            orderItemEntity.setSpuName(spuEntity.getName());
            orderItemEntity.setSpuId(spuEntity.getId());

            // 描述信息
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            orderItemEntity.setSpuPic(spuDescEntity.getDecript());

            orderItemEntity.setRealAmount(skuEntity.getPrice());

            itemMapper.insert(orderItemEntity);
        });

        /**
         * 模拟订单创建失败, 查看锁定库存是否已经解锁回去
         *      断点打在 wms StockListener 更新订单状态为无效订单 代码上, 创建订单 放行 查看 库存表是不已经锁定库存成功
         *      断点搭载 oms StockListener 根据 orderToken 获取锁定信息的缓存 代码上, 放行 查看库存表锁定库存数量是否已经还原
         *
         *          当创建订单表(提交订单)失败前 锁库存已经成功, 当创建订单失败 解锁也成功
         */
//        int i = 1 / 0;

        /**
         * 模拟订单创建成功, feign 超时. 查看锁定库存是否已经解锁回去
         *      结果同上
         */
//        try {
//            TimeUnit.SECONDS.sleep(4); // feign 超时是三秒, 此处睡四秒保证 feign 会超时
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}