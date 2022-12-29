package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 10:37:52
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    // 校验库存, 查询
    List<WareSkuEntity> check(@Param("skuId") Long skuId, @Param("count") Integer count);

    // 锁库存, 更新
    int lock(@Param("id") Long id, @Param("count") Integer count);

    // 解锁, 更新
    int unlock(@Param("id") Long id, @Param("count") Integer count);

    // 减库存
    int minus(@Param("id") Long id, @Param("count") Integer count);
}
