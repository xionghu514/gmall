package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper descMapper; // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表

    @Autowired
    private SpuAttrValueService baseAttrService; // 1.3 保存 pms_spu_attr_value 基本属性值表

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
        spu.setCreateTime(new Date());
        // 再创建时间会导致不一致, 直接获取上一个设置的时间即可
        spu.setUpdateTime(spu.getCreateTime());
        save(spu);

        // 保存完 spu 后主键回显 抽取 spuId 给以下保存方法使用
        Long spuId = spu.getId();

        // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表(不需要批量新增使用 mapper 即可)
        List<String> spuImages = spu.getSpuImages();
        // spuImages 不为空才进行保存 spu 信息介绍表
        if (CollectionUtils.isNotEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            // 本质与 spu 是同一张表, 没有自己的 Id 需要 设置 spuId
            spuDescEntity.setSpuId(spuId); // 设置图片 id
            // 将集合以 "," 拼接符 拼接到一起形成新的 字符串
            spuDescEntity.setDecript(org.apache.commons.lang3.StringUtils.join(spuImages, ",")); // ["1", "2"] -> "1,2"
            descMapper.insert(spuDescEntity);
        }

        // 1.3 保存 pms_spu_attr_value 基本属性值表(需要使用批量保存使用 service)
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        // baseAttrs 不为空才需要保存 spu 基本信息
        if (CollectionUtils.isNotEmpty(baseAttrs)) {
            baseAttrService.saveBatch(
                    // 将 List<SpuAttrValueVo> 转换为 List<spuAttrValueEntity>
                    baseAttrs.stream().map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        // 将 spuAttrValueVo 中的值赋值给 spuAttrValueEntity, 需要在设值前 拷贝, 否则会出现 数据丢失
                        BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity); // 源 -> 对象(从 源中 拷贝到 对象)
                        // 设置 spuId
                        spuAttrValueEntity.setSpuId(spuId);
                        // 设置排序字段
                        spuAttrValueEntity.setSort(
                                // 如果 spuAttrValueVo.getSort() 值为 null 则设置为 0
                                Optional.ofNullable(spuAttrValueVo.getSort()).orElse(0)
                        );
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList())
            );
        }


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