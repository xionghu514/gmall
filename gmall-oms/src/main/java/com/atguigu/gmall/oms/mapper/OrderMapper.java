package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:50:27
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    // 订单唯一标识, 期望状态, 目标状态
    public int updateStatus(@Param("orderToken") String orderToken, @Param("expect") Integer expect, @Param("target") Integer target);
	
}
