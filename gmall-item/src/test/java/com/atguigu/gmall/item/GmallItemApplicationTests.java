package com.atguigu.gmall.item;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class GmallItemApplicationTests {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    // 1. 根据 skuId 查询 sku
    @Test
    void contextLoads() {
        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(13L);
        SkuEntity skuEntity = skuEntityResponseVo.getData();

        System.out.println("skuEntity = " + skuEntity);
    }

    // 2. 根据 sku 中的 三级分类 id 查询 一二三级分类
    @Test
    public void test() {
        ResponseVo<List<CategoryEntity>> categories = pmsClient.queryLvl123CategoriesByCid3(225L);
        List<CategoryEntity> categoryEntities = categories.getData();

        System.out.println("categoryEntities = " + categoryEntities);
    }

    // 3. 根据 sku 中的 品牌 id 查询品牌
    @Test
    public void test2() {
        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(1L);
        BrandEntity brandEntity = brandEntityResponseVo.getData();

        System.out.println("brandEntity = " + brandEntity);
    }

    // 4. 根据 sku 中的 spuId 查询 spu
    @Test
    public void test3() {
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(13L);
        SpuEntity spuEntity = spuEntityResponseVo.getData();

        System.out.println("spuEntity = " + spuEntity);
    }

    // 5. 根据 skuId 查询 sku 图片列表
    @Test
    public void test5() {
        ResponseVo<List<SkuImagesEntity>> listResponseVo = pmsClient.querySkuImagesBySkuId(13L);
        List<SkuImagesEntity> imagesEntities = listResponseVo.getData();

        System.out.println("imagesEntities = " + imagesEntities);
    }

    // 6. 根据 skuId 查询 sku 的所有营销信息
    @Test
    public void test6() {
        ResponseVo<List<ItemSaleVo>> listResponseVo = smsClient.querySalesBySkuId(13L);
        List<ItemSaleVo> itemSaleVos = listResponseVo.getData();

        System.out.println("itemSaleVos = " + itemSaleVos);
    }

    // 7. 根据 skuId 查询 sku 的库存信息
    @Test
    public void test7() {
        ResponseVo<List<WareSkuEntity>> listResponseVo = wmsClient.queryWareSkusBySkuId(1L);
        List<WareSkuEntity> skuEntities = listResponseVo.getData();

        System.out.println("skuEntities = " + skuEntities);
    }

    // 8. 根据 sku 中的 spuId 查询 spu 下的所有销售属性
    @Test
    public void test8() {
        ResponseVo<List<SaleAttrValueVo>> listResponseVo = pmsClient.querySaleAttrValuesBySpuId(12L);
        List<SaleAttrValueVo> saleAttrValueVos = listResponseVo.getData();

        // [
        //      SaleAttrValueVo(attrId=3, attrName=机身颜色, attrValue=[黑色, 白色]),
        //      SaleAttrValueVo(attrId=4, attrName=运行内存, attrValue=[6G, 12G, 8G]),
        //      SaleAttrValueVo(attrId=5, attrName=机身存储, attrValue=[256G, 128G, 512G])
        // ]
        System.out.println("saleAttrValueVos = " + saleAttrValueVos);
    }

    // 9. 根据 skuId 查询当前 sku 的销售属性
    @Test
    public void test9() {
        ResponseVo<List<SkuAttrValueEntity>> listResponseVo = pmsClient.querySaleAttrValuesBySkuId(12L);
        List<SkuAttrValueEntity> attrValueEntities = listResponseVo.getData();

        // [
        //      SkuAttrValueEntity(id=37, skuId=13, attrId=3, attrName=机身颜色, attrValue=黑色, sort=0),
        //      SkuAttrValueEntity(id=38, skuId=13, attrId=4, attrName=运行内存, attrValue=6G, sort=0),
        //      SkuAttrValueEntity(id=39, skuId=13, attrId=5, attrName=机身存储, attrValue=128G, sort=0)
        // ]
        System.out.println("attrValueEntities = " + attrValueEntities);
    }

    // 10. 根据 spuId 查询 spu下所有销售属性组合与 skuId 的映射关系
    @Test
    public void test10() {
        ResponseVo<String> stringResponseVo = pmsClient.queryMappingBySpuId(12L);
        String data = stringResponseVo.getData();

        // data = {
        //          "白色,8G,512G":25,"白色,8G,128G":23,"白色,12G,256G":27,
        //          "黑色,12G,256G":18,"黑色,6G,256G":12,"黑色,6G,128G":11,
        //          "白色,8G,256G":24,"黑色,6G,512G":13,"黑色,12G,128G":17,
        //          "白色,6G,256G":21,"黑色,12G,512G":19,"白色,6G,128G":20,
        //          "白色,12G,128G":26,"黑色,8G,128G":14,"白色,12G,512G":28,
        //          "白色,6G,512G":22,"黑色,8G,256G":15,"黑色,8G,512G":16
        //         }
        System.out.println("data = " + data);
    }

    // 11. 根据 sku 中 spuId 查询 spu 的描述信息
    @Test
    public void test11() {
        ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(13L);
        SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();

        if (spuDescEntity != null) {
            String decript = spuDescEntity.getDecript();

            List<String> list = Arrays.asList(decript.split(",")); // 字符串转集合
            System.out.println("list = " + list);
        }
    }

    // 12. 查询规格参数分组及组下的规格参数和值
    @Test
    public void test12() {
        ResponseVo<List<ItemGroupVo>> listResponseVo = pmsClient.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(225L, 12L, 11L);
        List<ItemGroupVo> responseVoData = listResponseVo.getData();

        System.out.println("responseVoData = " + responseVoData);
    }
}
