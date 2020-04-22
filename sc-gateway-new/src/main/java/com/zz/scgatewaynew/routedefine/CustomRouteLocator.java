package com.zz.scgatewaynew.routedefine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 18:18
 * ************************************
 */
@Component
@Slf4j
public class CustomRouteLocator implements RouteLocator {
    @Autowired
    private RouteLocatorBuilder routeLocatorBuilder;
    @Autowired
    private RouteRuleProp routeRuleProp;
    
    private Flux<Route> currentRoute = Flux.empty();
    /**
     * nacos刷新时会有refreshAll调用， RouteRefreshListener 会监听到然后重置RefreshRoutesEvent事件，所以这里也会刷新
     *
     * @see {@link org.springframework.cloud.gateway.route.RouteRefreshListener}
     *
     * @return
     */
    @Override
    public Flux<Route> getRoutes() {
        if(routeRuleProp == null || routeRuleProp.getRules() == null || routeRuleProp.getRules().isEmpty()) {
            return currentRoute;
        }
        if(!routeRuleProp.validate(routeLocatorBuilder.routes())) {
            log.error("配置的路由规则有误，无法更新路由规则");
            return currentRoute;
        }
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
    
        currentRoute = routeRuleProp.getRoute(builder);
        return currentRoute;
    }
}
