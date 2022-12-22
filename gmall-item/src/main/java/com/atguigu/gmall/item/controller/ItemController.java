package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.pojo.ItemVo;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:29
 * @Email: moumouguan@gmail.com
 */
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}.html")
    @ResponseBody
    public ResponseVo<ItemVo> loadData(@PathVariable("skuId") Long skuId) {
        ItemVo itemVo = itemService.loadData(skuId);

        return ResponseVo.ok(itemVo);
    }

}
