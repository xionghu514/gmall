package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.pojo.ItemVo;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:31
 * @Email: moumouguan@gmail.com
 */
@Service
public class ItemService {

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        // 1. 根据 skuId 查询 sku, 设置 sku 相关参数(基本信息)

        // 2. 根据 sku 中的 三级分类 id 查询 一二三级分类, 设置面包屑三级分类

        // 3. 根据 sku 中的 品牌 id 查询品牌, 设置 品牌 相关参数

        // 4. 根据 sku 中的 spuId 查询 spu, 设置 spu 相关参数

        // 5. 根据 skuId 查询 sku 图片列表, 设置 sku 图片列表

        // 6. 根据 skuId 查询 sku 的所有营销信息, 设置 营销类型

        // 7. 根据 skuId 查询 sku 的库存信息, 设置 是否有货

        // 8. 根据 sku 中的 spuId 查询 spu 下的所有销售属性, 设置 销售属性列表

        // 9. 根据 skuId 查询当前 sku 的销售属性, 设置 当前 sku 的销售属性

        // 10. 根据 sku 中的 spuId 查询 spu 下所有 sku, 设置 销售属性组合与 skuId 映射关系

        // 11. 根据 sku 中 spuId 查询 spu 的描述信息

        // 12. 根据分类 id、spuId 及 skuId 查询分组及组下的规格参数值

        return itemVo;
    }

}
