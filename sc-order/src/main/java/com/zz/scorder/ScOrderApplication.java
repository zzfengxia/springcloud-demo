package com.zz.scorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务消费者，向Eureka服务注册中心注册服务
 * EnableFeignClients的EnableFeignClients指定需要扫描的feign client包
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zz.scservice"})
public class ScOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScOrderApplication.class, args);
    }
}
