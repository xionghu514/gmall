package com.atguigu.gmall.search;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void contextLoads() {
        // 索引库的操作对象
        IndexOperations indexOps = restTemplate.indexOps(Goods.class);

        // 判断是有有索引库
        if (!indexOps.exists()) {
            // 创建索引库
            indexOps.create();
            // 声明映射
            indexOps.putMapping(indexOps.createMapping());
        }

        Integer pageNum = 1; // 页码
        Integer pageSize = 100; // 当前页数量

        /**
         * 本质是将每一个 sku 转换成 goods, 先分批查询 100 条 spu, 遍历每个 spu 并查询出找个 spu 下的所有 sku 将其转换成 goods 保存到 es 中
         * 考虑到数据量较大一次性导入不够合理, 应当分批查询 借助 do while, 每次 查询 100 条 spu. 当 pageSize 不足 100 条时结束循环
         *
         * 假设 db 中有 101 条 spu. 我们首次查询 100 条, pageSize 被设置 为 100 会继续下一次循环导入 第 101 条
         * 假设 db 中有 100 条 spu. 我们首次查询 100 条, pageSize 被设置 为 100 会继续下一次循环 没有数据多一次循环也无所谓
         */
        do {
            // 1. 分批查询 spu
            ResponseVo<List<SpuEntity>> spuResponseVo = pmsClient.querySpuByPageJson(
                    new PageParamVo(pageNum, pageSize, null)
            );
            List<SpuEntity> spuEntities = spuResponseVo.getData();

            // 如果 spus 为空的话直接直接返回
            if (CollectionUtils.isEmpty(spuEntities)) {
                return;
            }

            // 遍历当前页码的 spu, 根据 spuId 查询 spu 下的 sku 转换成 Goods 集合 导入索引库中
            spuEntities.forEach(spuEntity -> {
                // 2. 根据 spuId 查询 sku
                ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkuBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();

                // 判断当前 spu 下的 skus 是否为空, 如果不为空则转化成 goods 集合
                if (CollectionUtils.isNotEmpty(skuEntities)) {

                    // 4. 根据 品牌id 查询 品牌
                    ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(spuEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResponseVo.getData();

                    // 5. 根据 分类id 查询 分类
                    ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(225L);
                    CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

                    // 7. 查询 基本类型的检索属性和值
                    ResponseVo<List<SpuAttrValueEntity>> baseAttrResponseVo = pmsClient.querySearchAttrValueByCidAndSpuId(
                            spuEntity.getCategoryId(), spuEntity.getId()
                    );
                    List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrResponseVo.getData();

                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        BeanUtils.copyProperties(skuEntity, goods);

                        // 设置 商品列表所需要的字段 相关字段
                        goods.setTitle(skuEntity.getTitle()); // 设置标题
                        goods.setSubtitle(skuEntity.getSubtitle()); // 设置副标题
                        goods.setPrice(skuEntity.getPrice().doubleValue()); // 设置价格
                        goods.setDefaultImage(skuEntity.getDefaultImage()); // 设置默认图片
                        goods.setSkuId(skuEntity.getId()); // 设置 skuId

                        // 设置 排序及过滤 相关字段
                        goods.setCreateTime(spuEntity.getCreateTime()); // 设置新品

                        // 3. 根据 skuId 查询对应的 sku 库存信息
                        ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkusBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                        // 如果库存不为空, 默认销量为 0, 库存为无货. 如果不为空才需要设置
                        if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                            // 设置 是否有货
                            goods.setStore(
                                    // 任意库存有货默认都有货, anyMatch 集合中 任意仓库商品数量减去已锁定的库存数量 大于 0 默认此 sku 有货
                                    wareSkuEntities.stream().anyMatch( // [1, 2, 3] anyMatch(x -> x > 0) = true
                                            wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                    )
                            );
                            // 设置销量
                            goods.setSales(
                                    // 将库存集合的销量转换成 集合进行求和
                                    wareSkuEntities.stream().map(
                                            WareSkuEntity::getSales
                                    ).reduce((a, b) -> a + b).get()
                            );
                        }

                        // 设置品牌相关参数
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId()); // 品牌 id
                            goods.setBrandName(brandEntity.getName()); // 品牌 名称
                            goods.setLogo(brandEntity.getLogo());
                        }

                        // 设置分类相关参数
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        // 检索类型的规格参数
                        // 6. 查询 销售类型的检索属性和值
                        ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySearchAttrValueByCidAndSkuId(
                                skuEntity.getCategoryId(), skuEntity.getId()
                        );
                        List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();

                        List<SearchAttrValueVo> searchAttrs = new ArrayList<>();

                        // 把销售类型的检索属性和值转换成 vo 对象
                        if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                            searchAttrs.addAll(
                                    skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                                        return searchAttrValueVo;
                                    }).collect(Collectors.toList())
                            );
                        }

                        // 把基本类型的检索属性和值转换成 vo 对象
                        if (CollectionUtils.isNotEmpty(spuAttrValueEntities)) {
                            searchAttrs.addAll(
                                    spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                        return searchAttrValueVo;
                                    }).collect(Collectors.toList())
                            );
                        }

                        goods.setSearchAttrs(searchAttrs); // 设置 检索类型的规格参数

                        return goods;
                    }).collect(Collectors.toList());

                    restTemplate.save(goodsList); // 导入索引库
                }

            });

            pageNum++; // 此页查询完成 查询下一页
            pageSize = spuEntities.size(); // 将当前页 spu 总条数 赋值给 pageSize;
        } while (pageSize == 100);// 当前页没有 100 条记录, 结束循环
    }

    // es 数据导入 提供远程接口, 1. 分批、分页查询 spu
    @Test
    public void test() {
        ResponseVo<List<SpuEntity>> spuResponseVo = pmsClient.querySpuByPageJson(
                new PageParamVo(1, 100, null)
        );

        List<SpuEntity> spuEntities = spuResponseVo.getData();
        spuEntities.forEach(System.out::println);
    }

    // es 数据导入 提供远程接口, 2. 根据 spuId 查询 sku
    @Test
    public void test2() {
        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkuBySpuId(7L);

        List<SkuEntity> skuEntities = skuResponseVo.getData();
        skuEntities.forEach(System.out::println);
    }

    // es 数据导入 提供远程接口, 3. 根据 skuId 查询对应的 sku 库存信息
    @Test
    public void test3() {
        ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsClient.queryWareSkusBySkuId(1L);

        List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
        wareSkuEntities.forEach(System.out::println);
    }

    // es 数据导入 提供远程接口, 4. 根据 品牌id 查询 品牌
    @Test
    public void test4() {
        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(1L);

        BrandEntity brandEntity = brandEntityResponseVo.getData();
        System.out.println("brandEntity = " + brandEntity);
    }

    // es 数据导入 提供远程接口, 5. 根据 分类id 查询 分类
    @Test
    void test5() {
        ResponseVo<CategoryEntity> responseVo = pmsClient.queryCategoryById(225L);

        CategoryEntity categoryEntity = responseVo.getData();
        System.out.println("categoryEntity = " + categoryEntity);
    }

    // es 数据导入 提供远程接口, 6. 查询 销售类型的检索属性和值
    @Test
    void test6() {
        ResponseVo<List<SkuAttrValueEntity>> searchAttrValueResponseVo = pmsClient.querySearchAttrValueByCidAndSkuId(225L, 10L);

        List<SkuAttrValueEntity> searchAttrValueResponseVoData = searchAttrValueResponseVo.getData();
        searchAttrValueResponseVoData.forEach(System.out::println);
    }

    // es 数据导入 提供远程接口, 7. 查询 基本类型的检索属性和值
    @Test
    void test7() {
        ResponseVo<List<SpuAttrValueEntity>> searchAttrValueResponseVo = pmsClient.querySearchAttrValueByCidAndSpuId(225L, 11L);

        List<SpuAttrValueEntity> searchAttrValueResponseVoData = searchAttrValueResponseVo.getData();
        searchAttrValueResponseVoData.forEach(System.out::println);
    }
}
