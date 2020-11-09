package com.zz.scgatewaynew.webfilter;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.MonoSentinelOperator;
import com.zz.gateway.common.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-10-26 16:29
 * ************************************
 */
@Component
@Slf4j
public class RecordNoRouteFilter implements WebFilter, Ordered {
    
    @Override
    public int getOrder() {
        return 10;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Mono<Void> asyncResult = chain.filter(exchange);
    
        return asyncResult.transform(
                publisher -> publisher.onErrorResume(error -> {
                    if(error instanceof ResponseStatusException) {
                        if(HttpStatus.NOT_FOUND.equals(((ResponseStatusException) error).getStatus())
                                && exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR) == null) {
                            // 未找到路由，记录到sentinel
                            return new MonoSentinelOperator<>(publisher, new EntryConfig(GatewayConstants.RESOURCE_FOR_NOROUTE,
                                    ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, 1,
                                    null, new ContextConfig(SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX + GatewayConstants.RESOURCE_FOR_NOROUTE)));
                        }
                    }
                    return Mono.error(error);
                })
        );
    }
}
