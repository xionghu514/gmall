package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryGroupsWithAttrsByCid(Long cid) {
        // 1. 根据 cid 查询分组
        List<AttrGroupEntity> groupEntities = list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid)
        );

        /**
         * 如果 groupEntities 为 null 直接返回
         *      1. 没有数据快速返回
         *      2. 防止 null. 空指针异常
         */
        if (CollectionUtils.isEmpty(groupEntities)) {
            return groupEntities;
        }

        // 2. 遍历分组查询组下的规格参数
        groupEntities.forEach(attrGroupEntity ->
                attrGroupEntity.setAttrEntities(
                        attrMapper.selectList(
                                new QueryWrapper<AttrEntity>()
                                        .eq("group_id", attrGroupEntity.getId())
                                        // spu 只需要查询 基本属性, 不需要查询销售属性
                                        .eq("type", 1)
                        )
                )
        );

        return groupEntities;
    }

}