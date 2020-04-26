package com.zz.scgatewaynew.handler;

import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * ************************************
 * create by Intellij IDEA
 * 仅获取request body for POST.不做断言操作
 * @author Francis.zz
 * @date 2020-04-26 16:22
 * ************************************
 */
@Slf4j
public class CustomeReadBodyPredicateFactory extends AbstractRoutePredicateFactory<CustomeReadBodyPredicateFactory.Config> {
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies
            .withDefaults().messageReaders();
    
    public CustomeReadBodyPredicateFactory() {
        super(CustomeReadBodyPredicateFactory.Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public AsyncPredicate<ServerWebExchange> applyAsync(CustomeReadBodyPredicateFactory.Config config) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange exchange) {
                if(!HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
                    return Mono.just(true);
                }
                
                Object cachedBody = exchange.getAttribute(GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY);
                if (cachedBody == null) {
                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                            (serverHttpRequest) -> ServerRequest
                                    .create(exchange.mutate().request(serverHttpRequest)
                                            .build(), messageReaders)
                                    .bodyToMono(String.class)
                                    .doOnNext(objectValue -> exchange.getAttributes().put(
                                            GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
                                    .map(objectValue -> true));
                }
                return Mono.just(true);
            }
        };
    }
    
    @Validated
    public static class Config {
    
    }
}
