package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description: 营销信息远程接口
 * @Author: Guan FuQing
 * @Date: 2022/12/10 11:07
 * @Email: moumouguan@gmail.com
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

//    @PostMapping("sms/skubounds/sales/save")
//    public ResponseVo saveSales(@RequestBody SkuSaleVo saleVo);

}
