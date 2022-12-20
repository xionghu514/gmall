package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/20 19:21
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Bean
    public RBloomFilter rBloomFilter(){
        // 初始化布隆过滤器
        RBloomFilter<String> bloomfilter = this.redissonClient.getBloomFilter("index:bf ");
        bloomfilter.tryInit(2000, 0.03);

        // 向 步隆过滤器中初始化数据: 分类 广告
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity -> {
                bloomfilter.add(categoryEntity.getId().toString());
            });
        }
        return bloomfilter;
    }
}
