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

    }

}