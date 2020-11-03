package com.zz.scgatewaynew.routedefine;

import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.zz.gateway.common.routedefine.RouteRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-08 10:14
 * ************************************
 */
@Slf4j
public class GatewayRouteManager {
    private static ApplicationEventPublisher PUBLISHER;
    private static volatile List<RouteRule> GATEWAY_ROUTE_RULE = Collections.synchronizedList(new ArrayList<>());
    
    private static final GatewayRouteManager.GatewayRoutePropertyListener LISTENER = new GatewayRouteManager.GatewayRoutePropertyListener();
    private static SentinelProperty<List<RouteRule>> currentProperty = new DynamicSentinelProperty<>();
    
    static {
        currentProperty.addListener(LISTENER);
    }
    
    public static void register2Property(SentinelProperty<List<RouteRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }
    
    public static void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        GatewayRouteManager.PUBLISHER = eventPublisher;
    }
    
    /**
     * 获取当前最新的路由规则配置
     *
     * @return
     */
    public static List<RouteRule> latestRouteRule() {
        return new ArrayList<>(GATEWAY_ROUTE_RULE);
    }
    
    public static boolean loadRouteRules(List<RouteRule> rules) {
        return currentProperty.updateValue(rules);
    }
    
    private static final class GatewayRoutePropertyListener implements PropertyListener<List<RouteRule>> {
        
        @Override
        public void configUpdate(List<RouteRule> conf) {
            applyGatewayRouteInternal(conf);
        }
        
        @Override
        public void configLoad(List<RouteRule> conf) {
            applyGatewayRouteInternal(conf);
        }
        
        private synchronized void applyGatewayRouteInternal(List<RouteRule> conf) {
            GATEWAY_ROUTE_RULE.clear();
            
            if(!CollectionUtils.isEmpty(conf)) {
                GATEWAY_ROUTE_RULE.addAll(conf);
            }
            // 刷新路由配置
            if(PUBLISHER == null) {
                log.debug("not init ApplicationEventPublisher, so could not publish RefreshRoutesEvent");
                return;
            }
            log.info("update route rule from nacos, refresh RefreshRoutesEvent");
            PUBLISHER.publishEvent(new RefreshRoutesEvent(this));
        }
    }
    
    private GatewayRouteManager() {}
}
