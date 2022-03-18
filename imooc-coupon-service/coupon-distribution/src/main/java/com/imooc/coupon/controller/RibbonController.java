package com.imooc.coupon.controller;


import com.imooc.coupon.annotation.IgnoreResponseAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class RibbonController {
    @Autowired
    private RestTemplate restTemplate;

    // 通过ribbon调用模板微服务
    @GetMapping("/info")
    @IgnoreResponseAdvice // 忽略通用响应的包装
    public TemplateInfo getTemplateInfo(){
        String infoUrl = "eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity(infoUrl, TemplateInfo.class).getBody();
    }


    // 模板微服务的源信息
    private static class TemplateInfo{
        private Integer code;
        private String message;
        private List<Map<String, Object>> data;
    }
}
