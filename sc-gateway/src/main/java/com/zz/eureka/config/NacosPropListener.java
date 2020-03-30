package com.zz.eureka.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.spring.context.event.config.NacosConfigurationPropertiesBeanBoundEvent;
import com.zz.eureka.service.DynamicGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 15:46
 * ************************************
 */
@Component
public class NacosPropListener implements ApplicationListener<NacosConfigurationPropertiesBeanBoundEvent> {
    @Autowired
    private RouteRuleProp predicateRulesList;
    @Autowired
    private DynamicGatewayService dynamicGatewayService;
    
    @Override
    public void onApplicationEvent(NacosConfigurationPropertiesBeanBoundEvent event) {
        if(event.getBean() instanceof RouteRuleProp && predicateRulesList != null) {
            System.out.println("predicateRulesList:" + JSON.toJSONString(predicateRulesList.getRules()));
            dynamicGatewayService.update();
        }
    }
}
