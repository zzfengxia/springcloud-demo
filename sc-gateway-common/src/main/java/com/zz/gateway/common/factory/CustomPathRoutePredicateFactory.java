package com.zz.gateway.common.factory;

import com.zz.gateway.common.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.PathContainer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.putUriTemplateVariables;
import static org.springframework.http.server.PathContainer.parsePath;

/**
 * ************************************
 * create by Intellij IDEA
 * 自定义PathRoutePredicateFactory，加入定制功能
 *
 * @see {@link org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory}
 * @author Francis.zz
 * @date 2020-04-17 18:17
 * ************************************
 */
@Slf4j
public class CustomPathRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomPathRoutePredicateFactory.Config> {
    
    private static final String MATCH_OPTIONAL_TRAILING_SEPARATOR_KEY = "matchOptionalTrailingSeparator";
    private PathPatternParser pathPatternParser = new PathPatternParser();
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies
            .withDefaults().messageReaders();
    
    public CustomPathRoutePredicateFactory() {
        super(Config.class);
    }
    
    private static void traceMatch(String prefix, Object desired, Object actual,
                                   boolean match) {
        if (log.isTraceEnabled()) {
            String message = String.format("%s \"%s\" %s against value \"%s\"", prefix,
                    desired, match ? "matches" : "does not match", actual);
            log.trace(message);
        }
    }
    
    public void setPathPatternParser(PathPatternParser pathPatternParser) {
        this.pathPatternParser = pathPatternParser;
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("patterns", MATCH_OPTIONAL_TRAILING_SEPARATOR_KEY);
    }
    
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST_TAIL_FLAG;
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        final ArrayList<PathPattern> pathPatterns = new ArrayList<>();
        synchronized (this.pathPatternParser) {
            pathPatternParser.setMatchOptionalTrailingSeparator(
                    config.isMatchOptionalTrailingSeparator());
            config.getPatterns().forEach(pattern -> {
                PathPattern pathPattern = this.pathPatternParser.parse(pattern);
                pathPatterns.add(pathPattern);
            });
        }
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                PathContainer path = parsePath(
                        exchange.getRequest().getURI().getRawPath());
                
                Optional<PathPattern> optionalPathPattern = pathPatterns.stream()
                        .filter(pattern -> pattern.matches(path)).findFirst();
                // 获取请求body
                /*Object cachedBody = exchange.getAttribute(GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY);
                if(cachedBody == null) {
                    ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                            (serverHttpRequest) -> {
                        return ServerRequest
                                    .create(exchange.mutate().request(serverHttpRequest)
                                            .build(), messageReaders)
                                    .bodyToMono(String.class)
                                    .doOnNext(objectValue -> {
                                        exchange.getAttributes().put(GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY, objectValue);
                                    });});
                }*/
                
                if (optionalPathPattern.isPresent()) {
                    PathPattern pathPattern = optionalPathPattern.get();
                    traceMatch("Pattern", pathPattern.getPatternString(), path, true);
                    PathPattern.PathMatchInfo pathMatchInfo = pathPattern.matchAndExtract(path);
                    putUriTemplateVariables(exchange, pathMatchInfo.getUriVariables());
                    
                    // 保存异常响应策略
                    exchange.getAttributes().put(GatewayConstants.FAIL_RESPONSE_STRATEGY, config.getRespStrategy());
                    return true;
                }
                else {
                    traceMatch("Pattern", config.getPatterns(), path, false);
                    return false;
                }
            }
            
            @Override
            public String toString() {
                return String.format("Paths: %s, match trailing slash: %b",
                        config.getPatterns(), config.isMatchOptionalTrailingSeparator());
            }
        };
    }
    
    /*@Override
    @SuppressWarnings("unchecked")
    public AsyncPredicate<ServerWebExchange> applyAsync(CustomPathRoutePredicateFactory.Config config) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange exchange) {
                Object cachedBody = exchange.getAttribute(GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY);
                log.info("custom predicate applyAsync exec...");
                if (cachedBody == null) {
                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                            (serverHttpRequest) -> ServerRequest
                                    .create(exchange.mutate().request(serverHttpRequest)
                                            .build(), messageReaders)
                                    .bodyToMono(String.class)
                                    .doOnNext(objectValue -> exchange.getAttributes().put(
                                            GatewayUtils.CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
                                    .map(objectValue -> true));
                } else {
                    return Mono.just(true);
                }
            }
        };
    }*/
    
    @Validated
    public static class Config {
        
        private List<String> patterns = new ArrayList<>();
        /**
         * 配置网关路由异常时的响应策略,跟path绑定
         */
        private int respStrategy = 0;
        /**
         * 匹配尾部分隔符
         */
        private boolean matchOptionalTrailingSeparator = true;
        
        public List<String> getPatterns() {
            return patterns;
        }
        
        public Config setPatterns(List<String> patterns) {
            this.patterns = patterns;
            return this;
        }
        
        public boolean isMatchOptionalTrailingSeparator() {
            return matchOptionalTrailingSeparator;
        }
    
        public int getRespStrategy() {
            return respStrategy;
        }
    
        public void setRespStrategy(int respStrategy) {
            this.respStrategy = respStrategy;
        }
    
        public Config setMatchOptionalTrailingSeparator(
                boolean matchOptionalTrailingSeparator) {
            this.matchOptionalTrailingSeparator = matchOptionalTrailingSeparator;
            return this;
        }
        
        @Override
        public String toString() {
            return new ToStringCreator(this).append("patterns", patterns)
                    .append("matchOptionalTrailingSeparator",
                            matchOptionalTrailingSeparator)
                    .toString();
        }
        
    }
}
