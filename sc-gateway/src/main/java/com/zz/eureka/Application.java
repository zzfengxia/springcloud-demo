package com.zz.eureka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.CompositeRouteLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

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
     * 加载解析路由并保存到内存的执行步骤：
     * 1. 调用{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#cachedCompositeRouteLocator}初始化{@link CachingRouteLocator}，
     * 参数为{@link RouteLocator}接口的实现类集合。比如{@link com.zz.eureka.Application#myRoutes}定义的实现
     * 和{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionRouteLocator}注入的实现
     *
     * 2. 调用{@link CachingRouteLocator#fetch}方法，并调用{@link CompositeRouteLocator#getRoutes()}方法。该方法会遍历步骤1中RouteLocator的实现类并分别调用其getRoutes方法，
     * 然后将返回的结果合并，即{@link Route}集合
     *
     * 3. 步骤2调用{@link RouteDefinitionRouteLocator#getRoutes()}方法时，routeDefinitionLocator 的实现类是在
     * {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionLocator}中注入的，
     * 而其中的参数 List<RouteDefinitionLocator> routeDefinitionLocators 就是{@link RouteDefinitionLocator}的实现类集合(需要注入到spring),
     * 比如 {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#propertiesRouteDefinitionLocator}(接收属性文件配置的路由信息)
     * 以及 inMemoryRouteDefinitionRepository 的覆盖类，我们自定义的动态路由实现 {@link CustomRouteDefinitionRepository}从redis或者DB中获取配置路由信息。
     * 这里就会分别调用实现类的 {@link RouteDefinitionLocator#getRouteDefinitions}，然后再将结果合并。
     *
     * 4. 将步骤2合并的结果保存在本地缓存中
     *
     * 实现 {@link org.springframework.cloud.gateway.route.RouteDefinitionRepository}类自定义动态路由存储，
     * 但只有在项目初始化时获取动态路由信息，最终还是由CachingRouteLocator存储在本地缓存？
     * 动态路由实现参考{@link com.zz.eureka.service.DynamicGatewayService}
     *
     * @param builder
     * @return
     */
    //@Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        String httpUri = "http://baidu.com:80";
        return builder.routes()
                .route("route-demo-2", p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(httpUri))
                /*.route(p -> p
                        .path("/hystrix")
                        .filters(f -> f
                                .hystrix(config -> config
                                        .setName("mycmd")
                                        .setFallbackUri("forward:/fallback"))
                                        .retry(2))  // 默认只支持5XX错误并且是GET请求的场景
                        .uri("http://localhost:8081"))*/
                /*.route(p -> p
                        // 获取psot请求体
                        .readBody(Object.class, body -> {
                            log.info("request json:{}", JSON.toJSONString(body));
                            return false;
                        })
                        .and()
                        .method(HttpMethod.POST)
                        .uri(httpUri)
                        .order(-100)
                )*/
                .route("route-demo-1", p -> p
                        .path("/testFlowRule")
                        .and()
                        .method(HttpMethod.POST)
                        .and()
                        .readBody(String.class, body -> {
                            // 不能读多次
                            log.info("request json:{}", JSON.toJSONString(body));
                            JSONObject jsonObject = JSONObject.parseObject(body);
                            String issureId;
                            if ((issureId = jsonObject.getString("issuerid")) != null && "test".equalsIgnoreCase(issureId)) {
                                return true;
                            } else {
                                return false;
                            }
                        })
                        .filters(filterSpec -> {
                            return filterSpec
                                    .setPath("/mq/postDemo")
                                    .modifyRequestBody(String.class, String.class, ((serverWebExchange, s) -> {
                                        // modify request body, add traceID
                                        JSONObject jsonObject = JSONObject.parseObject(s);
                                        String traceId = getTraceId(serverWebExchange);
                                        log.info("[{}] modify request body...", traceId);
                                        
                                        if(StringUtils.isEmpty(jsonObject.getString("transactionid"))) {
                                            jsonObject.put("transactionid", traceId);
                                        }
                                        return Mono.just(jsonObject.toJSONString());
                                    }));
                        })
                        // uri中不能包含path
                        .uri(URI.create("http://localhost:8083/"))
                        .order(-100)
                )
                /*.route(p -> p
                        .readBody(Object.class, b -> true)
                        .uri("https://sina.cn/")
                        .order(100)
                )*/
                .build();
    }

    /*@RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }*/
    
    private String getTraceId(ServerWebExchange serverWebExchange) {
        String traceId = null;
        if(serverWebExchange != null) {
            // 请求头key不区分大小写
            traceId = serverWebExchange.getRequest().getHeaders().getFirst("traceId");
        }
        
        return StringUtils.isEmpty(traceId) ? UUID.randomUUID().toString().replaceAll("-", "").toUpperCase() : traceId;
    }
}
