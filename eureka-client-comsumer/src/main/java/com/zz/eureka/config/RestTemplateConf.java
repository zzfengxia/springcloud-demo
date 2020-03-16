package com.zz.eureka.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 配置RestTemplate,可作为Rest客户端使用
 */
@Configuration
public class RestTemplateConf {
    @Bean
    @Primary
    public RestTemplate getTemplate() {
        return new RestTemplate();
    }

    /**
     * 可使用负载功能
     *
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate getBalanceTemplate() {
        return new RestTemplate();
    }
}
