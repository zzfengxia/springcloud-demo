package com.zz.scgatewaynew.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.sccommon.util.LogUtils;
import com.zz.scgatewaynew.nacos.entity.ApiDefinitionEntity;
import com.zz.scgatewaynew.nacos.entity.GatewayFlowRuleEntity;
import com.zz.scgatewaynew.nacos.entity.RuleEntityWrapper;
import com.zz.scgatewaynew.respdefine.IFailResponse;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 * sentinel网关限流配置
 * 单机本地限流
 *
 * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}
 *
 * @author Francis.zz
 * @date 2020-03-18 11:52
 * ************************************
 */
@Configuration
@Slf4j
public class SentinelConfigForGateway {
    @Autowired
    private ResponseFactoryService responseFactoryService;
    /**
     * 注入限流异常处理
     * 可定制限流响应信息，默认为{@link com.alibaba.csp.sentinel.adapter.gateway.sc.callback.DefaultBlockRequestHandler}
     * 使用spring-cloud-alibaba-sentinel-gateway 集成需要创建SPI文件，指定GatewaySlotChainBuilder才能使限流生效
     *
     * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}#blockRequestHandlerOptional
     */
    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange,
                                                      Throwable t) {
                String uid = GatewayUtils.getTraceIdFromCache(exchange);
                LogUtils.saveSessionIdForLog(uid);
                log.info("请求已被限流");
    
                IFailResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(exchange, ErrorCode.TOO_MANY_REQUESTS.getReturnMsg(), ErrorCode.TOO_MANY_REQUESTS.getErrorCode());
    
                // JSON result by default.
                return ServerResponse.status(failResponseInfo.getCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(failResponseInfo.getMsg()));
            }
        };
    }
    
    /**
     * 网关限流动态规则转换，sentinel-console配置并存储在nacos， 配置参考
     * <pre>
     * [
     *     {
     *       "resource": "customized_api1",
     *       "resourceMode": 1,
     *       "count": 1.0,
     *       "intervalSec": 5.0,
     *       "paramItem": {
     *           "parseStrategy": 2,
     *           "fieldName": "flowctrlflag",
     *           "pattern": "true",
     *           "matchStrategy": 0
     *       }
     *     }
     * ]
     * </pre>
     * 存储实体类：GatewayFlowRuleEntity
     * beanName必须使用“sentinel-”开头，后面字符串则为application.yml中的converter-class配置
     * <code>
     *     converter-class: gatewayFlowDecoder
     *     data-type: custom
     * </code>
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler}
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayRuleCommandHandler} sentinel-client接收处理动态Flow rule实现
     */
    @Bean("sentinel-gatewayFlowDecoder")
    public Converter<String, Set<GatewayFlowRule>> gatewayFlowDecoder() {
        return s -> {
            RuleEntityWrapper<GatewayFlowRuleEntity> ruleEntityWrapper = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<GatewayFlowRuleEntity>>(){});
            
            return ruleEntityWrapper.getRuleEntity().stream().map(GatewayFlowRuleEntity::toGatewayFlowRule).collect(Collectors.toSet());
        };
    }
    
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
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler}
     * 存储实体类：ApiDefinitionEntity
     * beanName必须使用“sentinel-”开头
     * 转换参考：{@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayApiDefinitionGroupCommandHandler#parseJson}
     */
    @Bean("sentinel-apiDefinitionDecoder")
    public Converter<String, Set<ApiDefinition>> apiDefinitionDecoder() {
        return s -> {
            RuleEntityWrapper<ApiDefinitionEntity> apiEntity = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<ApiDefinitionEntity>>(){});
            
            return apiEntity.getRuleEntity().stream().map(ApiDefinitionEntity::toApiDefinition).collect(Collectors.toSet());
        };
    }
}
