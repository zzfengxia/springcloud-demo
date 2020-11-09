package com.zz.scgatewaynew.handler;

import com.alibaba.csp.sentinel.slots.block.UpstreamRespException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-09-24 17:16
 * ************************************
 */
public class UpstreamResponseFailHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        if(throwable instanceof UpstreamRespException) {
            // 拦截上游服务响应体非成功的请求异常，不做任何处理。只用来触发sentinel的onError
            return Mono.empty();
        }
        return Mono.error(throwable);
    }
}
