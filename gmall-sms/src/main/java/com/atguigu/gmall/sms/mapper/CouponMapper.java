package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2023-02-09 13:55:42
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponEntity> {
	
}
