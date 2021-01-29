package com.zz.scgatewaynew.sentinelcustom;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.GatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.matcher.WebExchangeApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 * 基于{@link SentinelGatewayFilter} 源码定制一些特性
 * 增加特性：针对请求body体属性限流
 * @author Francis.zz
 * @date 2021-01-28 17:40
 * ************************************
 */
public class CustomSentinelGatewayFilter extends SentinelGatewayFilter {
    public CustomSentinelGatewayFilter() {
        super(Ordered.HIGHEST_PRECEDENCE);
    }
    
    public CustomSentinelGatewayFilter(int order) {
        super(order);
    }
    
    private final CustomGatewayParamParser<ServerWebExchange> paramParser = new CustomGatewayParamParser<>(
            new CustomServerWebExchangeItemParser());
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        
        Mono<Void> asyncResult = chain.filter(exchange);
        if (route != null) {
            String routeId = route.getId();
            Object[] params = paramParser.parseParameterFor(routeId, exchange,
                    r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID);
            String origin = Optional.ofNullable(GatewayCallbackManager.getRequestOriginParser())
                    .map(f -> f.apply(exchange))
                    .orElse("");
            asyncResult = asyncResult.transform(
                    new SentinelReactorTransformer<>(new EntryConfig(routeId, ResourceTypeConstants.COMMON_API_GATEWAY,
                            EntryType.IN, 1, params, new ContextConfig(contextName(routeId), origin)))
            );
        }
        
        Set<String> matchingApis = pickMatchingApiDefinitions(exchange);
        for (String apiName : matchingApis) {
            Object[] params = paramParser.parseParameterFor(apiName, exchange,
                    r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
            asyncResult = asyncResult.transform(
                    new SentinelReactorTransformer<>(new EntryConfig(apiName, ResourceTypeConstants.COMMON_API_GATEWAY,
                            EntryType.IN, 1, params))
            );
        }
        
        return asyncResult;
    }
    
    private String contextName(String route) {
        return SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX + route;
    }
    
    Set<String> pickMatchingApiDefinitions(ServerWebExchange exchange) {
        return GatewayApiMatcherManager.getApiMatcherMap().values()
                .stream()
                .filter(m -> m.test(exchange))
                .map(WebExchangeApiMatcher::getApiName)
                .collect(Collectors.toSet());
    }
}
