package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueMapper attrValueMapper;

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

    @Override
    public List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId) {
        // 1. 根据 spuId 查询 spu 下所有的 sku
        List<SkuEntity> skuEntities = skuMapper.selectList(
                new QueryWrapper<SkuEntity>().eq("spu_id", spuId)
        );

        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }

        // 获取 skuId 集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 2. 根kuId 集合 查询销售属性列表
        List<SkuAttrValueEntity> attrValueEntities = list(
                new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds)
        );

        if (CollectionUtils.isEmpty(attrValueEntities)) {
            return null;
        }

        // 3. 把销售属性列表转化成 List<SaleAttrValueVo>
        // [{attrId: 3, attrName: 机身颜色, attrValues: ['白天白', '暗夜黑']}]
        // [{attrId: 4, attrName: 运行内存, attrValues: ['8G', '12G']}]
        // [{attrId: 5, attrName: 机身存储, attrValues: ['256G', '512G']}]
        // 分组成一个 Map<Long, List<SkuAttrValueEntity>>
        Map<Long, List<SkuAttrValueEntity>> map = attrValueEntities.stream().collect(
                Collectors.groupingBy(SkuAttrValueEntity::getAttrId)
        );

        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        // 把每一个 kv 转换成 SaleAttrValueVo
        map.forEach((key, value) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            // 当前遍历的 key 就是 attrId
            saleAttrValueVo.setAttrId(key);
            // 有该数据分组的情况下, 该分组至少有一条数据
            saleAttrValueVo.setAttrName(value.get(0).getAttrName());
            saleAttrValueVo.setAttrValue(
                    value.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet())
            );
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String queryMappingBySpuId(Long spuId) {
        // 1. 根据 spuId 查询 spu 下所有的 sku
        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));

        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }

        // 获取 skuId 集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 获取到了规格参数值组合与 skuId 的关系
        List<Map<String, Object>> maps = attrValueMapper.queryMappingBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(maps)) {
            return null;
        }

        Map<String, Object> mappingMap = maps.stream().collect(
                Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long)map.get("sku_id"))
        );

        return JSON.toJSONString(mappingMap);
    }
}