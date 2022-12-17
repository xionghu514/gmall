package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> queryCategoriesByPid(Long pid);
}
