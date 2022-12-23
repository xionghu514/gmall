package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/24 02:19
 * @Email: moumouguan@gmail.com
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {

}
