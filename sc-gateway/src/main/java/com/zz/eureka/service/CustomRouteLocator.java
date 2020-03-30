package com.zz.eureka.service;

import com.zz.eureka.config.RouteRuleProp;
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
public class CustomRouteLocator implements RouteLocator {
    @Autowired
    private RouteLocatorBuilder routeLocatorBuilder;
    @Autowired
    private RouteRuleProp routeRuleProp;
    
    @Override
    public Flux<Route> getRoutes() {
        if(routeRuleProp == null || routeRuleProp.getRules().isEmpty()) {
            return Flux.empty();
        }
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        routeRuleProp.getRules().forEach(routeRule -> {
            routeRule.getRoute(builder);
        });
        return builder.build().getRoutes();
    }
}
