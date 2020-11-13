package com.zz.scorder.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.influx.InfluxDbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-30 11:36
 * ************************************
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(InfluxDBClient.class)
@EnableConfigurationProperties(InfluxDbProperties.class)
public class InfluxDbConfig {
    @Value("${spring.influx.database:}")
    private String database;
    @Value("${spring.influx.retentionPolicy:autogen}")
    private String retentionPolicy;
    
    @Bean
    @ConditionalOnProperty("spring.influx.url")
    public InfluxDBClient influxDb(InfluxDbProperties properties) {
        return InfluxDBClientFactory.createV1(
                properties.getUrl(), properties.getUser(), properties.getPassword().toCharArray(),
                database, retentionPolicy);
    }
}
