package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * spu信息
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuByCidAndPage(Long cid, PageParamVo paramVo);

    void bigSave(SpuVo spu);

    // 因为 注解是 基于 AOP 的, AOP 是基于动态代理的. 动态代理分为两种 1. jdk 代理(默认) 基于 接口代理. 2. cglib 代理 基于类代理
    // 接口中没有该方法无法进行增强, 所以此处扩展该方法
//    public void saveSpuDesc(SpuVo spu, Long spuId);
}

