package com.atguigu.gmall.pms.controller;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.gmall.common.bean.ResponseVo;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/2/9 15:22
 * @Email: moumouguan@gmail.com
 */
@Api(tags = "阿里云 OSS 图片上传 管理")
@RestController
@RequestMapping("pms/oss")
public class OssController {

    String accessId = "LTAI5tM6zGYSk2mCQvQUH5A6"; // 请填写您的AccessKeyId。
    String accessKey = "7W9fOvhqiKKlxWOVuqPOjCLo4qVPQi"; // 请填写您的AccessKeySecret。
    String endpoint = "oss-cn-hangzhou.aliyuncs.com"; // 请填写您的 endpoint。
    String bucket = "yiki-gmall"; // 请填写您的 bucketname 。
    String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
    // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
    //String callbackUrl = "http://88.88.88.88:8888";
    // 图片目录，每天一个目录
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dir = sdf.format(new Date()); // 用户上传文件时指定的前缀。

    @GetMapping("policy")
    public ResponseVo<Object> policy() throws UnsupportedEncodingException {

        OSSClient client = new OSSClient(endpoint, accessId, accessKey);

        long expireTime = 30;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

        String postPolicy = client.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes("utf-8");
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        Map<String, String> respMap = new LinkedHashMap<String, String>();
        respMap.put("accessid", accessId);
        respMap.put("policy", encodedPolicy);
        respMap.put("signature", postSignature);
        respMap.put("dir", dir);
        respMap.put("host", host);
        respMap.put("expire", String.valueOf(expireEndTime / 1000));
        // respMap.put("expire", formatISO8601Date(expiration));

        return ResponseVo.ok(respMap);
    }
}
