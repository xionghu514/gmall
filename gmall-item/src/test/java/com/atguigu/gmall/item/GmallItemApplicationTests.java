package com.atguigu.gmall.item;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallItemApplicationTests {

    @Autowired
    private GmallPmsClient pmsClient;

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
}
