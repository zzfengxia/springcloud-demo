package com.zz.scgateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.zz.scgateway.respdefine.IFailResponse;
import com.zz.scgateway.respdefine.ResponseFactoryService;
import com.zz.scgateway.util.GatewayUtils;
import com.zz.scgateway.util.IPAddrUtils;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.LogUtils;
import com.zz.sccommon.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
    @Autowired
    private ReadBodyPredicateFactory readBodyPredicateFactory;
    @Autowired
    private ResponseFactoryService responseFactoryService;
    /**
     * 校验请求信息，注入日志id,限流标识到请求头
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
        // 全流程多次交互的事务ID,优先从请求体中获取
        String traceId = null;
        // 流控标识
        String flowCtrlFlag = "false";
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        /**
         * 获取body的方法参考{@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory}
         * 或者{@link ReadBodyPredicateFactory}
         * 这里默认已经调用过readBody的断言，直接从缓存中获取body
         */
        Object cachedBody = GatewayUtils.fetchBody(readBodyPredicateFactory, exchange);
        boolean isJson = GatewayUtils.isJson(exchange.getRequest().getHeaders().getContentType());
        String command = null;
        if(cachedBody != null) {
            if((cachedBody instanceof String) && isJson) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject((String) cachedBody);
                    traceId = jsonObject.getString("transactionid");
                    command = jsonObject.getString("command");
                    if(StringUtils.isEmpty(traceId)) {
                        flowCtrlFlag = "true";
                        traceId = UuidUtils.generateUuid();
                    }
                }catch (Exception e) {
                    flowCtrlFlag = "true";
                    traceId = UuidUtils.generateUuid();
                    log.info("parse requestJson fail.", e);
                } finally {
                    // save trace id to log session
                    LogUtils.saveSessionIdForLog(traceId);
                }
            } else {
                traceId = UuidUtils.generateUuid();
                flowCtrlFlag = "true";
                // save trace id to log session
                LogUtils.saveSessionIdForLog(traceId);
                log.warn("request body is not string");
            }
    
            log.info("request data:" + cachedBody);
        } else {
            traceId = UuidUtils.generateUuid();
            // save trace id to log session
            LogUtils.saveSessionIdForLog(traceId);
            
            flowCtrlFlag = "true";
        }
        
        String ipstr = IPAddrUtils.getClientIp(request);
        log.info("client ip:" + ipstr + ", " + GatewayUtils.formatRequest(request));
        log.info("request headers:" + request.getHeaders().toString());
        
        exchange.getAttributes().put(BizConstants.MDC_TRACE_ID, traceId);
        exchange.getAttributes().put(BizConstants.REQUEST_START_TIME, startTime);
        
        /**
         * 使用exchange.getRequest().getHeaders()获取到的Headers不支持新增操作
         * 参考 {@link org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory}
         */
        ServerHttpRequest.Builder modifyRequestBuilder = request.mutate()
                .header(BizConstants.HEADER_TRACE_ID, traceId)
                .header(BizConstants.FLOW_CTRL_FLAG, flowCtrlFlag);
        
        if(StringUtils.isNotEmpty(command)) {
            modifyRequestBuilder.header(BizConstants.COMMAND_ID, command);
        }
    
        // 删除MDC缓存
        LogUtils.clearSessionForLog();
    
        return wrapResponseFilter().filter(exchange.mutate().request(modifyRequestBuilder.build()).build(), chain).then(Mono.fromRunnable(() -> {
            // post 执行完filter之后执行这里的操作，注意这里跟filter执行的线程是不一样的
            
        }));
    }
    
    private GatewayFilter wrapResponseFilter() {
        return modifyResponseBodyGatewayFilterFactory.apply((c -> c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {
            /**
             * 这里的modifyResponseBodyFilter的执行线程也有可能前面执行的filter线程不是同一个。所以traceId要从serverWebExchange缓存中取值
             */
            String uid = GatewayUtils.getTraceIdFromCache(serverWebExchange);
            LogUtils.saveSessionIdForLog(uid);
            Long startExecTime = serverWebExchange.getAttribute(BizConstants.REQUEST_START_TIME);
            
            log.info("response body:" + body);
            log.info("response header:" + serverWebExchange.getResponse().getHeaders().toString());
        
            HttpStatus responseStatus = serverWebExchange.getResponse().getStatusCode();
            if(responseStatus != null && responseStatus.value() != HttpStatus.OK.value()) {
                // 后台服务响应不是正常的200状态， 这里只记录异常信息，给客户端响应正常状态码，使用json格式的信息标识错误信息
                log.info("服务端响应http status:{}, name:{}, reason:{}", responseStatus.value(), responseStatus.name(), responseStatus.getReasonPhrase());
                serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                
                // 保存日志到DB
                Object cachedBody = GatewayUtils.fetchBody(readBodyPredicateFactory, serverWebExchange);
                
                IFailResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(serverWebExchange, "服务器开小差啦", null);
                body = failResponseInfo.getMsg();
    
                serverWebExchange.getResponse().setRawStatusCode(failResponseInfo.getCode());
            }
            
            if(startExecTime != null) {
                long end = System.currentTimeMillis();
                log.info("request execute time [" + (end - startExecTime) + "] ms");
            }
            // 删除MDC缓存
            LogUtils.clearSessionForLog();
        
            return Mono.just(body);
        })));
    }
    
    /**
     * 设置执行顺序，值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        // 在 SentinelGatewayFilter 之前执行
        return -2;
    }
}
