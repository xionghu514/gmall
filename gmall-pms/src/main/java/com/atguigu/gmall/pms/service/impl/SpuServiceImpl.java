package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

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

    @Override
    public void bigSave(SpuVo spu) {
        // 1. 保存 spu 相关信息
        // 1.1 保存 spu 表
        // 1.2 保存 pms_spu_desc 本质与 spu 是同一张表
        // 1.3 保存 pms_spu_attr_value 基本属性值表

        // 2. 保存 sku 相关信息
        // 2.1 保存 pms_sku
        // 2.2 保存 pms_sku_images 本质与 sku 是同一张表, 如果不为空才需要保存图片
        // 2.3 保存 pms_sku_attr_value 销售属性值表

        // 3. 保存 营销 相关信息
        // 3.1 保存积分优惠表
        // 3.2 保存满减优惠表
        // 3.3 保存打折优惠表
    }

}