package com.zz.scgatewaynew.webfilter;

import com.zz.gateway.common.GatewayConstants;
import com.zz.scgatewaynew.util.GatewayUtils;
import com.zz.scgatewaynew.util.IPAddrUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
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
public class RecordLogFilter implements WebFilter, Ordered {
    
    /**
     * 需要在{@link org.springframework.cloud.sleuth.instrument.web.TraceWebFilter} 之后2个执行
     */
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ipstr = IPAddrUtils.getClientIp(exchange.getRequest());
        log.info("client ip:" + ipstr + ", " + GatewayUtils.formatRequest(exchange.getRequest()));
        log.info("request headers:" + exchange.getRequest().getHeaders().toString());
        Object reqBody = exchange.getAttributes().get(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);
        if(reqBody != null) {
            log.info("request body:" + reqBody);
        }
        
        return chain.filter(exchange);
    }
}
