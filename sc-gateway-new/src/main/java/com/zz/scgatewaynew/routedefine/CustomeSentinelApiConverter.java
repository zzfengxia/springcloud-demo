package com.zz.scgatewaynew.routedefine;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-21 16:15
 * ************************************
 */
public class CustomeSentinelApiConverter implements Converter<String, Set<ApiDefinition>> {
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
     *
     * @param source
     */
    @Override
    public Set<ApiDefinition> convert(String source) {
        String config = source;
        try {
            SentinelDefinition definition = JSON.parseObject(config, SentinelDefinition.class);
            if(definition != null && StringUtils.isNotEmpty(definition.getApiDefinition())) {
                config = definition.getApiDefinition();
            }
        } catch (Exception e) {
        
        }
        // 将Set<CustomApiDefinition>转成Set<ApiDefinition> todo 简单实现
        Set<CustomApiDefinition> apiDefinitionSet = JSON.parseObject(config, new TypeReference<Set<CustomApiDefinition>>() {});
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
    }
}
