package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/27 23:24
 * @Email: moumouguan@gmail.com
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
