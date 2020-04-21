package com.zz.scgatewaynew.routedefine;

import com.zz.scgatewaynew.routedefine.predicaterule.PredicateGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
@ConfigurationProperties(prefix = "route")
@Component
@Data
@Slf4j
public class RouteRuleProp {
    private List<RouteRule> rules;
    
    private PredicateGroup commonPredicate;
    
    public Flux<Route> getRoute(RouteLocatorBuilder.Builder builder) {
        try {
            this.rules.forEach(routeRule -> routeRule.getRoute(builder, this.commonPredicate));
        } catch (Exception e) {
            log.error("路由规则配置失败", e);
            return Flux.empty();
        }
        return builder.build().getRoutes()
                .doOnNext(route -> log.info("注册路由id:" + route.getId()));
    }
    
    public boolean validate(RouteLocatorBuilder.Builder builder) {
        if(rules == null) {
            log.warn("not found RouteRule config");
            return false;
        }
        try {
            this.rules.forEach(routeRule -> routeRule.getRoute(builder, this.commonPredicate));
        } catch (Exception e) {
            log.error("路由规则配置失败", e);
            return false;
        }
        return true;
    }
}
