package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:49
 * @Email: moumouguan@gmail.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
