package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/21 19:45
 * @Email: moumouguan@gmail.com
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValue;

}
