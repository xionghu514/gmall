package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 保存营销信息使用的 vo
 *      不直接使用 SkuVo 的原因: 存在很多不需要的字段 sku 的图片列表, 销售属性 传输过程中占用大量网络带宽
 * @Author: Guan FuQing
 * @Date: 2023/2/10 15:07
 * @Email: moumouguan@gmail.com
 */
@Data
public class SkuSaleVo {
    private Long skuId;

    /*                       积分优惠信息                       */

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
//    private Integer work;
    private List<Integer> work;


    /*                       满减优惠信息                       */

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
//    private Integer addOther;
    private Integer fullAddOther;


    /*                       打折优惠信息                       */

    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
//    private Integer addOther;
    private Integer ladderAddOther;

}
