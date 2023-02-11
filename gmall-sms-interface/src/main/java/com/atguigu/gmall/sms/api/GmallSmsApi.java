package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2023/2/11 10:07
 * @Email: 1796235969@qq.com
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sales/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo saleVo);
}
