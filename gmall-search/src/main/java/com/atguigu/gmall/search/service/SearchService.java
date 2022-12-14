package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public SearchResponseVo search(SearchParamVo paramVo) {

        try {
            SearchRequest request = new SearchRequest();
            // 指定搜索索引库
            request.indices("goods");
            // 搜索条件
            request.source(builderDsl(paramVo));
            // 获取响应对象
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            SearchResponseVo responseVo = parseResult(response);
            // 分页参数可以从请求参数中获取
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();

        // 1.解析普通搜索结果集
        SearchHits hits = response.getHits();
        // 总记录数
        responseVo.setTotal(hits.getTotalHits().value);
        // 获取当前页的记录
        SearchHit[] hitsHits = hits.getHits();
        // 把当前页的记录（hitsHits）数组 转换成 GoodsList集合
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
            // 把每条记录中的_source反序列化为goods对象
            String json = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(json, Goods.class);
            // 获取高亮标题
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            goods.setTitle(highlightField.fragments()[0].string());

            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        // 2.解析聚合结果集
        Aggregations aggregations = response.getAggregations();
        // 2.1. 解析品牌id的聚合结果集，获取品牌列表
        ParsedLongTerms brandIdAgg = aggregations.get("brandIdAgg");
        // 获取品牌id聚合中的桶列表
        List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();
        if (!org.springframework.util.CollectionUtils.isEmpty(brandIdAggBuckets)){
            // 把桶列表转化成 品牌列表
            responseVo.setBrands(brandIdAggBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                // 当前桶中的key就是品牌id
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                // 获取子聚合
                Aggregations subAggs = ((Terms.Bucket) bucket).getAggregations();
                // 通过子聚合获取品牌名称
                ParsedStringTerms brandNameAgg = subAggs.get("brandNameAgg");
                List<? extends Terms.Bucket> brandNameAggBuckets = brandNameAgg.getBuckets();
                if (!org.springframework.util.CollectionUtils.isEmpty(brandNameAggBuckets)){
                    // 品牌名称的桶集合应该有且仅有一个
                    brandEntity.setName(brandNameAggBuckets.get(0).getKeyAsString());
                }
                // 通过子聚合获取logo
                ParsedStringTerms logoAgg = subAggs.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!org.springframework.util.CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList()));
        }

        // 3.1. 解析分类id的聚合结果集，获取分类列表
        ParsedLongTerms categoryIdAgg = aggregations.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        // 把分类id的桶集合 转化成 分类集合
        if (!org.springframework.util.CollectionUtils.isEmpty(categoryIdAggBuckets)){
            responseVo.setCategories(categoryIdAggBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                // 获取分类名称的子聚合，解析出分类名称
                ParsedStringTerms categoryNameAgg = ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> categoryNameAggBuckets = categoryNameAgg.getBuckets();
                categoryEntity.setName(categoryNameAggBuckets.get(0).getKeyAsString());
                return categoryEntity;
            }).collect(Collectors.toList()));
        }

        // 3.3. 解析规格参数的嵌套聚合，获取规格参数列表
        ParsedNested attrAgg = aggregations.get("attrAgg");
        // 从嵌套聚合中获取规格参数id的子聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        // 获取规格参数id聚合中桶集合
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!org.springframework.util.CollectionUtils.isEmpty(buckets)){
            // 把桶集合转化成vo集合
            List<SearchResponseAttrVo> filters = buckets.stream().map(bucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                // 当前桶中的key就是规格参数的id
                responseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                // 获取当前桶中的子聚合
                Aggregations subAggs = ((Terms.Bucket) bucket).getAggregations();

                // 获取子聚合中规格参数名的子聚合，获取规格参数名
                ParsedStringTerms attrNameAgg = subAggs.get("attrNameAgg");
                responseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());

                // 获取子聚合中规格参数值的子聚合，获取规格参数值列表
                ParsedStringTerms attrValueAgg = subAggs.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                responseAttrVo.setAttrValues(attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                return responseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(filters);
        }

        return responseVo;
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
