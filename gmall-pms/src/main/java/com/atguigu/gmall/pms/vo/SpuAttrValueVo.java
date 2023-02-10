package com.atguigu.gmall.pms.vo;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description: 基本属性
 * @Author: Guan FuQing
 * @Date: 2023/2/9 23:36
 * @Email: moumouguan@gmail.com
 */
@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {

    //    private List<String> valueSelected;
//
//    public void setValueSelected(List<String> valueSelected) {
//
//        // 如果接受的集合为空, 则不设置
//        if (CollectionUtils.isEmpty(valueSelected))
//            return;
//
//        this.valueSelected = valueSelected;
//    }

    public void setValueSelected(List<String> valueSelected) {
        // 如果接受的集合为空, 则不设置
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }

        // 将接受的集合根据 "," 分割为字符串 赋值给 AttrValue 属性
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }

}
