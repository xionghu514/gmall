package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public List<AttrGroupEntity> queryAttrGroupsAndAttrsByCid(Long cid) {
        // 根据分类id 查询 分组信息
        List<AttrGroupEntity> attrGroupEntityList = list(
                new LambdaQueryWrapper<AttrGroupEntity>().eq(AttrGroupEntity::getCategoryId, cid)
        );

        // 查询 根据分组id 查询 属性分组下的 分组属性
        attrGroupEntityList.forEach(attrGroupEntity -> {
            List<AttrEntity> attrEntities = attrMapper.selectList(
                    new LambdaQueryWrapper<AttrEntity>()
                            .eq(AttrEntity::getGroupId, attrGroupEntity.getId())
                            .eq(AttrEntity::getType, 1)
            );
            attrGroupEntity.setAttrEntities(attrEntities);
        });

        return attrGroupEntityList;
    }

}