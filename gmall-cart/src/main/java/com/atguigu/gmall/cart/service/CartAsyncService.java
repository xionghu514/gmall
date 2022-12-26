package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/26 02:28
 * @Email: moumouguan@gmail.com
 */
@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    // 需要异步执行新增购物车 需要添加 async 注解, 不能直接在 update() 方法上直接加所以需要封装一个方法
    // 因为 注解是 基于 aop 的所以为了能让该注解生效我们应该注入代理类对象调用该方法
    @Async
    public void updateCart(String userId, String skuId, Cart cart) {
        cartMapper.update(
                cart, new UpdateWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("sku_id", skuId)
        );
    }

    @Async
    public void insertCart(String userId, Cart cart) {

        // 模拟异步新增购物车失败
//        int i = 1 / 0;

        cartMapper.insert(cart);
    }

    @Async
    public void deleteByUserId(String userId) {
//        System.out.println("执行到删除了");
        cartMapper.delete(
                new UpdateWrapper<Cart>().eq("user_id", userId)
        );
    }

    @Async
    public void deleteByUserIdAndSkuId(String userId, Long skuId) {
        cartMapper.delete(
                new UpdateWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("sku_id", skuId)
        );
    }
}
