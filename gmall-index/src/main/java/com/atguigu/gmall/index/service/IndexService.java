package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/17 10:24
 * @Email: moumouguan@gmail.com
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    public List<CategoryEntity> queryLvl1Categories() {
        // 通过已有接口直接调用 传参 0 即可查询全部一级分类
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryCategoriesByPid(0L);

        return categoryResponseVo.getData();
    }

    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLevel23CategoriesByPid(pid);
        List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

        return categoryEntities;
    }
}
