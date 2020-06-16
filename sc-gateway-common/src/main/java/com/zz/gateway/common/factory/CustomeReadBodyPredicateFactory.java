package com.zz.gateway.common.factory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static final String TEST_ATTRIBUTE = "read_body_predicate_test_attribute";
    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    public CustomeReadBodyPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public AsyncPredicate<ServerWebExchange> applyAsync(Config config) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange exchange) {
                if(!HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
                    return Mono.just(true);
                }
                Class inClass = config.getInClass();
                Object cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
                Mono<?> modifiedBody;
                if (cachedBody != null) {
                    try {
                        boolean test = config.predicate.test(cachedBody);
                        exchange.getAttributes().put(TEST_ATTRIBUTE, test);
                        return Mono.just(test);
                    }
                    catch (ClassCastException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Predicate test failed because class in predicate "
                                    + "does not match the cached body object", e);
                        }
                    }
                    return Mono.just(false);
                } else {
                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                            (serverHttpRequest) -> ServerRequest
                                    .create(exchange.mutate().request(serverHttpRequest)
                                            .build(), messageReaders)
                                    .bodyToMono(inClass)
                                    .doOnNext(objectValue -> exchange.getAttributes().put(
                                            CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
                                    .map(objectValue -> config.getPredicate()
                                            .test(objectValue)));
                }
            }
    
            /**
             * toString方法在打印route信息时会用到
             *
             * @return
             */
            @Override
            public String toString() {
                if(ObjectUtils.isEmpty(config.getAttrMap())) {
                    return "CustomReadBody: only cache post request body";
                }
                Map<String, List<String>> attrMap = Collections.unmodifiableMap(config.getAttrMap());
                List<String> descList = new ArrayList<>(attrMap.size() + 1);
                
                attrMap.forEach((k, v) -> {
                    String desc = String.format("[%s pattern %s]", k, v.toString());
                    descList.add(desc);
                });
                descList.add(String.format("strategy = %s", config.getStrategy()));
                
                return "CustomReadBody: " + descList.toString();
            }
        };
    }
    
    @Validated
    public static class Config {
        private Class inClass;
    
        private Predicate predicate;
    
        private Map<String, List<String>> attrMap;
    
        private String strategy;
    
        public Predicate getPredicate() {
            return predicate;
        }
    
        public Class getInClass() {
            return inClass;
        }
    
        public String getStrategy() {
            return strategy;
        }
    
        public Config setStrategy(String strategy) {
            this.strategy = strategy;
            return this;
        }
    
        public Config setInClass(Class inClass) {
            this.inClass = inClass;
            return this;
        }
        
        public Config setPredicate(Predicate predicate) {
            this.predicate = predicate;
            return this;
        }
    
        public <T> Config setPredicate(Class<T> inClass, Predicate<T> predicate) {
            setInClass(inClass);
            this.predicate = predicate;
            return this;
        }
    
        public Map<String, List<String>> getAttrMap() {
            return attrMap;
        }
    
        public Config setAttrMap(Map<String, List<String>> attrMap) {
            this.attrMap = attrMap;
            return this;
        }
    }
}
