package com.atguigu.gmall.item.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.pojo.ItemVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:31
 * @Email: moumouguan@gmail.com
 */
@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        // 1. 根据 skuId 查询 sku, 设置 sku 相关参数(基本信息)
        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
        SkuEntity skuEntity = skuEntityResponseVo.getData();

        if (skuEntity == null) {
            throw new RuntimeException("您访问的数据不存在");
        }

        itemVo.setSkuId(skuId); // skuId
        itemVo.setTitle(skuEntity.getTitle()); // 标题
        itemVo.setSubtitle(skuEntity.getSubtitle()); // 副标题
        itemVo.setPrice(skuEntity.getPrice()); // 价格
        itemVo.setWeight(skuEntity.getWeight()); // 重量
        itemVo.setDefaultImage(skuEntity.getDefaultImage()); // 默认图片

        // 2. 根据 sku 中的 三级分类 id 查询 一二三级分类, 设置面包屑三级分类
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLvl123CategoriesByCid3(skuEntity.getCategoryId());
        List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

        itemVo.setCategories(categoryEntities); // 三级分类

        // 3. 根据 sku 中的 品牌 id 查询品牌, 设置 品牌 相关参数
        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResponseVo.getData();

        if (brandEntity != null) {
            itemVo.setBrandId(brandEntity.getId()); // 品牌id
            itemVo.setBrandName(brandEntity.getName()); // 品牌名称
        }

        // 4. 根据 sku 中的 spuId 查询 spu, 设置 spu 相关参数
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity != null) {
            itemVo.setSpuId(spuEntity.getId()); // spuId
            itemVo.setSpuName(spuEntity.getName()); // spu 名称
        }

        // 5. 根据 skuId 查询 sku 图片列表, 设置 sku 图片列表
        ResponseVo<List<SkuImagesEntity>> imagesEntityResponseVo = pmsClient.querySkuImagesBySkuId(skuId);
        List<SkuImagesEntity> imagesEntities = imagesEntityResponseVo.getData();

        itemVo.setImage(imagesEntities); // sku 图片列表

        // 6. 根据 skuId 查询 sku 的所有营销信息, 设置 营销类型
        ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySalesBySkuId(skuId);
        List<ItemSaleVo> saleVos = saleResponseVo.getData();

        itemVo.setSales(saleVos); // 营销类型

        // 7. 根据 skuId 查询 sku 的库存信息, 设置 是否有货
        ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkusBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();

        if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
            itemVo.setStore(wareSkuEntities.stream().anyMatch(
                    wareSkuEntity ->
                            wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                    )
            ); // 库存
        }

        // 8. 根据 sku 中的 spuId 查询 spu 下的所有销售属性, 设置 销售属性列表
        ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId());
        List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();

        itemVo.setSaleAttrs(saleAttrValueVos); // 销售属性列表

        // 9. 根据 skuId 查询当前 sku 的销售属性, 设置 当前 sku 的销售属性
        ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
        List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();

        if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
            itemVo.setSaleAttr(
                    skuAttrValueEntities.stream().collect(Collectors.toMap(
                            SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)
                    )
            ); // 当前 sku 的销售属性
        }

        // 10. 根据 sku 中的 spuId 查询 spu 下所有 sku, 设置 销售属性组合与 skuId 映射关系
        ResponseVo<String> stringResponseVo = pmsClient.queryMappingBySpuId(skuEntity.getSpuId());
        String json = stringResponseVo.getData();

        itemVo.setSkuJsons(json); // 销售属性组合 与 skuId 的映射关系

        // 11. 根据 sku 中 spuId 查询 spu 的描述信息
        ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
        SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();

        if (spuDescEntity != null) {
            itemVo.setSpuImages(Arrays.asList(
                    StringUtils.split(spuDescEntity.getDecript(), ","))
            ); // spu 的描述信息
        }

        // 12. 根据分类 id、spuId 及 skuId 查询分组及组下的规格参数值
        ResponseVo<List<ItemGroupVo>> groupResponseVo = pmsClient.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
                skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
        itemVo.setGroups(groupResponseVo.getData()); // 规格参数分组

        return itemVo;
    }

}
