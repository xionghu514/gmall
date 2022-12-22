package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 属性分组
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    // 商品详情页 12. 查询规格参数分组及组下的规格参数和值
    @GetMapping("with/attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId
    ) {
        List<ItemGroupVo> groupVos = attrGroupService.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(cid, spuId, skuId);

        return ResponseVo.ok(groupVos);
    }

    /**
     * baseCrud: 7. 根据分类 id 查询分类下的组及规格参数
     *
     * 　请求路径
     * 　　　　http://api.gmall.com/pms/attrgroup/withattrs/225
     * 　　　　　　　　　　　　　　　　/pms/attrgroup/withattrs/{catId}
     *
     * @param cid
     * @return
     */
    @GetMapping("/withattrs/{catId}")
    public ResponseVo<List<AttrGroupEntity>> queryGroupsWithAttrsByCid(@PathVariable("catId") Long cid) {
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.queryGroupsWithAttrsByCid(cid);

        return ResponseVo.ok(attrGroupEntities);
    }

    /**
     * baseCrud: 2. 根据分类 id 查询属性规格分组
     *
     * 　请求路径
     * 　　　　http://api.gmall.com/pms/attrgroup/category/225
     * 　　　　　　　　　　　　　　　　/pms/attrgroup/category/{cid}
     * @param cid
     * @return
     */
    @GetMapping("/category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryGroupsByCid(@PathVariable("cid") Long cid) {
        // select * from pms_attr_group where category_id = cid;
        List<AttrGroupEntity> groupEntities = attrGroupService.list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid)
        );

        return ResponseVo.ok(groupEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
