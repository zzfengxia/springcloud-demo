package com.zz.scorder;

import com.alibaba.cloud.sentinel.feign.SentinelFeign;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.zz.api.common.config.SentinelMybatisConfig;
import com.zz.sccommon.config.ClientSecurityConfig;
import feign.Feign;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务消费者，向Eureka服务注册中心注册服务
 * EnableFeignClients的EnableFeignClients指定需要扫描的feign client包
 */
@SpringBootApplication(scanBasePackages = {"com.zz.scservice.fallback", "com.zz.scorder"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zz.scservice"})
//@EnableCircuitBreaker
@ImportAutoConfiguration({ClientSecurityConfig.class, SentinelMybatisConfig.class})
public class ScOrderApplication {
    /**
     * OpenFeign 执行流程分析参考：https://www.cnblogs.com/chiangchou/p/api.html
     * @EnableFeignClients 注解开启FeignClient扫描，导入{{@link org.springframework.cloud.openfeign.FeignClientsRegistrar} 来扫描所有指定包下所有<code>FeignClient</code>注解类,
     * 并解析FeignClient注解的相关参数，用来生成Target(Feign代理客户端)
     *
     * Feign客户端默认由{@link org.springframework.cloud.openfeign.FeignClientsConfiguration#feignBuilder}构建
     * sentinel feign由{@link com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration}构建，覆盖默认构建
     * 超时时间、request client等{@link Feign.Builder#build()}设置.
     * {@link org.springframework.cloud.openfeign.FeignClientFactoryBean#getTarget}里面有创建Client
     * apache http client创建{@link org.springframework.cloud.openfeign.FeignAutoConfiguration.HttpClientFeignConfiguration#feignClient}，注入{@link feign.Client}
     * <p>如果使用的client是apach http client，则可以在{@link feign.httpclient.ApacheHttpClient#execute}中查看最终的http client配置</p>
     *
     * 其中超时时间有多个地方设置，{@link org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer#execute}中ribbon的超时时间默认是1s，
     * 可以使用ribbon.ReadTimeout=5000设置全部服务的请求超时时间。
     * ribbon参数设置参见：{@link com.netflix.client.config.DefaultClientConfigImpl}
     *
     * <h1>sentinel-feign代理请求服务的步骤</h1>
     * 1. sentinel{@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler#invoke}进行限流、降级等拦截处理
     * 2. {@link feign.SynchronousMethodHandler#invoke}包裹重试逻辑，其中buildTemplateFromArgs.create会解析动态url(@PathVariable)，这是在sentinel降级拦截之后执行的。
     * 3. {@link feign.SynchronousMethodHandler#targetRequest} 是构建请求参数，target为Feign代理，执行之前会先执行{@link feign.RequestInterceptor}所有注册的请求拦截器，
     * 可以通过实现RequestInterceptor接口自定义一些操作，比如向Header添加参数，修改请求服务ID（多租户定制服务实现）等。
     * 4. {@link feign.SynchronousMethodHandler#executeAndDecode} 执行请求操作
     *
     * <h1>sentinel降级说明</h1>
     * 实例化过程：
     * sentinel feign由{@link com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration}构建，覆盖默认构建的Feign.Builder
     * {@link SentinelFeign.Builder#build()}创建SentinelInvocationHandler
     * SentinelInvocationHandler实现资源降级操作
     * 1. 如果实现了fallback，那么只要请求服务报错就会执行fallback的降级方法(不管有没有触发降级配置)
     * @see {@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler#invoke}
     * @see {@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule#passCheck} 降级检查实现，可以通过提取sentinel-core模块
     * 2. RT配置：同1s内的请求数大于5({@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule#rtSlowRequestAmount})且平均响应时间大于阀值则在接下来的时间窗口触发熔断降级
     * 其余规则配置具体参考：
     * https://mrbird.cc/Sentinel%E6%8E%A7%E5%88%B6%E5%8F%B0%E8%AF%A6%E8%A7%A3.html
     * https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7
     * <h2>DegradeSlot</h2>
     * 降级插槽{@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot}
     * {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 初始化统计节点
     *
     * <h1>限流(FlowRule)异常拦截</h1>
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebMvcConfig} 注入BlockExceptionHandler，以及web处理相关参数
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebInterceptor}注入异常处理拦截器，拦截并处理BlockException异常。
     * 这里也会获取resourceName对应的所有插槽，包括降级插槽。{@link com.alibaba.csp.sentinel.CtSph#lookProcessChain}.
     * 也就是说如果接口触发降级也会在这里的preHandler中抛出DegradeException.
     *
     * 可以通过spring.cloud.setinel.blockPage 设置异常响应重定向的页面
     * 否则默认使用{@link com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler}类处理
     *
     * <h1>sentinel控制台交互</h1>
     * <h2>sentinel客户端向sentinel-console上报心跳</h2>
     * 1. {@link com.alibaba.csp.sentinel.cluster.ClusterStateManager} 调用 {@link InitExecutor#doInit} 来初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类
     * 2. sentinel-transport客户端服务上报信息是初始化的实现类 {@link com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc}
     * 3. 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.HeartbeatSender}的实现类{@link com.alibaba.csp.sentinel.transport.heartbeat.SimpleHttpHeartbeatSender}
     *
     * <h2>sentinel客户端开放接口</h2>
     * ClusterStateManager初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类
     * sentinel-transport客户端开启sentinel命令的接口 {@link com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc}
     * 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.CommandCenter}的实现类 {@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}
     *
     * spring.cloud.sentinel.transport.port 配置的端口会在{@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}中用来开启socket监听服务（如果端口被占用，则会自动使用别的端口）.
     * 然后使用{@link com.alibaba.csp.sentinel.transport.command.http.HttpEventTask} 类做实际的处理操作。
     * {@link com.alibaba.csp.sentinel.command.annotation.CommandMapping}注解用来注册接口实际处理逻辑。
     * {@link com.alibaba.csp.sentinel.command.CommandHandlerProvider} 注册Handler
     * 比如sentinel console实时监控界面获取metric信息的请求<code>metric</code>就是sentinel客户端的{@link com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler}类处理的。
     * 实际metric数据来源就是从sentinel的metric日志文件中读取的。
     *
     * <h1>动态规则</h1>
     * 参考{@link com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource} nacos数据源属性
     * {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler} 注册动态数据源Bean
     * {@link com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties#postRegister} 注册数据源
     *
     * <h1>规则配置说明</h1>
     * 例如有如下的接口树：
     * <code>
     *     /createOrder
     *     ├── POST:http://sc-service1/320200/createOrder
     *     ├── mybatis:com.zz.scorder.dao.ConfigMapper.selectByCardCode
     * </code>
     * “/createOrder” 接口为当前服务对外接口
     * “POST:http://sc-service1/320200/createOrder” 为当前服务的createOrder接口需要调用的上游接口
     * 对“/createOrder”接口可以配置限流、降级，降级也会在prehandler中体现，如果降级的话就不会再执行Controller中的代码。所有如果需要定制客户端响应也需要在`BlockExceptionHandler`中处理
     * 而对于“POST:http://sc-service1/320200/createOrder”来说就只能配置降级了，因为限流是在Controller之前拦截的，这里的降级执行的就是 `InvocationHandler`。
     *
     * <h1>nacos、sentinel相关日志输出目录配置</h1>
     * 1. sentinel客户端qps等日志：通过 `spring.cloud.sentinel.log.dir` 属性配置.
     * 2. nacos-client日志通过重写`nacos-logback.xml`配置文件或者使用java启动命令行参数 `-Dnacos.logging.config=` 配置. {@link com.alibaba.nacos.client.logging.AbstractNacosLogging}
     * 3. nacos-SNAPSHOT日志通过命令行参数 `-DJM.SNAPSHOT.PATH=` 配置. {@link com.alibaba.nacos.client.config.impl.LocalConfigInfoProcessor}
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ScOrderApplication.class, args);
    }
}
