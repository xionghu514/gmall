package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategoriesByPid(Long pid);

    List<CategoryEntity> queryLevel23CategoriesByPid(Long pid);
}

