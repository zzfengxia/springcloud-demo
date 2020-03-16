package com.zz.eureka.service;

import com.alibaba.fastjson.JSON;
import com.zz.eureka.common.RouteInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 * 自定义RouteDefinition获取方式，覆盖默认的 GatewayAutoConfiguration.inMemoryRouteDefinitionRepository注入
 *
 * @author Francis.zz
 * @date 2020-03-13 14:52
 * ************************************
 */
@Slf4j
@Component
public class CustomRouteDefinitionRepository implements RouteDefinitionRepository {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        log.info("CustomRouteDefinitionRepository get invoking...");
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        Set<String> routeKeys = redisTemplate.keys("config:card:" + "*");
        if(routeKeys == null || routeKeys.size() == 0) {
            log.error("not found routing config");
            return Flux.fromIterable(routeDefinitions);
        }
        redisTemplate.opsForValue().multiGet(routeKeys).stream().forEach(routeInfoJson -> {
            RouteInfo routeInfo = JSON.parseObject(routeInfoJson, RouteInfo.class);
            // text格式： route1=http://127.0.0.1,Host=baidu.com,Path=/get
            // eg： routeId=http://127.0.0.1,Host=**.addrequestparameter.org,Path=/get
            String defineText = StringUtils.join(routeInfo.genId(), "=", routeInfo.getServerUrl(), ",Path=", routeInfo.getCardExternalCode());
            RouteDefinition routeDefinition = new RouteDefinition(defineText);
            routeDefinitions.add(routeDefinition);
        });
        return Flux.fromIterable(routeDefinitions);
    }
    
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        log.info("CustomRouteDefinitionRepository save invoking...");
        return null;
    }
    
    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        log.info("CustomRouteDefinitionRepository delete invoking...");
        return null;
    }
}
