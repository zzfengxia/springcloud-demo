package com.zz.eureka.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-17 16:32
 * ************************************
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    /**
     * 与该值保持一致{@link org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory#CACHE_REQUEST_BODY_OBJECT_KEY}
     */
    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    /**
     * 全局过滤器，在断言之后，特定路由过滤器之前执行
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // chain.filter(exchange)为执行路由转发
        // pre执行
        // 全流程多次交互的事务ID
        String traceId = null;
        // 每次请求的ID
        String reqId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        log.info("custom global filter exec [pre]...");
        /**
         * 获取body的方法参考{@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory}
         * 或者{@link org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory}
         * 这里默认已经调用过readBody的断言，直接从缓存中获取body
         */
        Object cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
        if(cachedBody != null) {
            if(cachedBody instanceof String) {
                JSONObject jsonObject = JSONObject.parseObject((String) cachedBody);
                traceId = jsonObject.getString("transactionid");
                log.info("cache origin request body is:{}", cachedBody);
            } else {
                log.warn("cache origin request body is not string");
            }
        }
        // 流控标识
        String flowCtrlFlag = "false";
        if(StringUtils.isEmpty(traceId)) {
            flowCtrlFlag = "true";
            traceId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        }
        /**
         * 使用exchange.getRequest().getHeaders()获取到的Headers不支持新增操作
         * 参考 {@link org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory}
         */
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("traceId", traceId)
                .header("reqId", reqId)
                .header("flowCtrlFlag", flowCtrlFlag)
                .build();
        log.info("[{}] custom global filter exec [post]...", traceId);
        return chain.filter(exchange.mutate().request(request).build()).then(Mono.fromRunnable(() -> {
            // post执行
            
        }));
    }
    
    /**
     * 设置执行顺序，值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        // 在SentinelGatewayFilter之前
        return -100;
    }
}
