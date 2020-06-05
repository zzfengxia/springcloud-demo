/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntityWrapper;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Configuration
public class NacosConfig {
    @Value("${nacos.config.addr:localhost}")
    private String serverAddr;
    
    @Bean("flowRuleEncoder")
    public Converter<RuleEntityWrapper<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return d -> JSON.toJSONStringWithDateFormat(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    @Bean("flowRuleDecoder")
    public Converter<String, RuleEntityWrapper<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<RuleEntityWrapper<FlowRuleEntity>>(){});
    }
    
    @Bean("degradeRuleEncoder")
    public Converter<RuleEntityWrapper<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return d -> JSON.toJSONStringWithDateFormat(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    @Bean("degradeRuleDecoder")
    public Converter<String, RuleEntityWrapper<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<RuleEntityWrapper<DegradeRuleEntity>>(){});
    }
    
    @Bean("apiDefinitionEncoder")
    public Converter<RuleEntityWrapper<ApiDefinitionEntity>, String> apiDefinitionEncoder() {
        return d -> JSON.toJSONStringWithDateFormat(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    @Bean("apiDefinitionDecoder")
    public Converter<String, RuleEntityWrapper<ApiDefinitionEntity>> apiDefinitionDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<RuleEntityWrapper<ApiDefinitionEntity>>(){});
    }
    
    /**
     * 网关流控
     *
     * @return
     */
    @Bean("gatewayFlowEncoder")
    public Converter<RuleEntityWrapper<GatewayFlowRuleEntity>, String> gatewayFlowEncoder() {
        return d -> JSON.toJSONStringWithDateFormat(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    @Bean("gatewayFlowDecoder")
    public Converter<String, RuleEntityWrapper<GatewayFlowRuleEntity>> gatewayFlowDecoder() {
        return s -> JSON.parseObject(s, new TypeReference<RuleEntityWrapper<GatewayFlowRuleEntity>>(){});
    }
    
    @Bean
    public ConfigService nacosConfigService() throws Exception {
        return ConfigFactory.createConfigService(serverAddr);
    }
}
