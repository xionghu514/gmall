package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/13 20:16
 * @Email: moumouguan@gmail.com
 */
@Data
@Document(indexName = "goods", shards = 3, replicas = 2) // indexName 指定索引库, shards 分片数量, replicas 副本数量
public class Goods {

    // sku 相关字段
    @Id
    private Long skuId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title; // 标题
    @Field(type = FieldType.Keyword, index = false)
    private String subtitle; // 副标题
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage; // 默认图片
    @Field(type = FieldType.Double)
    private Double price; // 价格

    // 排序及过滤
    @Field(type = FieldType.Long)
    private Long sales; // 销量
    @Field(type = FieldType.Date, format = DateFormat.date)
    private Date createTime; // 新品
    @Field(type = FieldType.Boolean)
    private Boolean store; // 是否有货

    // 过滤条件
    @Field(type = FieldType.Long)
    private Long brandId; // 品牌 id
    @Field(type = FieldType.Keyword)
    private String brandName; // 品牌名称
    @Field(type = FieldType.Keyword)
    private String logo; // 品牌 logo

    // 分类
    @Field(type = FieldType.Long)
    private Long categoryId; // 分类 id
    @Field(type = FieldType.Keyword)
    private String categoryName; // 分类名称

    // 规格参数
    @Field(type = FieldType.Nested) // 嵌套类型
    private List<SearchAttrValueVo> searchAttrs;
}
