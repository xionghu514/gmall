package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuDescService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    // 在 Spring 中通过 @Autowired 注入的都是代理类对象, 因为通过代理类调用方法 事务注解才会生效
    // 注意在 Spring 中默认是 JDK 代理。 但是 Spring Boot 2.x 以及之后都是 CGLiB 代理 更加通用
    @Autowired
//    private SpuDescMapper descMapper; // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表
    private SpuDescService descService; // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表

    @Autowired
    private SpuAttrValueService baseAttrService; // 1.3 保存 pms_spu_attr_value 基本属性值表

    @Autowired
    private SkuMapper skuMapper; // 2.1 保存 pms_sku

    @Autowired
    private SkuImagesService imagesService; // 2.2 保存 pms_sku_images 本质与 sku 是同一张表, 如果不为空才需要保存图片

    @Autowired
    private SkuAttrValueService saleAttrService;  // 2.3 保存 pms_sku_attr_value 销售属性值表

    @Autowired
    private GmallSmsClient smsClient; // 3. 保存 营销 相关信息

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 1. 查本类
     * 　　http://api.gmall.com/pms/spu/category/225?t=1649653070604&pageNum=1&pageSize=10&key=7
     * 　　　　select * from pms_spu where category_id = cid and (id = key or name like '%key%');
     * 　　http://api.gmall.com/pms/spu/category/225?t=1649653070604&pageNum=1&pageSize=10&key=
     * 　　　　select * from pms_spu where category_id = cid;
     * 2. 查全站
     * 　　http://api.gmall.com/pms/spu/category/0?t=1649653070604&pageNum=1&pageSize=10&key=
     * 　　　　select * from pms_spu;
     * 　　http://api.gmall.com/pms/spu/category/0?t=1649653070604&pageNum=1&pageSize=10&key=7
     * 　　　　select * from pms_spu where (id = key or name like '%key%');
     *
     * @param cid
     * @param paramVo
     * @return
     */
    @Override
    public PageResultVo querySpuByCidAndPage(Long cid, PageParamVo paramVo) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();

        // 当 cid = 0 时, 不拼接查询条件 查询全部分类, 否则 拼接查询条件 查询本类
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }

        // 获取查询条件
        String key = paramVo.getKey();

        // 当查询条件不为空时才需要拼接查询条件
        if (StringUtils.isNotBlank(key)) {
            /**
             * 此处的 .and 是消费型函数式接口 Consumer, t -> t.eq 相当于 stream 操作集合. stream t -> 提供的是集合的某一个元素, 而 wrapper 提供的自身
             *      select * from pms_spu where (id = key or name like '%key%');
             */
            wrapper.and(t -> t.like("name", key).or().eq("id", key));
        }

        /**
         * page 是 IService 提供的分页方法
         *      default <E extends IPage<T>> E page(E page, Wrapper<T> queryWrapper)
         *          他有两个参数, 一个是 page, 一个是 wrapper
         *              <E extends IPage<T>> E page 形参, 限定为 IPage 以及他的子类
         *              wrapper 查询条件
         */
        IPage<SpuEntity> page = this.page(
                /**
                 *      // 返回 IPage 对象
                 *      public <T> IPage<T> getPage(){
                 *
                 *         return new Page<>(pageNum, pageSize);
                 *     }
                 */
                paramVo.getPage(),
                // 查询条件
                wrapper
        );

        return new PageResultVo(page);
    }

    /**
     * 事务的传播行为 @Transactional(propagation = Propagation.REQUIRES) 默认
     *      一个 sevice 的方法 调用 另一个 service 的方法时 事务之间的影响 spring 特有
     *
     *      支持当前事务
     *          REQUIRED    支持当前事务，如果不存在，就新建一个
     *          SUPPORTS    支持当前事务，如果不存在，就不使用事务
     *          MANDATORY   支持当前事务，如果不存在，抛出异常
     *
     *      不支持当前事务(挂起当前事务)
     *          REQUIRES_NEW    如果有事务存在，挂起当前事务，创建一个新的事务
     *          NOT_SUPPORTED   以非事务方式运行，如果有事务存在，挂起当前事务
     *          NEVER           以非事务方式运行，如果有事务存在，抛出异常
     *
     *      嵌套事务
     *          NESTED  如果当前事务存在，则嵌套事务执行（嵌套式事务）
     *              依赖于JDBC3.0提供的SavePoint技术(保存点)
     *              保存点不好控制所以不使用
     *
     *      常用的两种
     *          REQUIRED：一个事务，要么成功，要么失败
     *          REQUIRES_NEW：两个不同事务，彼此之间没有关系。一个事务失败了不影响另一个事务
     *
     * 回滚策略
     *      默认的回滚策略
     *          所有的受检异常都不会回滚(编译器可以检查出来, 编译时异常)
     *          所有的非受检异常都会回滚(编译器不可以检查出来, 运行时异常)
     *      自定义回滚策略
     *          rollbackFor                 什么异常会回滚, 指定异常类型
     *          rollbackForClassName        什么异常不会回滚, 指定异常类的全路径
     *          noRollbackFor               什么异常不回滚, 指定异常类型
     *          noRollbackForClassName      什么异常不会回滚, 指定异常类的全路径
     *
     * 只读事物
     *      对于一些事务要求严格的项目 不仅仅写需要添加事务 读也需要添加事物
     *          如果一个方法标记为 readOnly=true 事务, 则代表该方法只能查询，不能增删改。readOnly 默认为 false
     *
     * @param spu
     */
