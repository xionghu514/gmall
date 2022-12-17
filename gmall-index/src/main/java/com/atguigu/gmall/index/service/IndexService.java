package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:";

    public List<CategoryEntity> queryLvl1Categories() {
        // 通过已有接口直接调用 传参 0 即可查询全部一级分类
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryCategoriesByPid(0L);

        return categoryResponseVo.getData();
    }

    // 一般 自定义前缀 + 请求参数作为 key, 返回结果集作为 value 放入缓存
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        // 1. 先查询缓存, 如果缓存命中则返回
        String json = redisTemplate.opsForValue().get(
                // key, value
                KEY_PREFIX + pid
        );
        // 不为空则
        if (StringUtils.isNotBlank(json)) {
            // 将 Json 字符串数据转换成集合对象
            return JSON.parseArray(json, CategoryEntity.class); // json 字符串, 转换类型
        }

        // 2. 远程调用, 查询数据库 并 放入缓存
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLevel23CategoriesByPid(pid);
        List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

        redisTemplate.opsForValue().set(
                // key, value
                KEY_PREFIX + pid, JSON.toJSONString(categoryEntities) // 将 Java 对象转换换为 JSON 对象
        );

        return categoryEntities;
    }
}
