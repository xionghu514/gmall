package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2023-02-09 13:40:29
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

