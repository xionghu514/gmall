package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品会员价格
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2023-02-09 13:55:42
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

