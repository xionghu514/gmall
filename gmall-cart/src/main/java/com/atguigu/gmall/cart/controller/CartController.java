package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/25 12:44
 * @Email: moumouguan@gmail.com
 */
@Controller
public class CartController {

//    @Autowired
//    private LoginInterceptor loginInterceptor;

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车成功, 重定向到购物车成功页
     *
     * @param cart
     * @return
     */
    @GetMapping
    public String saveCart(Cart cart) {

        if (cart == null || cart.getSkuId() == null) {
            throw new RuntimeException("没有选择添加到购物车的商品信息！");
        }

        cartService.saveCart(cart);

        // 重定向到新增成功页
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    /**
     * 新增成功跳转新增成功页
     * @return
     */
    @GetMapping("addCart.html")
    public String queryCart(Cart cart, Model model) {
        // 覆盖前取出当前传入的 count
        BigDecimal count = cart.getCount();
        // 返回的是购物车对象, 对传入的参数进行覆盖
        cart = cartService.queryCartBySkuId(cart.getSkuId());
        // 覆盖前的 count 设置进去
        cart.setCount(count);

        model.addAttribute("cart", cart);

        return "addCart";
    }

    @GetMapping("test")
    @ResponseBody
    public String test() {
//    public String test(HttpServletRequest request){

//        System.out.println("Controller 方法执行了" + loginInterceptor.getUserInfo());

//        System.out.println("Controller 方法执行了" + request.getAttribute("userId"));
//        System.out.println("Controller 方法执行了" + request.getAttribute("userKey"));

        System.out.println(LoginInterceptor.getUserInfo());

        return "hello cart!";
    }
}
