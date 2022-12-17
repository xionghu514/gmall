package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/17 10:27
 * @Email: moumouguan@gmail.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
