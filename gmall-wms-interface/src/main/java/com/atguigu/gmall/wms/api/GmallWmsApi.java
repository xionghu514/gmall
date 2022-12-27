package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/14 10:00
 * @Email: moumouguan@gmail.com
 */
public interface GmallWmsApi {

    // es 数据导入 提供远程接口, 3. 根据 skuId 查询对应的 sku 库存信息
    // 商品详情页 7. 根据 skuId 查询 sku 的库存信息
    // order 5. 根据skuId查询营销信息
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);

    // 提交订单 验库存 锁库存
    @PostMapping("wms/waresku/check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken") String orderToken);
}
