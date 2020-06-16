package com.zz.scgatewaynew.routedefine;

import com.zz.gateway.common.routedefine.RouteRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

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
    
    private volatile Flux<Route> currentRoute = Flux.empty();
    /**
     * nacos刷新时会有refreshAll调用， RouteRefreshListener 会监听到然后重置RefreshRoutesEvent事件，所以这里也会刷新
     * RouteRefreshListener会监听HeartbeatEvent事件，当开启NacosWatch时会定时发布HeartbeatEvent事件，所以这里也会刷新。
     * 如果网关不需要自动刷新新服务的路由(即spring.cloud.gateway.discovery.locator.enabled: false)则可以关闭NacosWatch(spring.cloud.nacos.discovery.watch.enabled: false).
     *
     * @see {@link org.springframework.cloud.gateway.route.RouteRefreshListener}
     * @see {@link com.alibaba.cloud.nacos.refresh.NacosContextRefresher}#registerNacosListener
     * @see {@link org.springframework.cloud.endpoint.event.RefreshEventListener#handle}
     * @see {@link org.springframework.cloud.client.discovery.event.HeartbeatEvent}
     * @return
     */
    @Override
    public Flux<Route> getRoutes() {
        List<RouteRule> currentRules = GatewayRouteManager.latestRouteRule();
        if(CollectionUtils.isEmpty(currentRules)) {
            log.error("routeRuleProp is null, unable build route");
            return Flux.empty();
        }
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        String routeId = null;
        try {
            for (RouteRule rule : currentRules) {
                if(!rule.isValid()) {
                    continue;
                }
                routeId = rule.getId();
                rule.getRoute(builder);
            }
            currentRoute = builder.build().getRoutes()
                    .doOnNext(route -> log.info("注册路由id:" + route.getId()));
        } catch (Exception e) {
            log.error("route id [" + routeId + "] build custom route rule error ", e);
        }
        
        return currentRoute;
    }
}
