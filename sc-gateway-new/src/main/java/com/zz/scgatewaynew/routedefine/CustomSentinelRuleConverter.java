package com.zz.scgatewaynew.routedefine;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-21 16:11
 * ************************************
 */
public class CustomSentinelRuleConverter implements Converter<String, Set<GatewayFlowRule>> {
    /**
     * 动态限流规则配置参考
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
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler}
     *
     * @param source
     */
    @Override
    public Set<GatewayFlowRule> convert(String source) {
        String config = source;
        try {
            SentinelDefinition definition = JSON.parseObject(config, SentinelDefinition.class);
            if(definition != null && StringUtils.isNotEmpty(definition.getFlowRule())) {
                config = definition.getFlowRule();
            }
        } catch (Exception e) {
        
        }
        return JSON.parseObject(config, new TypeReference<Set<GatewayFlowRule>>() {});
    }
}
