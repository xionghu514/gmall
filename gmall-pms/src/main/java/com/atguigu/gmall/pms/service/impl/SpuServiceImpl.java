package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpusByCidAndPageNumAndPageSize(Long cid, PageParamVo paramVo) {
        LambdaQueryWrapper<SpuEntity> wrapper = new LambdaQueryWrapper<>();

        if (cid != 0) {
            wrapper.eq(SpuEntity::getCategoryId, cid);
        }

        // 获取关键字
        String key = paramVo.getKey();

        // 判空
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.like(SpuEntity::getName, key).or().eq(SpuEntity::getId, key));
        }

        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @Override
    public void bigSave(SpuVo spu) {
// 1. 保存 spu 相关信息
        // 1.1 保存 spu 表
        // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表
        // 1.3 保存 pms_spu_attr_value 基本属性值表

        // 2. 保存 sku 相关信息
        // 2.1 保存 pms_sku
        // 2.2 保存 pms_sku_images 本质与 sku 是同一张表, 如果不为空才需要保存图片
        // 2.3 保存 pms_sku_attr_value 销售属性值表

        // 3. 保存 营销 相关信息
        // 3.1 保存积分优惠表
        // 3.2 保存满减优惠表
        // 3.3 保存打折优惠表
    }

}