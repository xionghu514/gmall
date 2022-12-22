package com.atguigu.gmall.sms.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/21 12:28
 * @Email: moumouguan@gmail.com
 */
@Data
public class ItemSaleVo {

    private Long saleId; // 营销 id
    private String type; // 营销 类型
    private String desc; // 营销 描述

}
