package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description: 营销信息远程接口
 * @Author: Guan FuQing
 * @Date: 2023/2/10 15:14
 * @Email: moumouguan@gmail.com
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {


}
