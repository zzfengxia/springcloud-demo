package com.zz.eureka;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务注册中心
 */
@SpringBootApplication
@RestController
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    /**
     * RouteLocator数据会注入到 GatewayAutoConfiguration.cachedCompositeRouteLocator
     * 路由映射器由GatewayAutoConfiguration.routePredicateHandlerMapping 注入
     * 其中的RouteLocator由 GatewayAutoConfiguration.cachedCompositeRouteLocator 注入，所有每次请求都会调用
     * {@link org.springframework.cloud.gateway.route.CachingRouteLocator#getRoutes()}方法，并且该类在初始化时会将下面的myRoutes配置注入
     * 合并 RouteDefinitionRepository 实现类获取到的RouteDefinition信息
     *
     * {@link org.springframework.cloud.gateway.route.CachingRouteLocator} 将路由信息缓存到本地内存，
     * 通过监听{@link org.springframework.cloud.gateway.event.RefreshRoutesEvent}事件更新路由缓存信息。
     *
     * 实现 {@link org.springframework.cloud.gateway.route.RouteDefinitionRepository}类自定义动态路由存储，
     * 但只有在项目初始化时获取动态路由信息，最终还是由CachingRouteLocator存储在本地缓存？
     * 动态路由实现参考{@link com.zz.eureka.service.DynamicGatewayService}
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        String httpUri = "http://baidu.com:80";
        return builder.routes()
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(httpUri))
                .route(p -> p
                        .path("/hystrix")
                        .filters(f -> f
                                .hystrix(config -> config
                                        .setName("mycmd")
                                        .setFallbackUri("forward:/fallback"))
                                        .retry(2))  // 默认只支持5XX错误并且是GET请求的场景
                        .uri("http://localhost:8081"))
                .route(p -> p
                        // 获取psot请求体
                        .readBody(Object.class, body -> {
                            log.info("request json:{}", JSON.toJSONString(body));
                            return false;
                        })
                        .and()
                        .method(HttpMethod.POST)
                        .uri(httpUri)
                        .order(-100)
                )
                .route(p -> p
                        .readBody(Object.class, b -> true)
                        .uri("https://sina.cn/")
                        .order(100)
                )
                .build();
    }

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
}
