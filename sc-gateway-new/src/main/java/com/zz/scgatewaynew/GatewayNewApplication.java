package com.zz.scgatewaynew;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zz.scgatewaynew.service.DynamicGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.CompositeRouteLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务注册中心
 */
@SpringBootApplication
@RestController
@EnableDiscoveryClient
@Slf4j
public class GatewayNewApplication {
    /**
     * <h1>sentinel动态规则</h1>
     * 参考{@link com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource} nacos数据源属性
     * {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler} 注册动态数据源Bean
     * {@link com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties#postRegister} 注册数据源
     * 配置：
     * 通过定制sentinel-console实现规则持久化，规则data-id参照{@link com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil}
     * 数据转换参照{@link com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfig}
     *
     * sentinel-client接收动态数据处理类:{@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayApiDefinitionGroupCommandHandler}
     *
     * <h1>SCG sentinel拦截/过滤器</h1>
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter} sentinel全局过滤器，实现网关限流、API分组限流
     * api分组匹配逻辑:
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.sc.api.GatewayApiMatcherManager}
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.common.api.matcher.AbstractApiMatcher}
     * 可以看出同一分组下配置的多个匹配规则是“或”的关系。
     *
     * <h1>sentinel metric监控记录</h1>
     * {@link com.alibaba.csp.sentinel.slots.statistic.MetricEvent} 监控指标常量
     * {@link com.alibaba.csp.sentinel.node.metric.MetricWriter} 将资源metric日志写入文件
     * {@link com.alibaba.csp.sentinel.Tracer#traceContext}
     * {@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorSubscriber}.hookOnError
     * {@link com.alibaba.csp.sentinel.adapter.reactor.InheritableBaseSubscriber#onError}
     *
     * <h1>WebFlux异常处理流程</h1>
     * 响应结果Mono.onError异常处理首先由 {@link org.springframework.web.server.handler.ExceptionHandlingWebHandler#handle} 中处理，装饰了 HttpWebHandlerAdapter。
     * 遍历所有异常处理器，如果没找到合适的处理器或者处理后还是Mono.error，
     * 就会被外层的{{@link org.springframework.web.server.adapter.HttpWebHandlerAdapter#handleUnresolvedError} 处理。
     *
     * 使用try catch对handle包裹，所以只会在网关抛出异常时调用onError,
     * sentinel使用InheritableBaseSubscriber监听了onError，路由服务器响应的httpstatus不会触发onError，所以也不会记录到监控日志的 exceptionQps 中。
     * <code>
     *     for (WebExceptionHandler handler : this.exceptionHandlers) {
     *         completion = completion.onErrorResume(ex -> handler.handle(exchange, ex));
     *     }
     * </code>
     * 注入的异常处理实现类中通过使用<code>Mono.error(throwable);</code>可以让其他的异常处理类接着处理。
     * 在全局Filter中通过返回<code>Mono.error(throwable);</code>的方式触发异常处理。
     *
     * <h1>Spring WebFlux处理请求流程</h1>
     * <img src="https://img-blog.csdnimg.cn/20190529064535152.jpg" style="width: 2241px;">
     * {@link reactor.netty.http.server.HttpServerHandle}.onStateChange >
     * {@link org.springframework.http.server.reactive.ReactorHttpHandlerAdapter}.apply >
     * {@link org.springframework.web.server.adapter.HttpWebHandlerAdapter#handle}（ServerWebExchange管理上下文） >
     * {@link org.springframework.web.server.handler.ExceptionHandlingWebHandler} 装饰 HttpWebHandlerAdapter >
     * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler} 装饰 ExceptionHandlingWebHandler >
     * {@link org.springframework.web.reactive.DispatcherHandler}.handle
     *
     * HttpWebHandlerAdapter由{@link WebHttpHandlerBuilder#build()} 创建，因此 getDelegate 取到的是{@link org.springframework.web.server.handler.ExceptionHandlingWebHandler}
     * {@link org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration} 注册 HttpHandler，
     * 这里会调用 {@link WebHttpHandlerBuilder#applicationContext(ApplicationContext)} 和{@link WebHttpHandlerBuilder#build()}，
     * 异常处理的{@link org.springframework.web.server.WebExceptionHandler}接口实现也是在这里注入到 ExceptionHandlingWebHandler 中。
     *
     * <h1>scg网关断言过滤路由处理流程解析</h1>
     * {@link org.springframework.web.reactive.DispatcherHandler#handle(ServerWebExchange)}
     * 1）handlerMappings中会依次执行predicateHandler实现类的getHandler方法，合并取到的Hanlder结果集并提取第一个Handler（.next()方法的作用，获取结果的第一个数据作为新的Mono）.
     *
     * 网关断言过滤器handler：{@link org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping}，调用lookupRoute从所有的路由中断言匹配Route，
     * 然后返回{@link org.springframework.cloud.gateway.handler.FilteringWebHandler}并把匹配到的Route存入ServerWebExchange上下文。
     *
     * FilteringWebHandler存放了GlobalFilter实现类的列表，所有GlobalFilter实现类的Bean会注册进来（GatewayAutoConfiguration），
     * 并使用其自定义的GatewayFilterAdapter或者OrderedGatewayFilter装饰。
     *
     * 如果未成功断言Path，则会调用{@link org.springframework.web.reactive.resource.ResourceWebHandler#handle} 资源path处理器匹配是否是资源path请求，
     * {@link org.springframework.web.servlet.resource.PathResourceResolver#resolveResourceInternal}
     *
     * 2）如果匹配到Handler则执行invokeHandler方法，遍历HandlerAdapter查找支持上面响应的Handler处理类的适配器。
     * 支持网关FilteringWebHandler的适配器是{@link org.springframework.web.reactive.result.SimpleHandlerAdapter}，
     * 执行 {@link org.springframework.cloud.gateway.handler.FilteringWebHandler}.handle。
     * 这里会将所有的 GlobalFilter和 Route中的GatewayFilter合并排序。然后执行 DefaultGatewayFilterChain 调用链的filter方法。
     * 所以所有的Filter最后都必须调用chain.filter来继续调用链。
     *
     * 3）转发请求的过滤器一般优先级最低，最后执行。比如：
     * {@link org.springframework.cloud.gateway.filter.NettyRoutingFilter}
     * {@link org.springframework.cloud.gateway.filter.ForwardRoutingFilter}
     * {@link org.springframework.cloud.gateway.filter.WebClientHttpRoutingFilter}
     * {@link org.springframework.cloud.gateway.filter.WebsocketRoutingFilter} 等
     * 这里会执行 NettyRoutingFilter 过滤器执行请求的转发，其中 getResponseTimeout 获取路由超时时间。
     * 而 {@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter} 过滤器使用then方法确保在所有过滤器执行完之后再执行其方法，
     * 其中会调用 response.writeWith 方法解析响应body(ModifyResponseGatewayFilter重新封装的response对象ModifiedServerHttpResponse最后也会调用装饰对象的writeWith方法)，
     * 由{@link org.springframework.http.server.reactive.ReactorServerHttpResponse#writeWith}装饰，
     * 在这里调用 doCommit 会修改 response commit状态。{@link org.springframework.http.server.reactive.AbstractServerHttpResponse#doCommit(Supplier)}
     * 
     * <b>所以可以通过提前替换 ServerWebExchange 的 ServerHttpResponse为代理代理，然后重新 writeWith 方法即可实现修改response body的目的</b>。
     * 自定义的过滤器也可以通过 then 方法来实现转发请求获得响应后的一些处理操作。
     * 过滤器链条上如果有多个 then, 那么遵从与链条相反的顺序执行，即最后执行的过滤器里面的then先执行。
     *
     *
     * 最终会调用{@link ServerHttpResponse#setComplete()} 完成请求响应。
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayNewApplication.class, args);
    }
    /**
     * 加载解析路由并保存到内存的执行步骤：
     * 1. 调用{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#cachedCompositeRouteLocator}初始化{@link CachingRouteLocator}，
     * 参数为{@link RouteLocator}接口的实现类集合。比如{@link GatewayNewApplication#myRoutes}定义的实现
     * 和{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionRouteLocator}注入的实现
     *
     * 2. 调用{@link CachingRouteLocator#fetch}方法，并调用{@link CompositeRouteLocator#getRoutes()}方法。该方法会遍历步骤1中RouteLocator的实现类并分别调用其getRoutes方法，
     * 然后将返回的结果合并，即{@link org.springframework.cloud.gateway.route.Route}集合
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
     * 动态路由实现参考{@link DynamicGatewayService}
     *
     * <h1>网关流控</h1>
     * {@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorSubscriber#entryWhenSubscribed}
     * {@link com.alibaba.csp.sentinel.CtSph#asyncEntryWithPriorityInternal}
     * {@link com.alibaba.csp.sentinel.adapter.gateway.common.slot.GatewayFlowSlot}.checkGatewayParamFlow 网关流控的插槽
     * {@link com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowChecker} 流控检查
     *
     * {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot#entry} 这里调用 节点的 increaseBlockQps 方法将拦截数量写入metric
     */
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        String httpUri = "http://baidu.com:80";
        return builder.routes()
                .route("route-demo-122", p -> p
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
                .route("route-demo-123", p -> p
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