//    @Transactional(propagation = Propagation.REQUIRED) // 事务注解 默认的 传播行为
//    @Transactional(rollbackFor = Exception.class) // 所有异常都回滚
//    @Transactional(noRollbackFor = ArithmeticException.class, rollbackFor = FileNotFoundException.class) // 自定义回滚策略 1 / 0 不会回滚, 文件找不到回滚
    @Transactional(readOnly = true) // 只读事务, 该方法只能进行查询 不能做 增删改 操作
    @Override
    public void bigSave(SpuVo spu) throws FileNotFoundException {
        // 1. 保存 spu 相关信息
        // 1.1 保存 spu 表
        Long spuId = saveSpuInfo(spu);

        // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表(不需要批量新增使用 mapper 即可)
        // saveSpuDesc(spu, spuId); // 编译时 默认会添加 this 关键字. 实现类自己调用自己的方法没有 通过代理 也就没有增强. 方法在自己类时 通过this 调用没有通过代理类调用, 事务注解没有生效, 传播行为更不可能生效
        descService.saveSpuDesc(spu, spuId); // 通过代理类调用才会有增强, 进而事物注解才能生效 传播行为才生效

        int i = 1 / 0;
        // spuInfo 与 spuDesc 保存成功, 后面方法不执行
//        new FileInputStream("xxx"); // 受检异常 默认不会回滚. 抛出该异常 而不是 try catch. try catch, aop 无法监测该异常 事务无法回滚

        // 1.3 保存 pms_spu_attr_value 基本属性值表(需要使用批量保存使用 service)
        saveBaseAttr(spu, spuId);

        // 2. 保存 sku 相关信息表
        saveSkuInfo(spu, spuId);
    }

    private void saveSkuInfo(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        // skus 不为 null 才进行保存
        if (CollectionUtils.isNotEmpty(skus)) {
            // 2.1 保存 pms_sku
            skus.forEach(skuVo -> { // 每一个 skuVo 就是 一个 sku
                skuVo.setSpuId(spuId); // 设置 spuId
                skuVo.setBrandId(spu.getBrandId()); // 设置品牌 Id
                skuVo.setCategoryId(spu.getCategoryId()); // 设置分类 Id

                // 获取 图片列表
                List<String> images = skuVo.getImages();
                // 如果图片列表不为空才设置默认图片
                if (CollectionUtils.isNotEmpty(images)) {
                    skuVo.setDefaultImage(
                            // 判断 默认图片是否为空, 如果不为空设置为默认图片, 如果为空将 images 第一张设置为默认图片. 以后前端设置默认图片无需更改代码
                            StringUtils.isNotBlank(skuVo.getDefaultImage())
                                    ? skuVo.getDefaultImage() : images.get(0)
                    );
                }

                skuMapper.insert(skuVo);

                // 获取 保存后回显的 skuId, 提供给下方使用
                Long skuId = skuVo.getId();

                // 2.2 保存 pms_sku_images 本质与 sku 是同一张表, 如果不为空才需要保存图片
                if (CollectionUtils.isNotEmpty(images)) {
                    imagesService.saveBatch(
                            // 需要将 List<String> 转换为 List<skuImagesEntity>
                            images.stream().map(image -> {
                                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                skuImagesEntity.setSkuId(skuId); // 设置 sku Id
                                skuImagesEntity.setUrl(image); // 将 集合的每一个图片 设置为 Url
                                skuImagesEntity.setSort(0); // 设置排序字段
                                skuImagesEntity.setDefaultStatus( // 设置默认图片
                                        // 将刚刚设置的默认图片与 遍历的图片比对, 相等 为 1 不等 为 0. url 互联网唯一
                                        StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0
                                );
                                return skuImagesEntity;
                            }).collect(Collectors.toList())
                    );
                }

                // 2.3 保存 pms_sku_attr_value 销售属性值表
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                // 如果 saleAttrs 不为空才需要保存
                if (CollectionUtils.isNotEmpty(saleAttrs)) {
                    saleAttrs.forEach(skuAttrValueEntity -> {
                        skuAttrValueEntity.setSkuId(skuId); // 设置 skuId
                        skuAttrValueEntity.setSort(0); // 设置排序字段
                    });

                    saleAttrService.saveBatch(saleAttrs);
                }

                // 3. 保存 营销 信息 远程 -> com/atguigu/gmall/sms/SkuBoundsController
                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo, skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                smsClient.saveSales(skuSaleVo);
            });
        }
    }

    private void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        // baseAttrs 不为空才需要保存 spu 基本信息
        if (CollectionUtils.isNotEmpty(baseAttrs)) {
            baseAttrService.saveBatch(
                    // 将 List<SpuAttrValueVo> 转换为 List<spuAttrValueEntity>
                    baseAttrs.stream().map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        // 将 spuAttrValueVo 中的值赋值给 spuAttrValueEntity, 需要在设值前 拷贝, 否则会出现 数据丢失
                        BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity); // 源 -> 对象(从 源中 拷贝到 对象)
                        // 设置 spuId
                        spuAttrValueEntity.setSpuId(spuId);
                        // 设置排序字段
                        spuAttrValueEntity.setSort(
                                // 如果 spuAttrValueVo.getSort() 值为 null 则设置为 0
                                Optional.ofNullable(spuAttrValueVo.getSort()).orElse(0)
                        );
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList())
            );
        }
    }

    /**
     * 此处想演示的是, 当 saveSpuDesc 方法下出现异常时 saveSpuInfo 保存进行回滚, saveSpuDesc 开启新的事物 保存成功
     * 　　Transactional 默认的传播行为 REQUIRES 支持当前事务，如果不存在，就新建一个.
     * 　　　　　　　　　　　需要手动设置　REQUIRES_NEW 如果有事务存在，挂起当前事务，创建一个新的事务.
     *
     * 测试:
     *      1. saveSpuDesc 方法下人为制作异常
     *      2. saveSpuDesc 保存成功不进行回滚 添加 REQUIRES_NEW 属性. 因为 private 不能被 增强 所以修改为 public
     *          Transactional 是基于 aop 的, aop 需要对一个方法进行增强. 私有方法在外面无法被获取到, 切面类无法对该方法进行增强
     *      3. jdk 代理(默认) 基于 接口代理.接口中没有该方法无法进行增强, 所以 SpuService 扩展该方法
     *
     * 结果:
     *      数据仍然进行回滚
     *
     * 原因: 事物的传播行为是(一个 sevice 的方法 调用 另一个 service 的方法时 事务之间的影响 spirng 特有)
     *      想要做到 saveBaseAttr 方法事物传播行为生效. 应该将方法放入 另一个 service 中. 在此处相当于 类调用方法 不是通过代理类调用. 事物注解没有生效, 传播行为更不可能生效
     *
     * @param spu
     * @param spuId
     */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void saveSpuDesc(SpuVo spu, Long spuId) {
//        List<String> spuImages = spu.getSpuImages();
//        // spuImages 不为空才进行保存 spu 信息介绍表
//        if (CollectionUtils.isNotEmpty(spuImages)) {
//            SpuDescEntity spuDescEntity = new SpuDescEntity();
//            // 本质与 spu 是同一张表, 没有自己的 Id 需要 设置 spuId
//            spuDescEntity.setSpuId(spuId); // 设置图片 id
//            // 将集合以 "," 拼接符 拼接到一起形成新的 字符串
//            spuDescEntity.setDecript(StringUtils.join(spuImages, ",")); // ["1", "2"] -> "1,2"
//            descMapper.insert(spuDescEntity);
//        }
//    }

    private Long saveSpuInfo(SpuVo spu) {
        spu.setCreateTime(new Date());
        // 再创建时间会导致不一致, 直接获取上一个设置的时间即可
        spu.setUpdateTime(spu.getCreateTime());
        save(spu);

        // 保存完 spu 后主键回显 抽取 spuId 给以下保存方法使用
        Long spuId = spu.getId();
        return spuId;
    }

}