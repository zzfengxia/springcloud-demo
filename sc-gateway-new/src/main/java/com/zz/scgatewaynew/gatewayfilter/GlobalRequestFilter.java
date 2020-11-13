package com.zz.scgatewaynew.gatewayfilter;

import com.alibaba.fastjson.JSONObject;
import com.zz.gateway.common.GatewayConstants;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.UuidUtils;
import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

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
    private ReadBodyPredicateFactory readBodyPredicateFactory;
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
        // 流控标识，通过请求body中是否有transactionid判断是否需要合并请求次数
        String flowCtrlFlag = "true";
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if(route != null) {
            log.info(String.format("匹配到的路由信息：{id:%s, routeUrl:%s}", route.getId(), route.getUri()));
        }
        /**
         * 获取body的方法参考{@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory}
         * 或者{@link ReadBodyPredicateFactory}
         * 这里默认已经调用过readBody的断言，直接从缓存中获取body
         */
        Object cachedBody = GatewayUtils.fetchBody(readBodyPredicateFactory, exchange);
        boolean isJson = GatewayUtils.isJson(request.getHeaders().getContentType());
        String command = null;
        String transactionid = null;
        if(cachedBody != null) {
            if((cachedBody instanceof String) && isJson) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject((String) cachedBody);
                    command = jsonObject.getString("command");
                    if(StringUtils.isNotEmpty(jsonObject.getString("transactionid"))) {
                        transactionid = jsonObject.getString("transactionid");
                        // 请求body中没有transactionid参数则判断为一次独立的请求
                        flowCtrlFlag = "false";
                    }
                }catch (Exception e) {
                    log.info("parse requestJson fail.", e);
                }
            }
        }
        
        exchange.getAttributes().put(BizConstants.REQUEST_START_TIME, startTime);
        // 将网关的日志追踪ID作为事务ID放入请求头传递到上游服务，上游服务优先会从请求体中获取事务ID，没有则取请求头的事务id
        String spSessionId = request.getHeaders().getFirst(GatewayConstants.TRACE_ID_NAME);
        spSessionId = StringUtils.isEmpty(spSessionId) ? StringUtils.isEmpty(transactionid) ? UuidUtils.generateUuid() : transactionid : spSessionId;
        /**
         * 使用exchange.getRequest().getHeaders()获取到的Headers不支持新增操作
         * 参考 {@link org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory}
         */
        ServerHttpRequest.Builder modifyRequestBuilder = request.mutate()
                .header(BizConstants.HEADER_TRACE_ID, spSessionId.toUpperCase())
                .header(BizConstants.FLOW_CTRL_FLAG, flowCtrlFlag);
        
        if(StringUtils.isNotEmpty(command)) {
            modifyRequestBuilder.header(BizConstants.COMMAND_ID, command);
        }
    
        // 使用chain.filter继续Filter调用链
        return chain.filter(exchange.mutate().request(modifyRequestBuilder.build()).build())
                .then(Mono.defer(() ->
                {
                    // then是在调用链中所有的Filter都执行完之后再执行的，所以这里也能获取到路由服务的响应信息
                    // 最后执行的filter的then方法执行优先级越高（比较其他filter的then）
                    log.info("-- record gateway response datestamp");
                    return Mono.empty();
                }));
    }
    
    /**
     * 设置执行顺序，值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        // 在 SentinelGatewayFilter 之前执行
        return -30;
    }
}
