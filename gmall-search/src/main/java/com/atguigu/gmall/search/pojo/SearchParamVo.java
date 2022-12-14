package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Description: 封装 搜索工程 搜索条件参数对象
 * 　　http://search.gmall.com/search?keyword=手机&brandId=1,2,3&categoryId=225&props=4:8G-12G&props=5:256G-512G
 * 　　　　&sort=1&priceFrom=1000&priceTo=5000&pageNum=1&store=true
 * @Author: Guan FuQing
 * @Date: 2022/12/14 14:01
 * @Email: moumouguan@gmail.com
 */
@Data
public class SearchParamVo {
    // 搜索关键字
    private String keyword;

    // 品牌 id 过滤条件
    private List<Long> brandId; // 可以通过数组或者集合直接接收, 不用做字符串处理

    // 分类 id 过滤条件
    private List<Long> categoryId; // 同品牌 id

    // 规格参数的过滤条件: ["4:8-12G", "5:256-512G"]
    private List<String> props;

    // 排序条件: 0-综合排序 1-价格降序 2-价格升序 3-销量降序 4-新品降序
    private Integer sort = 0;

    // 价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    // 分页
    private Integer pageNum = 1;
    private final Integer pageSize = 20; // 每页仅显示 20 条

    // 仅显示有货
    private Boolean store = false; // 默认为无货
}
