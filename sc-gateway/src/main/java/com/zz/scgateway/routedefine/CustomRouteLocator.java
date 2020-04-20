package com.zz.scgateway.routedefine;

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
    
    @Override
    public Flux<Route> getRoutes() {
        if(routeRuleProp == null || routeRuleProp.getRules().isEmpty()) {
            return Flux.empty();
        }
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        
        return routeRuleProp.getRoute(builder);
    }
}
