package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/16 11:09
 * @Email: moumouguan@gmail.com
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 加载分类应该调用 service 访问远程接口加载数据
     *      1. 访问首页时应该加载一级分类
     *      2. 鼠标划过 1 级分类时在去加载其下的二级分类以及二级分类下的三级分类
     * @param model
     * @return
     */
    @GetMapping("/**")
    public String toIndex(Model model) { // 访问任意路径都可以到达首页, /** 表示任意路径
        // 加载一级分类
        List<CategoryEntity> categoryEntityList = indexService.queryLvl1Categories(); // 加载一级分类不需要参数,
        model.addAttribute("categories", categoryEntityList); // 通过 model 将数据携带给页面

        // TODO: 加载广告

        return "index";
    }
}
