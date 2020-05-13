package com.zz.scservice1;

import com.zz.sccommon.config.ClientSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Francis.zz on 2018/2/27.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ImportAutoConfiguration({ClientSecurityConfig.class})
public class ScService1Application {
    public static void main(String[] args) {
        SpringApplication.run(ScService1Application.class, args);
    }
}
