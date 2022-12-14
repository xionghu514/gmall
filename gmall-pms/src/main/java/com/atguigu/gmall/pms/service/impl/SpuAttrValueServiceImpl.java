package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchAttrValueByCidAndSpuId(Long cid, Long spuId) {
        // 1. 查询检索类型的规格参数列表
        List<AttrEntity> attrEntities = attrMapper.selectList(
                // select * from pms_attr where category_id = 225 AND search_type = 1;
                new QueryWrapper<AttrEntity>()
                        .eq("category_id", cid).eq("search_type", 1)
        );

        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }

        // 获取规格参数 id 集合
        List<Long> attrIds = attrEntities.stream()
                .map(AttrEntity::getId)
                .collect(Collectors.toList());

        // 2. 查询基本类型的检索属性和值
        return list(
                // select * from pms_spu_attr_value where spu_id = 13 AND attr_id in (4,5,6,8,9);
                new QueryWrapper<SpuAttrValueEntity>()
                        .eq("spu_id", spuId)
                        .in("attr_id", attrIds)
        );
    }

}