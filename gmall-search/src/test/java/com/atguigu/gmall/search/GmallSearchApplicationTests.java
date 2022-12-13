package com.atguigu.gmall.search;

import com.atguigu.gmall.search.pojo.Goods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // 索引库的操作对象
        IndexOperations indexOps = restTemplate.indexOps(Goods.class);
        // 创建索引库
        indexOps.create();
        // 声明映射
        indexOps.putMapping(indexOps.createMapping());
    }

}
