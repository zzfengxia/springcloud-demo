package com.zz.ops.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-27 15:47
 * ************************************
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class OpsAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpsAdminApplication.class, args);
    }
}
