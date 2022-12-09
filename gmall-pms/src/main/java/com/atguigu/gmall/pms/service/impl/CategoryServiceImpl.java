package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 存在两种请求
     *      1. 查询全部分类
     *          http://api.gmall.com/pms/category/parent/-1
     *              select * from pms_category;
     *      2. 查询某一个分类的子分类
     *          http://api.gmall.com/pms/category/parent/34
     *              select * from pms_category where parent_id = pid;
     * @param pid
     * @return
     */
    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long pid) {
        // 构造查询条件
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();

        // 根据参数判断是否需要拼接查询条件
        if (pid != -1) {
            wrapper.eq("parent_id", pid);
        }

        /**
         * 如果 pid 为 -1 则查询全部相当于此处 list(null), wrapper 没有拼接查询条件;
         *      select * from pms_category;
         * 如果 pid 不为 -1 则查询某一个分类下的子分类, 相当于 list(new QueryWrapper<CategoryEntity>().eq("parent_id", pid))
         *      select * from pms_category where parent_id = pid;
         */
        return list(wrapper);
    }
}