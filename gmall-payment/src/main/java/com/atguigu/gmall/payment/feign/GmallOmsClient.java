package com.atguigu.gmall.payment.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/29 06:38
 * @Email: moumouguan@gmail.com
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
