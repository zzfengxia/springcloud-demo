package com.zz.scgatewaynew;

import brave.Tracer;
import brave.http.HttpServerRequest;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zz.scgatewaynew.service.DynamicGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.CompositeRouteLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.handler.FilteringWebHandler;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.URI;
import java.util.UUID;
import java.util.function.BiFunction;
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
     * <h1>sentinel控制台交互</h1>
     * <h2>sentinel客户端向sentinel-console上报心跳</h2>
     * 1. 通过{@link com.alibaba.csp.sentinel.Env}和{@link com.alibaba.csp.sentinel.cluster.ClusterStateManager} 调用 {@link InitExecutor#doInit}
     *    来初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类。
     * Env在sentinel过滤器中才会被调用，因此上报心跳是延迟的，等网关成功路由才会上报，这会让sentinel-console收集metric的起始时间大于第一次记录metric的时间(尽验证，这里不会有影响，而且提前调用InitExecutor.doInit();会导致无法初始化appType)；
     * 还有就是由于FlowRuleManager也是在第一次路由时加载，导致的MetricTimerListener中的MetricWriter也是延迟创建，
     * 从而导致第一次write metric时会走`else if (second == lastSecond) {`逻辑，该逻辑不会写入位置信息到idx文件，这样就会导致这条metric数据无法被MetricSearcher读取到。
     * 解决方法：参考{@link com.zz.scgatewaynew.config.SpringStartCallback#run(ApplicationArguments)}中的调用
     *
     * 2. sentinel-transport客户端服务上报信息是初始化的实现类 {@link com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc}
     * 3. 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.HeartbeatSender}
     *    的实现类{@link com.alibaba.csp.sentinel.transport.heartbeat.SimpleHttpHeartbeatSender}。请求地址是“/registry/machine”。
     * 不设置“csp.sentinel.dashboard.serve”参数console自身就不会注册，但是SimpleHttpHeartbeatSender的定时任务还是会继续运行。
     *
     * <h2>sentinel客户端开放接口</h2>
     * ClusterStateManager初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类
     * sentinel-transport客户端开启sentinel命令的接口 {@link com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc}
     * 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.CommandCenter}
     * 的实现类 {@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}
     *
     * spring.cloud.sentinel.transport.port 配置的端口会在{@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}
     * 中用来开启socket监听服务（如果端口被占用，则会自动使用别的端口）。
     * 然后使用{@link com.alibaba.csp.sentinel.transport.command.http.HttpEventTask} 类做实际的处理操作。
     * {@link com.alibaba.csp.sentinel.command.annotation.CommandMapping}注解用来注册接口实际处理逻辑。
     * {@link com.alibaba.csp.sentinel.command.CommandHandlerProvider} 注册Handler
     * 比如sentinel console实时监控界面获取metric信息的请求<code>metric</code>
     * 就是sentinel客户端的{@link com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler}类处理的。
     * 实际metric数据来源就是从sentinel的metric日志文件中读取的。
     * <p>如果需要新增CommandHandler实现，则需要在SPI文件com.alibaba.csp.sentinel.command.CommandHandler中注册CommandHandler实现类才会生效。</p>
     *
     *
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
     * <h2>scg sentinel加载slot实现流控、降级解析</h2>
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter} sentinel全局过滤器，实现网关限流、API分组限流。
     * 拦截网关响应，让{@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer#apply(Publisher)} 返回的Publisher代理。
     * {@link com.alibaba.csp.sentinel.adapter.reactor.MonoSentinelOperator}，最终订阅时会调用{@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorSubscriber#entryWhenSubscribed}.
     * 调用{@link com.alibaba.csp.sentinel.CtSph#asyncEntryWithType}#{@link com.alibaba.csp.sentinel.CtSph#asyncEntryWithPriorityInternal},然后调用chain.entry进行相关处理。
     * 其中{@link com.alibaba.csp.sentinel.CtSph#lookProcessChain(ResourceWrapper)} 用来创建ProcessorSlot，即Slot链也就是流控、降级等逻辑的处理链。
     * 创建ProcessorSlot：
     * 通过SPI方式获取配置的 {@link com.alibaba.csp.sentinel.slotchain.SlotChainBuilder} 接口实现类，没有则默认是{@link com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder}。
     * 然后调用实现类的build方法，这里又会通过SPI方式加载接口{@link com.alibaba.csp.sentinel.slotchain.ProcessorSlot}配置的所有实现类，这些实现类就是真正的处理限流、降级逻辑，
     * 然后按顺序放入{@link com.alibaba.csp.sentinel.slotchain.DefaultProcessorSlotChain}创建的调用链。
     * 网关流控由{@link com.alibaba.csp.sentinel.adapter.gateway.common.slot.GatewayFlowSlot}处理
     *
     * {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} 是sentinel统计metric信息的插槽，metric信息都在这里处理。除了BlockException外，其他metric信息都是在exit中记录。
     *
     * <h1>sentinel metric监控记录</h1>
     * {@link com.alibaba.csp.sentinel.slots.statistic.MetricEvent} 监控指标常量
     * {@link com.alibaba.csp.sentinel.node.metric.MetricWriter} 将资源metric日志写入文件
     * {@link com.alibaba.csp.sentinel.Tracer#traceContext}
     * {@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorSubscriber}.hookOnError
     * {@link com.alibaba.csp.sentinel.adapter.reactor.InheritableBaseSubscriber#onError}
     * {@link com.alibaba.csp.sentinel.node.StatisticNode}，写入文件使用的是 rollingCounterInMinute
     * {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}
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
     * {@link reactor.netty.http.server.HttpServerHandle#onStateChange} >
     * {@link org.springframework.http.server.reactive.ReactorHttpHandlerAdapter}.apply >
     * {@link org.springframework.web.server.adapter.HttpWebHandlerAdapter#handle}（ServerWebExchange管理上下文） >
     * {@link org.springframework.web.server.handler.ExceptionHandlingWebHandler} 装饰 HttpWebHandlerAdapter >
     * {@link org.springframework.web.server.handler.FilteringWebHandler} 装饰 ExceptionHandlingWebHandler，处理WebFilter过滤器（非网关的GlobalFilter）实现 >
     * {@link org.springframework.web.reactive.DispatcherHandler}.handle 上面所有的WebFilter调用完成后会调用这里
     *
     * 在{@link org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration}#httpHandler
     * 调用 {@link WebHttpHandlerBuilder#build()} 方法创建{@link org.springframework.http.server.reactive.HttpHandler}，
     * 由{@link org.springframework.web.server.adapter.HttpWebHandlerAdapter}代理，
     * 这里会调用 {@link WebHttpHandlerBuilder#applicationContext(ApplicationContext)} 和{@link WebHttpHandlerBuilder#build()}。
     * WebHanlder和 {@link org.springframework.web.server.WebFilter} 过滤器也会在这里注入，
     * 其中注册的 WebFilter 实现类会在{@link WebHttpHandlerBuilder#applicationContext(ApplicationContext)} 这里排序，
     * 异常处理的{@link org.springframework.web.server.WebExceptionHandler}接口实现也是在这里注入到 ExceptionHandlingWebHandler 中。
     *
     * WebHandler和WebFilter作为属性注入到FilteringWebHandler中。其中WebHanlder是{@link org.springframework.web.reactive.DispatcherHandler}，
     * 在{@link org.springframework.web.reactive.config.WebFluxConfigurationSupport}注册的WebHandler是被装饰的。
     *
     *
     * 服务启动时执行{@link SpringApplication#run(String...)}，调用 refreshContext，refresh
     * {@link ReactiveWebServerApplicationContext#onRefresh()}，调用 {@link ReactiveWebServerApplicationContext#createWebServer()}
     * 获取{{@link org.springframework.boot.web.reactive.server.ReactiveWebServerFactory}接口的Bean对象
     * {@link org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory}（在ReactiveWebServerFactoryConfiguration中创建Bean），
     * 然后作为属性初始化进{@link org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext.ServerManager}。
     * ServerManager 构造方法中调用的 {@link org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory#getWebServer}以及 createHttpServer，
     * 初始化 {@link org.springframework.http.server.reactive.ReactorHttpHandlerAdapter}（初始化 HttpHandler 属性，此处为ServerManager对象，代理了实际的HttpHandler对象，即后面通过getHttpHandler取到的HttpWebHandlerAdapter）
     * 和 {@link org.springframework.boot.web.embedded.netty.NettyWebServer}。ReactorHttpHandlerAdapter 对象放入NettyWebServer中。
     *
     * {@link ReactiveWebServerApplicationContext#finishRefresh()}#startReactiveWebServer#start
     * 方法中调用了getHttpHandler获取初始化的 HttpWebHandlerAdapter，这个就是上面 {@link WebHttpHandlerBuilder#build()}创建的HttpHandler。
     * 然后会调用 {@link org.springframework.boot.web.embedded.netty.NettyWebServer#start} 和 startHttpServer，
     * 在startHttpServer方法中会调用{@link reactor.netty.http.server.HttpServer#handle(BiFunction)} 创建HttpServerHandle监听请求，
     * 并把ReactorHttpHandlerAdapter放入HttpServerHandle对象中。
     * 
     * 综上：
     * {@link reactor.netty.http.server.HttpServerHandle#onStateChange}中的handler对象为 {@link org.springframework.http.server.reactive.ReactorHttpHandlerAdapter}。
     * {@link org.springframework.http.server.reactive.ReactorHttpHandlerAdapter} 中的httpHandler对象为 {@link org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext.ServerManager}。
     * ServerManager代理了 HttpHandler为 {@link org.springframework.web.server.adapter.HttpWebHandlerAdapter#handle(ServerHttpRequest, ServerHttpResponse)}对象，
     * 然后由{@link WebHttpHandlerBuilder#build()}得知最后会依次代理 {@link org.springframework.web.server.handler.ExceptionHandlingWebHandler}、
     * {@link FilteringWebHandler}以及最终的 {@link org.springframework.web.reactive.DispatcherHandler}。
     * 其中 FilteringWebHandler 是用来处理所有的 WebFilter 过滤链，处理完成后才会调用最终的 DispatcherHandler。
     *
     *
     * <h1>scg网关断言过滤路由处理流程解析</h1>
     * {@link org.springframework.web.reactive.DispatcherHandler#handle(ServerWebExchange)}
     * 1）handlerMappings中会依次执行predicateHandler实现类的getHandler方法，合并取到的Hanlder结果集并提取第一个Handler（.next()方法的作用，获取结果的第一个数据作为新的Mono）.
     *
     * 网关断言过滤器handler：{@link org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping}，调用lookupRoute从所有的路由中断言匹配Route，
     * 然后返回{@link org.springframework.cloud.gateway.handler.FilteringWebHandler}Gateway的GlobalFilter过滤器处理Handler，
     * 这里要区别于WebFlux的 {@link org.springframework.web.server.handler.FilteringWebHandler} WebFilter（类似springmvc的Filter），
     * 并把匹配到的Route存入ServerWebExchange上下文。
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
     *
     *
     * <h1>spring-cloud-sleuth调用链追踪(日志追踪)组件</h1>
     * {@link org.springframework.cloud.sleuth.instrument.web.TraceWebFilter#filter(ServerWebExchange, WebFilterChain)}
     * 服务启动时先在{{@link org.springframework.cloud.sleuth.instrument.reactor.HookRegisteringBeanDefinitionRegistryPostProcessor#setupHooks(ConfigurableApplicationContext)}
     * 注册Mono、Flux的钩子，此钩子会在之前执行。注册钩子调用的是{@link org.springframework.cloud.sleuth.instrument.reactor.ReactorSleuth#scopePassingSpanOperator}。
     * {@link org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration#sleuthCurrentTraceContext} 创建 {@link brave.propagation.ThreadLocalCurrentTraceContext}。
     *
     * {@link org.springframework.cloud.sleuth.instrument.web.TraceWebFilter.MonoWebFilterTrace#findOrCreateSpan(Context)}方法中会从请求Request中获取一些信息，并创建Span，
     * 具体在{@link brave.http.HttpServerHandler#handleReceive}（后面详细解析这里生成traceid的代码）
     * 上面的方法执行之后才会走到scopePassingSpanOperator方法的new {@link org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber#onSubscribe}，
     * 然后Mono响应被该类代理，从而执行其中的 onSubscribe。
     * 然后会执行到{@link brave.propagation.ThreadLocalCurrentTraceContext#newScope(TraceContext)}，
     * 其父类 CurrentTraceContext的scopeDecorators属性会有 {@link org.springframework.cloud.sleuth.log.Slf4jScopeDecorator}，
     * 在{@link org.springframework.cloud.sleuth.log.SleuthLogAutoConfiguration.Slf4jConfiguration}中被注入。
     * 因此又会执行到{@link org.springframework.cloud.sleuth.log.Slf4jScopeDecorator#decorateScope}，
     * 然后执行{@link brave.baggage.CorrelationScopeDecorator.Multiple#decorateScope}，其中的context.update 这里会把日志相关信息存入MDC，
     * 即{@link brave.context.slf4j.MDCScopeDecorator.MDCContext#update(String, String)}。
     * {@link brave.propagation.TraceIdContext#toTraceIdString(long, long)} 新版本取traceid
     *
     * {@link brave.http.HttpServerHandler#handleReceive}中先调用defaultExtractor.extract(request)，即{@link brave.propagation.B3Propagation.B3Extractor#extract}
     * 获取request头部信息相关key，
     * 然后执行{@link brave.http.HttpServerHandler#nextSpan(TraceContextOrSamplingFlags, HttpServerRequest)}，前面请求头中没有相关key则TranceContext为null，
     * 执行{@link Tracer#nextSpan(TraceContextOrSamplingFlags)}，在{@link Tracer#decorateContext}方法中生成traceID和spanID，{@link Tracer#nextId}，这里显然生成的id是不唯一的，只是一个随机数。
     *
     *
     * 总结：
     * sleuth通过注册Mono/Flux的Hook钩子实现向MDC中写入追踪信息以及使用WebFlux过滤器代理请求响应Mono，
     * 定制onSubscribe、onComplete等方法向转发的请求头中添加信息来实现日志、调用链的追踪。
     *
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
