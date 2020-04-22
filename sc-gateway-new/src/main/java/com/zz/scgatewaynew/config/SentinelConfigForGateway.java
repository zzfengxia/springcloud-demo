package com.zz.scgatewaynew.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.sccommon.util.LogUtils;
import com.zz.scgatewaynew.respdefine.IFailResponse;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 * sentinel网关限流配置
 * 单机本地限流
 *
 * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}
 *
 * @author Francis.zz
 * @date 2020-03-18 11:52
 * ************************************
 */
@Configuration
@Slf4j
public class SentinelConfigForGateway {
    @Autowired
    private ResponseFactoryService responseFactoryService;
    /**
     * 注入限流异常处理
     * 可定制限流响应信息，默认为{@link com.alibaba.csp.sentinel.adapter.gateway.sc.callback.DefaultBlockRequestHandler}
     * 使用spring-cloud-alibaba-sentinel-gateway 集成需要创建SPI文件，指定GatewaySlotChainBuilder才能使限流生效
     *
     * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}#blockRequestHandlerOptional
     */
    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange,
                                                      Throwable t) {
                String uid = GatewayUtils.getTraceIdFromCache(exchange);
                LogUtils.saveSessionIdForLog(uid);
                log.info("请求已被限流");
    
                IFailResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(exchange, ErrorCode.TOO_MANY_REQUESTS.getReturnMsg(), ErrorCode.TOO_MANY_REQUESTS.getErrorCode());
    
                // JSON result by default.
                return ServerResponse.status(failResponseInfo.getCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(failResponseInfo.getMsg()));
            }
        };
    }
}
