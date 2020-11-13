package com.zz.scgatewaynew.gatewayfilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-09-22 14:32
 * ************************************
 */
@Component
@Slf4j
public class CheckResponseStatusFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.defer(() -> {
                    log.debug("-- [CheckResponseStatusFilter] record exec time");
                    HttpStatus responseStatus = exchange.getResponse().getStatusCode();
                    if(responseStatus != null && responseStatus.value() != HttpStatus.OK.value()) {
                        return Mono.error(new ResponseStatusException(responseStatus));
                    }
                    return Mono.empty();
                }));
    }
    
    @Override
    public int getOrder() {
        // 必须在 NettyWriteResponseFilter 过滤器之后执行， 这样then方法会比NettyWriteResponseFilter的then方法之前执行，即在response.state状态未被修改之前执行
        return 10;
    }
}
