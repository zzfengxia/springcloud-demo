package com.zz.scservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Francis.zz on 2018/2/27.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ScServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScServiceApplication.class, args);
    }
}
