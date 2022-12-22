package com.atguigu.gmall.item;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
