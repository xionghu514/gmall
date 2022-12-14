package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/14 14:08
 * @Email: moumouguan@gmail.com
 */
@Data
public class SearchResponseVo {
    // 品牌过滤列表
    private List<BrandEntity> brands;

    // 分类过滤列表
    private List<CategoryEntity> categories;

    // 规格参数的过滤列表
    private List<SearchResponseAttrVo> filters;

    // 总记录数
    private Long total;

    // 页码
    private Integer pageNum;
    private Integer pageSize;

    // 当前页的数据列表
    private List<Goods> goodsList;
}

