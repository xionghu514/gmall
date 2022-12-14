package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueByCidAndSkuId(Long cid, Long skuId) {
        // 1. 查询检索类型的规格参数列表
        List<AttrEntity> attrEntities = attrMapper.selectList(
                // select * from pms_attr where category_id = 225 AND search_type = 1;
                new QueryWrapper<AttrEntity>()
                        .eq("category_id", cid)
                        .eq("search_type", 1)
        );

        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }

        // 获取规格参数 id 集合
        List<Long> attrIds = attrEntities.stream().map(
                AttrEntity::getId
        ).collect(Collectors.toList());

        // 2. 查询销售类型的检索属性和值
        return list(
                // select * from pms_sku_attr_value where sku_id = 13 AND attr_id in (4,5,6,8,9);
                new QueryWrapper<SkuAttrValueEntity>()
                        .eq("sku_id", skuId).in("attr_id", attrIds)
        );
    }

}