package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/14 14:05
 * @Email: moumouguan@gmail.com
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public ResponseVo<SearchResponseVo> search(SearchParamVo paramVo) {
        SearchResponseVo responseVo = searchService.search(paramVo);

        return ResponseVo.ok(responseVo);
    }
}
