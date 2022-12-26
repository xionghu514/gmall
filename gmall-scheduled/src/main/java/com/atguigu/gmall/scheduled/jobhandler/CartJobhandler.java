package com.atguigu.gmall.scheduled.jobhandler;

import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/26 18:55
 * @Email: moumouguan@gmail.com
 */
@Component
public class CartJobhandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String EXCEPTION_KEY = "CART:EXCEPTION";
    private static final String KEY_PREFIX = "CART:INFO:";

    @XxlJob("cartSyncData")
    public ReturnT<String> cartSyncData(String param) {

        // 1. 从 redis 中读取异常信息(Set<UserId>)
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);

        // 遍历 userId 集合
        String userId = setOps.pop();
        while (StringUtils.isNotBlank(userId)) {

            // 3. 删除该用户 mysql 中所有购物车
            cartMapper.delete(
                    new UpdateWrapper<Cart>().eq("user_id", userId)
            );

            // 4. 读取该用户 redis 中的购物车
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();

            // 5. 判断 redis 中的购物车是否为空, 则结束
            if (CollectionUtils.isEmpty(cartJsons)) {
                // 获取下一个用户
                userId = setOps.pop();

                continue;
            }

            // 6. 把 redis 中的数据新增到 mysql
            cartJsons.forEach(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setId(null); // 防止主键冲突
                    cartMapper.insert(cart);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            // 获取下一个用户
            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }
}
