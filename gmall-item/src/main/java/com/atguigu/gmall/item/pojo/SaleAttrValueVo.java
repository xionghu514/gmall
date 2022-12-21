package com.atguigu.gmall.item.pojo;

import lombok.Data;

import java.util.List;

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
    private List<String> attrValue;

}
