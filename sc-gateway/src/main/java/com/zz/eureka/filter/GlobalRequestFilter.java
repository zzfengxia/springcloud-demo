package com.zz.eureka.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
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
public class GlobalRequestFilter implements GlobalFilter, Ordered {
    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;
    
    /**
     * 与该值保持一致{@link org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory#CACHE_REQUEST_BODY_OBJECT_KEY}
     */
    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    /**
     * 全局过滤器，在断言之后，特定路由过滤器之前执行
     * 过滤器只有pre和post两个生命周期，即请求前后响应后
     * 在 调用chain.filter 之前的操作是pre, 在then里面的操作是post
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 全流程多次交互的事务ID
        String traceId = null;
        // 每次请求的ID
        String reqId = generateUuid();
        /**
         * 获取body的方法参考{@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory}
         * 或者{@link org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory}
         * 这里默认已经调用过readBody的断言，直接从缓存中获取body
         */
        Object cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
        if(cachedBody != null) {
            log.info("request json:{}", JSON.toJSONString(cachedBody));
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
            traceId = generateUuid();
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
            modifyResponseBodyGatewayFilterFactory.apply((c -> c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {
                HttpStatus responseStatus = serverWebExchange.getResponse().getStatusCode();
                if(responseStatus != null && responseStatus.value() != 200) {
                    // 后台服务响应不是正常的200状态， 这里只记录异常信息，给客户端响应正常状态码，使用json格式的信息标识错误信息
                    log.info("服务端响应http status:{}, name:{}, reason:{}", responseStatus.value(), responseStatus.name(), responseStatus.getReasonPhrase());
                    serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
            
                    Map<String, Object> errorAttributes = new HashMap<>();
                    errorAttributes.put("returnDesc", "服务器异常");
                    // returnCode 可以转换为自定义的code
                    errorAttributes.put("returnCode", "1001");
                    errorAttributes.put("transactionid", "1234567890");
                    errorAttributes.put("signType", null);
                    errorAttributes.put("sign", null);
            
                    body = JSON.toJSONString(errorAttributes);
                }
                System.out.println("response header:" + serverWebExchange.getResponse().getHeaders());
                return Mono.just(body);
            })));
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
        return -2;
    }
    
    public static String generateUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }
}
