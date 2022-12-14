package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/13 20:30
 * @Email: moumouguan@gmail.com
 */
public interface GmallPmsApi {

    /**
     * es 数据导入 提供远程接口, 1. 分批、分页查询 spu
     *      阉割版的 http 协议, 支持占位符, 支持普通参数, 不支持 form 表单
     *
     *      普通参数 ? 只能使用 @requestParam 一一接收
     * 		     不支持 fome 表单传入对象
     *
     *      json: 传递多个参数, @RequestBody 接收, 只支持 Post 请求
     */
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    // es 数据导入 提供远程接口, 2. 根据 spuId 查询 sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);
}
