package com.zz.gateway.common.routedefine;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 * 动态路由规则配置
 * nacso刷新时会调用RefreshEvent，最终会调用refreshAll
 * @see {@link com.alibaba.cloud.nacos.refresh.NacosContextRefresher#registerNacosListener}
 * @see {@link org.springframework.cloud.endpoint.event.RefreshEventListener#handle(org.springframework.cloud.endpoint.event.RefreshEvent)}
 *
 * @author Francis.zz
 * @date 2020-03-26 17:46
 * ************************************
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class RouteRuleWrapper {
    private List<RouteRule> rules;
    
    public RouteRuleWrapper(List<RouteRule> rules) {
        this.rules = rules;
    }
    
    public Flux<Route> getRoute(RouteLocatorBuilder.Builder builder) {
        this.rules.stream().filter(RouteRule::isValid)
                .map(rule -> {
                    rule.getRoute(builder);
                    return null;
                }).collect(Collectors.toList());
        return builder.build().getRoutes()
                .doOnNext(route -> log.info("注册路由id:" + route.getId()));
    }
    
    public void validate() {
        rules.forEach(RouteRule::validatePlus);
    }
    
    public boolean isValid() {
        try {
            for (RouteRule rule : rules) {
                if(!rule.isValid()) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("check route rule error", e);
            return false;
        }
        
        return true;
    }
}
