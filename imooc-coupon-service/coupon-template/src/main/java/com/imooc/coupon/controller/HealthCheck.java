package com.imooc.coupon.controller;

import com.imooc.coupon.exception.CouponException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthCheck {
    // 服务发现客户端
    @Resource
    private DiscoveryClient client;

    // 服务注册接口，提供了获取服务id的方法
    @Resource
    private Registration registration;

    @GetMapping("/health")
    public String health(){
        return "CouponTemplate Is OK!";
    }

    /**
     * 异常测试接口，验证自定义异常是否可用
     * @throws CouponException
     */
    @GetMapping("/exception")
    public void exception() throws CouponException{
        throw new CouponException("CouponTemplate Has Some Problem");
    }

    /**
     * 获取服务端中的实例信息
     * @return
     */
    @GetMapping("/info")
    public List<Map<String, Object>> info(){
        // 大约需要等待两分钟时间才能获取到注册信息
        List<ServiceInstance> instances = client.getInstances(registration.getServiceId());//本服务的信息

        List<Map<String, Object>> result = new ArrayList<>(instances.size());
        instances.forEach(i -> {
            Map<String, Object> info = new HashMap<>();
            info.put("serviceId", i.getServiceId());
            info.put("instanceId", i.getInstanceId());
            info.put("port", i.getPort());
            result.add(info);
        });
        return result;
    }
}
