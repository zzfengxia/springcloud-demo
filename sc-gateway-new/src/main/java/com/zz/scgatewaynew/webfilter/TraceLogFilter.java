package com.zz.scgatewaynew.webfilter;

import brave.internal.Platform;
import brave.internal.codec.HexCodec;
import com.alibaba.fastjson.JSONObject;
import com.zz.gateway.common.GatewayConstants;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.UuidUtils;
import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-10-23 16:47
 * ************************************
 */
@Component
@Slf4j
public class TraceLogFilter implements WebFilter, Ordered {
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies
            .withDefaults().messageReaders();
    
    
    @Override
    public int getOrder() {
        // 必须要在在 TraceWebFilter 之前执行
        return Integer.MIN_VALUE;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(BizConstants.GATEWAY_START_TIME, startTime);
    
        // spanid，单次请求的追踪；traceid，多次交互的追踪（客户端sessionID）
        String spanid = HexCodec.toLowerHex(nextId());
        
        Mono<ServerWebExchange> exchangeWithTrace = null;
        // post请求，读取请求body
        if (HttpMethod.POST.matches(exchange.getRequest().getMethodValue())) {
            exchangeWithTrace = ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                    (serverHttpRequest) -> ServerRequest
                            .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders)
                            .bodyToMono(String.class)
                            .map(reqBody -> {
                                String traceId = traceId(exchange.getRequest().getHeaders().getContentType(), reqBody);
                                ServerHttpRequest.Builder modifyRequestBuilder = exchange.getRequest().mutate()
                                        // SpanId必须是16位Hex字符串，必须小写
                                        .header(GatewayConstants.SPAN_ID_NAME, spanid)
                                        // ParentSpanId可以不存在
                                        //.header("X-B3-ParentSpanId", "")
                                        // TraceId必须为16位或32位 Hex字符串，必须小写
                                        .header(GatewayConstants.TRACE_ID_NAME, traceId);
                            
                                exchange.getAttributes().put(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY, reqBody);
                                exchange.getAttributes().put(BizConstants.MDC_TRACE_ID, traceId);
                                return exchange.mutate().request(modifyRequestBuilder.build()).build();
                            })
                            .doOnError(error -> log.warn("read and parse request body error", error)));
        } else {
            // 追踪id必须为小写
            String traceId = UuidUtils.generateUuid(UuidUtils.CaseType.LOWER_CASE);
            ServerHttpRequest.Builder modifyRequestBuilder = exchange.getRequest().mutate()
                    .header(GatewayConstants.SPAN_ID_NAME, spanid)
                    .header(GatewayConstants.TRACE_ID_NAME, traceId);
        
            exchange.getAttributes().put(BizConstants.MDC_TRACE_ID, traceId);
            exchangeWithTrace = Mono.just(exchange.mutate().request(modifyRequestBuilder.build()).build());
        }
        
        // 继续过滤链
        return exchangeWithTrace
                .flatMap(chain::filter)
                .then(Mono.defer(() -> {
                    Long startExecTime = exchange.getAttribute(BizConstants.GATEWAY_START_TIME);
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeid = route != null ? route.getId() : "";
                    if (startExecTime != null) {
                        long end = System.currentTimeMillis();
        
                        log.info("gateway for routeid [" + routeid + "] total execute time [" + (end - startExecTime) + "] ms");
                    }
                    return Mono.empty();
                }));
                
    }
    
    long nextId() {
        long nextId = Platform.get().randomLong();
        while (nextId == 0L) {
            nextId = Platform.get().randomLong();
        }
        return nextId;
    }
    
    /**
     * 这里可以通过mediaType 扩展，获取不同格式的transactionid。sleuth的traceid必须要小写
     */
    private String traceId(MediaType mediaType, String requestJson) {
        String traceId = null;
        try {
            // json格式
            if(GatewayUtils.isJson(mediaType)) {
                JSONObject jsonObject = JSONObject.parseObject(requestJson);
                traceId = jsonObject.getString("transactionid");
            }
        } catch (Exception e) {
            log.error("extract transactionid from request body error.request data:" + requestJson, e);
        }
        
        if(StringUtils.isEmpty(traceId)) {
            traceId = UuidUtils.generateUuid(UuidUtils.CaseType.LOWER_CASE);
        }
        
        return traceId.toLowerCase();
    }
}
