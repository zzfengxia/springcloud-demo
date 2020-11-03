package com.zz.scgatewaynew.gatewayfilter;

import com.alibaba.csp.sentinel.slots.block.UpstreamRespException;
import com.zz.gateway.common.GatewayConstants;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-09-24 16:43
 * ************************************
 */
@Component
public class CheckUpstreamResponseFilter implements GlobalFilter, Ordered {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private ResponseFactoryService responseFactoryService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.defer(() -> {
                    HttpStatus responseStatus = exchange.getResponse().getStatusCode();
                    if(responseStatus == null || responseStatus.value() != HttpStatus.OK.value()) {
                        // 只处理上游服务成功响应了的请求
                        return Mono.empty();
                    }
                    Object respBody = exchange.getAttribute(GatewayConstants.CACHE_RESPONSE_BODY);
                    if(ObjectUtils.isEmpty(respBody)) {
                        return Mono.empty();
                    }
                    try {
                        boolean isFailResp = responseFactoryService.isFailResponse(exchange, (String) respBody);
                        if(isFailResp) {
                            return Mono.error(new UpstreamRespException());
                        }
                    } catch (Exception e) {
                        LOG.warn("[CheckUpstreamResponseFilter] parse response body failed.", e);
                        return Mono.empty();
                    }
                    return Mono.empty();
                }));
    }
    
    @Override
    public int getOrder() {
        // 在sentinel filter之后
        return -3;//Integer.MIN_VALUE;
    }
}
