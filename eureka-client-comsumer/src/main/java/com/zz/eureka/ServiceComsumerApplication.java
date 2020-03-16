package com.zz.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务消费者，向Eureka服务注册中心注册服务
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceComsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceComsumerApplication.class, args);
    }
}
