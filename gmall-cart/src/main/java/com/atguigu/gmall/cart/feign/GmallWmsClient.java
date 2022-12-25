package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 09:34
 * @Email: moumouguan@gmail.com
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
