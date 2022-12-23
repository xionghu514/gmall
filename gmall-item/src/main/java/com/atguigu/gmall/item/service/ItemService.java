package com.atguigu.gmall.item.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.pojo.ItemVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:31
 * @Email: moumouguan@gmail.com
 */
@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        /**
         * 初始化方法: 都有重载的带线程池的方法
         *      1. 没有返回结果集
         *          static CompletableFuture<Void> runAsync(Runnable runnable)
         *          static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
         *      2. 有返回结果集
         *          static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
         *          static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
         *
         *  使用 supplyAsync 还是 runAsync
         *      runAsync        没有返回结果集
         *      supplyAsync     有返回结果集
         *
         *      因为如下接口都需要依赖 该任务 的查询结果 所以需要定一个有返回结果集的异步任务. 使用 supplyAsync
         *          2. 根据 sku 中的 三级分类 id 查询 一二三级分类
         *          3. 根据 sku 中的 品牌 id 查询品牌
         *          4. 根据 sku 中的 spuId 查询 spu
         *          8. 根据 sku 中的 spuId 查询 spu 下的所有销售属性
         *          10. 根据 sku 中的 spuId 查询 spu 下所有 sku
         *          11. 根据 sku 中 spuId 查询 spu 的描述信息
         *          12. 根据分类 ipuId 及 skuId 查询分组及组下的规格参数值
         */
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            // 1. 根据 skuId 查询 sku, 设置 sku 相关参数(基本信息)
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();

            if (skuEntity == null) {
                throw new RuntimeException("您访问的数据不存在");
            }

            itemVo.setSkuId(skuId); // skuId
            itemVo.setTitle(skuEntity.getTitle()); // 标题
            itemVo.setSubtitle(skuEntity.getSubtitle()); // 副标题
            itemVo.setPrice(skuEntity.getPrice()); // 价格
            itemVo.setWeight(skuEntity.getWeight()); // 重量
            itemVo.setDefaultImage(skuEntity.getDefaultImage()); // 默认图片

            return skuEntity; // 返回数据, 其他任务可以获取该任务的返回结果集并使用
        }, executorService); // 指定线程池, 控制线程数

        /**
         * 线程串行化方法: 带有Async默认是异步执行的。这里所谓的异步指的是不在当前线程内执行。
         *      1. thenApply 方法：当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
         *      即可以获取上一个任务的返回结果集, 也有自己的返回结果集
         *          public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
         *          public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
         *          public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
         *
         *      2. thenAccept方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
         *      获取上一个任务的返回结果集, 但是没有自己的返回结果集
         *          public CompletionStage<Void> thenAccept(Consumer<? super T> action);
         *          public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
         *          public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
         *
         *      3. thenRun方法：只要上面的任务执行完成，就开始执行thenRun，只是处理完任务后，执行 thenRun的后续操作
         *      即不获取上一个任务的返回结果集, 也没有自己的返回结果集
         *          public CompletionStage<Void> thenRun(Runnable action);
         *          public CompletionStage<Void> thenRunAsync(Runnable action);
         *          public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
         *
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync
         *          thenRun、thenRunAsync        无法获取上一个任务返回结果集, 我们需要 使用 skuEntity 所以此处不能使用
         *          thenApply、thenApplyAsync    可以获取上一个任务返回结果集, 同时有自己的返回结果集. 但是没有任务需要依赖此任务的返回结果集, 虽然也可以返回 null 但是也不适用
         *          thenAccept、thenAcceptAsync  可以获取上一个任务返回结果集, 同时自己没有返回结果集. 使用这个合适
         */
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 2. 根据 sku 中的 三级分类 id 查询 一二三级分类, 设置面包屑三级分类
            ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLvl123CategoriesByCid3(skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

            itemVo.setCategories(categoryEntities); // 三级分类
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 3. 根据 sku 中的 品牌 id 查询品牌, 设置 品牌 相关参数
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();

            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId()); // 品牌id
                itemVo.setBrandName(brandEntity.getName()); // 品牌名称
            }
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 4. 根据 sku 中的 spuId 查询 spu, 设置 spu 相关参数
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId()); // spuId
                itemVo.setSpuName(spuEntity.getName()); // spu 名称
            }
        }, executorService);

        /**
         * 该任务不需要依赖任何任务, 同时也没有任何任务需要依赖该任务. 使用 runAsync 开启一个全新的任务即可
         *      使用 runAsync 即可
         *          runAsync        没有返回结果集
         *          supplyAsync     有返回结果集
         */
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 5. 根据 skuId 查询 sku 图片列表, 设置 sku 图片列表
            ResponseVo<List<SkuImagesEntity>> imagesEntityResponseVo = pmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesEntities = imagesEntityResponseVo.getData();

            itemVo.setImage(imagesEntities); // sku 图片列表
        }, executorService);

        /**
         * 该任务不需要依赖任何任务, 同时也没有任何任务需要依赖该任务. 使用 runAsync 开启一个全新的任务即可
         *      使用 runAsync 即可
         *          runAsync        没有返回结果集
         *          supplyAsync     有返回结果集
         */
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            // 6. 根据 skuId 查询 sku 的所有营销信息, 设置 营销类型
            ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> saleVos = saleResponseVo.getData();

            itemVo.setSales(saleVos); // 营销类型
        }, executorService);

        /**
         * 该任务不需要依赖任何任务, 同时也没有任何任务需要依赖该任务. 使用 runAsync 开启一个全新的任务即可
         *      使用 runAsync 即可
         *          runAsync        没有返回结果集
         *          supplyAsync     有返回结果集
         */
        CompletableFuture<Void> wareSkuFuture = CompletableFuture.runAsync(() -> {
            // 7. 根据 skuId 查询 sku 的库存信息, 设置 是否有货
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();

            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(
                        wareSkuEntity ->
                                wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                        )
                ); // 库存
            }
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 8. 根据 sku 中的 spuId 查询 spu 下的所有销售属性, 设置 销售属性列表
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();

            itemVo.setSaleAttrs(saleAttrValueVos); // 销售属性列表
        }, executorService);

        /**
         * 该任务不需要依赖任何任务, 同时也没有任何任务需要依赖该任务. 使用 runAsync 开启一个全新的任务即可
         *      使用 runAsync 即可
         *          runAsync        没有返回结果集
         *          supplyAsync     有返回结果集
         */
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            // 9. 根据 skuId 查询当前 sku 的销售属性, 设置 当前 sku 的销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();

            if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(
                        skuAttrValueEntities.stream().collect(Collectors.toMap(
                                SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)
                        )
                ); // 当前 sku 的销售属性
            }
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 10. 根据 sku 中的 spuId 查询 spu 下所有 sku, 设置 销售属性组合与 skuId 映射关系
            ResponseVo<String> stringResponseVo = pmsClient.queryMappingBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();

            itemVo.setSkuJsons(json); // 销售属性组合 与 skuId 的映射关系
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 11. 根据 sku 中 spuId 查询 spu 的描述信息
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();

            if (spuDescEntity != null) {
                itemVo.setSpuImages(Arrays.asList(
                        StringUtils.split(spuDescEntity.getDecript(), ","))
                ); // spu 的描述信息
            }
        }, executorService);

        /**
         * 该接口需要依赖 1. 根据 skuId 查询 sku 任务的结果所以需要串给第一个任务. 将此处串给 skuFuture
         *      需要获取上一个任务的返回结果集, 并且没有其他任务需要依赖该任务, 使用 thenAcceptAsync 同上
         */
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            // 12. 根据分类 id、spuId 及 skuId 查询分组及组下的规格参数值
            ResponseVo<List<ItemGroupVo>> groupResponseVo = pmsClient.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
                    skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
            itemVo.setGroups(groupResponseVo.getData()); // 规格参数分组
        }, executorService);

        /**
         * 组合任务
         *      1. 所有任务都执行完才放行
         *          public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs);
         *      2. 任和一个任务执行完就放行
         *          public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs);
         *
         *      返回前需要阻塞下, 等待所以异步任务执行完成. 才能返回, 不能发生 已经返回 异步任务 还未执行完成的现象发生.
         *      当 需要依赖 skuFuture 的子任务执行完 skuFuture 肯定也执行完了 所以 skuFuture 不用放置其中
         */
        CompletableFuture.allOf(
                categoryFuture, brandFuture, spuFuture,
                imagesFuture, salesFuture, wareSkuFuture,
                saleAttrsFuture, saleAttrFuture, mappingFuture,
                descFuture, groupFuture
        ).join();

        // 生成静态页面
//        generateHtml(itemVo); // 使用这种方式如果发生异常可能会导致我们这个功能不可用, 应该使用 异步的方式生成
        executorService.execute( // 异步生成静态页面
                () -> generateHtml(itemVo)
        );

        return itemVo;
    }

    private void generateHtml(ItemVo itemVo) {
        try (PrintWriter printWriter = new PrintWriter(new File("/Users/admin/Documents/html/", itemVo.getSkuId() + ".html"))) {
            // 上下文对象的初始化
            Context context = new Context();
            // 页面静态化所需要的数据模型
            context.setVariable("itemVo", itemVo);
            /**
             * 页面静态化方法: 1. 模版名称 2. 上下文对象 3. 文件流
             */
            templateEngine.process("item", context, printWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
