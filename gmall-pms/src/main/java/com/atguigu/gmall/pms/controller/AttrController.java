package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
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
 * 商品属性
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
@Api(tags = "商品属性 管理")
@RestController
@RequestMapping("pms/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    /**
     * baseCrud: 8. 根据分类 id 以及 type 和 searchType 查询分类下的规格参数
     *
     * 　请求路径
     * 　　　http://api.gmall.com/pms/attr/category/225?type=0
     *                          /pms/attr/category/{cid}?type=0&searchType=1
     *
     * @param cid
     * @param type
     * @param searchType
     * @return
     */
    @GetMapping("category/{cid}")
    @ApiOperation("查询分类下的规格参数")
    public ResponseVo<List<AttrEntity>> queryAttrsByCidOrTypeOrSearchType(
            @PathVariable("cid") Long cid, // 路径传参不能为 null
            // defaultValue 设置默认值, 如果设置默认值可能会对本意产生歧意, required 默认为 true 设置为 false
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "searchType", required = false) Integer searchType
    ) {
        List<AttrEntity> attrEntities = attrService.queryAttrsByCidOrTypeOrSearchType(cid, type, searchType);

        return ResponseVo.ok(attrEntities);
    }

    /**
     * baseCrud: 3. 根据 属性规格分组 id 查询 组以及组下规格参数
     *
     * 　请求路径
     * 　　　　http://api.gmall.com/pms/attr/group/1
     * 　　　　　　　　　　　　　　　　/pms/attr/group/{gid}
     * @param gid
     * @return
     */
    @GetMapping("/group/{gid}")
    public ResponseVo<List<AttrEntity>> queryAttrsByGid(@PathVariable("gid") Long gid) {
        // select * from pms_attr where group_id = gid;
        List<AttrEntity> groupEntities = attrService.list(
                new QueryWrapper<AttrEntity>().eq("group_id", gid)
        );

        return ResponseVo.ok(groupEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrEntity> queryAttrById(@PathVariable("id") Long id){
		AttrEntity attr = attrService.getById(id);

        return ResponseVo.ok(attr);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrEntity attr){
		attrService.save(attr);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
