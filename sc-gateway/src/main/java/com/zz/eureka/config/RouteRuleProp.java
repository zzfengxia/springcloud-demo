package com.zz.eureka.config;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 * 动态路由规则配置
 *
 * @author Francis.zz
 * @date 2020-03-26 17:46
 * ************************************
 */
@NacosConfigurationProperties(prefix = "route", dataId = "demo.gateway.flow.rule.yaml", groupId = "sentinel:demo", type = ConfigType.YAML, autoRefreshed = true)
@Configuration
@Data
public class RouteRuleProp {
    /**
     * NacosConfigurationProperties 属性绑定的实现查看 {@link com.alibaba.nacos.spring.context.properties.config.NacosConfigurationPropertiesBinder#bind(Object, String, NacosConfigurationProperties)}
     */
    private List<RouteRule> rules;
}
