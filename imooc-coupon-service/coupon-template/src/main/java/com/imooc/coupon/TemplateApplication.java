package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling// 启用定时任务
@EnableEurekaClient
@EnableJpaAuditing
@SpringBootApplication
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }
}


