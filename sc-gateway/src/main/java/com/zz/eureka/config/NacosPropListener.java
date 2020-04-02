package com.zz.eureka.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.spring.context.event.config.NacosConfigurationPropertiesBeanBoundEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * NacosConfigurationProperties 属性绑定的实现查看 {@link com.alibaba.nacos.spring.context.properties.config.NacosConfigurationPropertiesBinder#bind(Object, String, NacosConfigurationProperties)}
 * 属性绑定后会发布 NacosConfigurationPropertiesBeanBoundEvent 事件
 * @author Francis.zz
 * @date 2020-03-26 15:46
 * ************************************
 */
@Component
@Slf4j
public class NacosPropListener implements ApplicationListener<NacosConfigurationPropertiesBeanBoundEvent>, ApplicationEventPublisherAware {
    @Autowired
    private RouteRuleProp predicateRulesList;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private RouteLocatorBuilder routeLocatorBuilder;
    
    @Override
    public void onApplicationEvent(NacosConfigurationPropertiesBeanBoundEvent event) {
        if(!(event.getBean() instanceof RouteRuleProp) || predicateRulesList == null) {
            return;
        }
        System.out.println("predicateRulesList:" + JSON.toJSONString(predicateRulesList.getRules()));
        if(!predicateRulesList.validate(routeLocatorBuilder.routes())) {
            log.error("配置的路由规则有误，无法更新路由规则");
            return;
        }
        // 调用刷新事件，更新路由缓存。由于没有使用 RouteDefinitionWriter 创建路由，所以这里无需清空操作
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
