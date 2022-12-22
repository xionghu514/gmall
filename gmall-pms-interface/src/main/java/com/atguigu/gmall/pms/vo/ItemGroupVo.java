package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/21 19:47
 * @Email: moumouguan@gmail.com
 */
@Data
public class ItemGroupVo {

    private Long id;
    private String name; // 分组名称
    private List<AttrValueVo> attrs; // 规格参数与值
}
