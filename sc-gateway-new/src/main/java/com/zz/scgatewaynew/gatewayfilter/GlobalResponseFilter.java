package com.zz.scgatewaynew.gatewayfilter;

import com.zz.gateway.common.GatewayConstants;
import com.zz.sccommon.constant.BizConstants;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import com.zz.scgatewaynew.respdefine.UpstreamResponse;
import com.zz.scgatewaynew.util.GatewayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-09-17 14:15
 * ************************************
 */
@Component
public class GlobalResponseFilter implements GlobalFilter, Ordered {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;
    @Autowired
    private ReadBodyPredicateFactory readBodyPredicateFactory;
    @Autowired
    private ResponseFactoryService responseFactoryService;
    
    /**
     * {@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory.ModifyResponseGatewayFilter}
     * 调用ModifyResponseGatewayFilter.filter，writeWith方法回调
     * 在{@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}的then方法调用后回调writeWith.
     * 因此如果要在这里调用之后再处理某个操作，只需要创建比 NettyWriteResponseFilter 优先级高的过滤器然后调用then方法即可。then方法与过滤器优先级成反比
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 这里是在 ModifyResponseGatewayFilter过滤器调用chain.filter继续调用链
        return modifyResponseBodyGatewayFilterFactory.apply((c -> c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {

            Long startExecTime = serverWebExchange.getAttribute(BizConstants.REQUEST_START_TIME);
    
            log.info("response body:" + body);
            log.info("response header:" + serverWebExchange.getResponse().getHeaders().toString());
    
            // 缓存responseBody
            serverWebExchange.getAttributes().put(GatewayConstants.CACHE_RESPONSE_BODY, body);
            
            HttpStatus responseStatus = serverWebExchange.getResponse().getStatusCode();
            if(responseStatus != null && responseStatus.value() != HttpStatus.OK.value()) {
                // 后台服务响应不是正常的200状态， 这里只记录异常信息，给客户端响应正常状态码，使用json格式的信息标识错误信息
                log.info("服务端响应http status:{}, name:{}, reason:{}", responseStatus.value(), responseStatus.name(), responseStatus.getReasonPhrase());
                serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
                // 保存日志到DB
                Object cachedBody = GatewayUtils.fetchBody(readBodyPredicateFactory, serverWebExchange);
        
                UpstreamResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(serverWebExchange, "服务器开小差啦", null);
                body = failResponseInfo.getMsg();
        
                serverWebExchange.getResponse().setRawStatusCode(failResponseInfo.getCode());
            }
    
            if(startExecTime != null) {
                long end = System.currentTimeMillis();
                log.info("request execute time [" + (end - startExecTime) + "] ms");
            }
            return Mono.just(body);
        }))).filter(exchange, chain);
    }
    
    @Override
    public int getOrder() {
        return -2;
    }
}
