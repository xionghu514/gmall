package com.atguigu.gmall.search.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/14 14:06
 * @Email: moumouguan@gmail.com
 */
@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void search(SearchParamVo paramVo) {

        try {
            SearchRequest request = new SearchRequest();
            // 指定搜索索引库
            request.indices("goods");
            // 搜索条件
            request.source(builderDsl(paramVo));
            // 获取响应对象
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            // TODO: 结果集解析

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 构建搜索条件
    private SearchSourceBuilder builderDsl(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 获取搜索关键字
        String keyword = paramVo.getKeyword();

        // 搜索关键字如果为空直接返回
        if (StringUtils.isBlank(keyword)) {
            throw new RuntimeException("请输入搜索条件");
        }

        // 1. 构建搜索过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        // 1.1 构建匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        // 1.2 构建过滤
        // 1.2.1 构建品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if (CollectionUtils.isNotEmpty(brandId)) {
            // 多词条过滤
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        // 1.2.2 构建分类过滤
        List<Long> categoryId = paramVo.getCategoryId();
        if (CollectionUtils.isNotEmpty(categoryId)) {
            // 多词条过滤
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }

        // 1.2.3 构建价格范围过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        // 价格区间过滤
        if (priceFrom != null || priceTo != null) {
            // 范围过滤
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);

            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }

            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
        }

        // 1.2.4 构建是否有货过滤
        Boolean store = paramVo.getStore();
        // 因为数据少为了方便演示所以可以查询有货无货, 正常情况下应该只能查询有货
        if (store != null) {
            // 单词条过滤
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }

        // 1.2.5 构建规格参数嵌套过滤 ["4:8G-12G", "5:256G-512G"]
        List<String> props = paramVo.getProps();
        if (CollectionUtils.isNotEmpty(props)) {
            props.forEach(prop -> { // 4:8G-12G
                // 先以冒号分割出规格参数id及规格参数值（8G-12G）
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2 && NumberUtils.isCreatable(attrs[0])){
                    // 嵌套查询中需要布尔查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    // 给布尔查询添加两个词条查询
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attrs[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", StringUtils.split(attrs[1], "-")));
                    // 每一个prop就是一个嵌套过滤
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None)); // ScoreMode.None 不影响得分
                }
            });
        }

        // 2. 构建排序条件
        Integer sort = paramVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1: sourceBuilder.sort("price", SortOrder.DESC); break;
                case 2: sourceBuilder.sort("price", SortOrder.ASC); break;
                case 3: sourceBuilder.sort("sales", SortOrder.DESC); break;
                case 4: sourceBuilder.sort("createTime", SortOrder.DESC); break;
                default:
                    sourceBuilder.sort("_score", SortOrder.DESC); break;
            }
        }

        // 3.构建分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 4.构建高亮
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red;'>")
                .postTags("</font>"));

        // 5.构建聚合
        // 5.1. 品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));

        // 5.2. 分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        // 5.3. 规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));

        // 6.结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "subtitle", "price", "defaultImage"}, null);

        // 打印的是 DSL 语句
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
