package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntityWrapper;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.zz.gateway.common.nacos.entity.route.RouteRuleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-22 15:27
 * ************************************
 */
@Component("gatewayRouteRuleNacosProvider")
public class GatewayRouteRuleNacosProvider implements DynamicRuleProvider<RuleEntityWrapper<RouteRuleEntity>> {
    @Autowired
    private ConfigService configService;
    @Autowired
    @Qualifier("gatewayRouteDecoder")
    private Converter<String, RuleEntityWrapper<RouteRuleEntity>> converter;
    
    @Override
    public RuleEntityWrapper<RouteRuleEntity> getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + NacosConfigUtil.GATEWAY_ROUTE_DATA_ID_POSTFIX,
                NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return null;
        }
        return converter.convert(rules);
    }
}
