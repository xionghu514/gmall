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

    /**
     * 在日常开发中，我们的逻辑都是 同步调用，顺序执行。在一些场景下，我们会希望异步调用，将和主线程关联度低的逻辑 异步调用，以实现让主线程更快的执行完成，提升性能。
     * 考虑到异步调用的 可靠性，我们一般会考虑引入分布式消息队列, 例如说 RabbitMQ、RocketMQ、Kafka 等等。但是在一些时候，我们并不需要这么高的可靠性，可以使用 进程内 的队列或者线程池
     *
     *      本地异步: 一个工程内, jvm 4 种多线程方式。性能较高, 可靠性相对较差
     *      分布式异步: 跨工程, MQ异步。性能相对较低, 可靠性较高
     *
     *      编程式异步: 四种多线程方式
     *      声明式异步: SpringTask 提供了一套注解 @EnableAsync 启用异步功能 @Async 开启异步
     *
     * @return
     */
    @GetMapping("test2")
    @ResponseBody
    public String test2(){
//        System.out.println("Controller 方法执行了" + LoginInterceptor.getUserInfo());

        long now = System.currentTimeMillis();
        System.out.println("controller.test 方法开始执行！");
        this.cartService.executor1();
        this.cartService.executor2();
        System.out.println("controller.test 方法结束执行！！！" + (System.currentTimeMillis() - now));

        return "hello cart!";
    }
}
