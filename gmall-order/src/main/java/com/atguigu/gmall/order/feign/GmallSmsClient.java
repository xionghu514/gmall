package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/22 08:49
 * @Email: moumouguan@gmail.com
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
