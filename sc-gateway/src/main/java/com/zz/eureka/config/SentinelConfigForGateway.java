package com.zz.eureka.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.eureka.routedefine.CustomApiDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 * sentinel网关限流配置
 * 单机本地限流
 *
 * @author Francis.zz
 * @date 2020-03-18 11:52
 * ************************************
 */
@Configuration
public class SentinelConfigForGateway {
    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;
    
    public SentinelConfigForGateway(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }
    
    /**
     * 注入限流异常处理
     * 可定制限流响应信息，默认为{@link com.alibaba.csp.sentinel.adapter.gateway.sc.callback.DefaultBlockRequestHandler}
     *
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        // 定制限流后的响应信息, 默认处理类为 DefaultBlockRequestHandler
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            // JSON result by default.
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("服务器繁忙，请稍后重试"));
        });
        
        // Register the block exception handler for Spring Cloud Gateway.
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }
    
    @Bean
    public GlobalFilter sentinelGatewayFilter() {
        // 参数为执行顺序ordered,顺序越小越优先
        return new SentinelGatewayFilter(-1);
    }
    
    private String serverAddr = "172.16.80.132:8848";
    private String groupId = "sentinel:demo";
    //@Value("nacos.config.dataId")
    private String dataId = "sentinel.txt";
    
    @PostConstruct
    public void doInit() {
        //initCustomizedApis();
        //initGatewayRules();
        
        // 注册动态资源推送。接入nacos配置中心推送
        /**
         * 动态限流规则配置参考
         * <pre>
         * [
         *     {
         *       "resource": "route-demo-1",
         *       "count": 2.0,
         *       "intervalSec": 1.0,
         *       "paramItem": {
         *           "parseStrategy": 2,
         *           "fieldName": "flowctrlflag",
         *           "pattern": "true",
         *           "matchStrategy": 0
         *       }
         *     }
         * ]
         * </pre>
         */
        ReadableDataSource<String, Set<GatewayFlowRule>> flowRuleDataSource = new NacosDataSource<>(
                serverAddr, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        GatewayRuleManager.register2Property(flowRuleDataSource.getProperty());
    
        // API分组动态配置
        /**
         * API分组动态配置参考
         * <pre>
         * [
         *      {
         *          "apiName": "customized_api1",
         *          "predicateItems": [{
         *              "pattern": "/dispatcher",
         *              "matchStrategy": 0
         *          }]
         *      }
         * ]
         * </pre>
         */
        ReadableDataSource<String, Set<ApiDefinition>> apiDefDataSource = new NacosDataSource<>(
                serverAddr, groupId, "sentinel2.txt",
                source -> {
                    // 将Set<CustomApiDefinition>转成Set<ApiDefinition> todo 简单实现
                    Set<CustomApiDefinition> apiDefinitionSet = JSON.parseObject(source, new TypeReference<Set<CustomApiDefinition>>() {});
                    Set<ApiDefinition> result = new HashSet<>(apiDefinitionSet.size());
                    if(apiDefinitionSet.isEmpty()) {
                        return result;
                    }
                    apiDefinitionSet.forEach(item -> {
                        ApiDefinition definition = new ApiDefinition();
                        definition.setApiName(item.getApiName());
                        if(item.getPredicateItems() != null) {
                            item.getPredicateItems().forEach(t -> {
                                ApiPathPredicateItem apiPredicateItem = new ApiPathPredicateItem();
                                apiPredicateItem.setPattern(t.getPattern());
                                apiPredicateItem.setMatchStrategy(t.getMatchStrategy());
                                if(definition.getPredicateItems() == null) {
                                    definition.setPredicateItems(new HashSet<>());
                                }
                                definition.getPredicateItems().add(apiPredicateItem);
                            });
                        }
                        
                        result.add(definition);
                    });
                    return result;
                });
        GatewayApiDefinitionManager.register2Property(apiDefDataSource.getProperty());
    }
    
    /**
     * 自定义api分组。可以将多个API合并为一组进行统一管理分配规则
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("some_customized_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/ahas"));
                    add(new ApiPathPredicateItem().setPattern("/product/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        ApiDefinition api2 = new ApiDefinition("another_customized_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(api1);
        definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }
    
    /**
     * 为resource定制限流规则
     * 网关限流规则 GatewayFlowRule 的字段解释如下(<url>https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E9%99%90%E6%B5%81</url>)：
     * resource：可以是自定义api分组名、gateway route id
     * resourceMode：规则是针对 API Gateway 的 route（RESOURCE_MODE_ROUTE_ID）还是用户在 Sentinel 中定义的 API 分组（RESOURCE_MODE_CUSTOM_API_NAME），默认是 route。
     * grade：限流指标维度，同限流规则的 grade 字段。其中，0 代表根据并发数量来限流，1 代表根据 QPS 来进行流量控制
     * count：限流阈值
     * intervalSec：统计时间窗口，单位是秒，默认是 1 秒。
     * controlBehavior：流量整形的控制效果，同限流规则的 controlBehavior 字段，目前支持快速失败和匀速排队两种模式，默认是快速失败。
     * burst：应对突发请求时额外允许的请求数目。
     * maxQueueingTimeoutMs：匀速排队模式下的最长排队时间，单位是毫秒，仅在匀速排队模式下生效。
     * paramItem：参数限流配置。若不提供，则代表不针对参数进行限流，该网关规则将会被转换成普通流控规则；否则会转换成热点规则。其中的字段：
     *      parseStrategy：从请求中提取参数的策略，目前支持提取来源 IP（PARAM_PARSE_STRATEGY_CLIENT_IP）、Host（PARAM_PARSE_STRATEGY_HOST）、任意 Header（PARAM_PARSE_STRATEGY_HEADER）和任意 URL 参数（PARAM_PARSE_STRATEGY_URL_PARAM）四种模式。
     *      fieldName：若提取策略选择 Header 模式或 URL 参数模式，则需要指定对应的 header 名称或 URL 参数名称。
     *      pattern：参数值的匹配模式，只有匹配该模式的请求属性值会纳入统计和流控；若为空则统计该请求属性的所有值。（1.6.2 版本开始支持）
     *      matchStrategy：参数值的匹配策略，目前支持精确匹配（PARAM_MATCH_STRATEGY_EXACT）、子串匹配（PARAM_MATCH_STRATEGY_CONTAINS）和正则匹配（PARAM_MATCH_STRATEGY_REGEX）。（1.6.2 版本开始支持）
     *      用户可以通过 GatewayRuleManager.loadRules(rules) 手动加载网关规则，或通过 GatewayRuleManager.register2Property(property) 注册动态规则源动态推送（推荐方式）
     *
     * 同一资源配置多个规则时，会遍历规则任何一个满足都会生效
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("route-demo-2")
                .setCount(10)
                .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("route-demo-2")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );
        rules.add(new GatewayFlowRule("route-demo-2")
                .setCount(10)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(600)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("X-Sentinel-Flag")
                )
        );
        rules.add(new GatewayFlowRule("route-demo-2")
                .setCount(1)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pa")
                )
        );
        rules.add(new GatewayFlowRule("route-demo-1")
                .setCount(2)
                .setIntervalSec(5)
                // QPS=并发数/平均响应时间
                //.setGrade()
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("flowctrlflag")
                        .setPattern("true")
                        .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT)
                )
        );
        
        rules.add(new GatewayFlowRule("some_customized_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pn")
                )
        );
        GatewayRuleManager.loadRules(rules);
    }
}
