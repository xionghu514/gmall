package com.atguigu.gmall.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/16 11:09
 * @Email: moumouguan@gmail.com
 */
@Controller
public class IndexController {

    @GetMapping("/**")
    public String toIndex(Model model) { // 访问任意路径都可以到达首页, /** 表示任意路径

        return "index";
    }
}
