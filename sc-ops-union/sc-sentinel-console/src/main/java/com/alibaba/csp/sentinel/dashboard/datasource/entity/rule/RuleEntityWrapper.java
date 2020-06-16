package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.zz.gateway.common.nacos.entity.route.BodyRuleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-26 10:35
 * ************************************
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RuleEntityWrapper<T> {
    /**
     * 维护当前最新ID
     */
    private Long curId;
    private Date updateTime;
    private List<T> ruleEntity;
    
    public static void main(String[] args) {
        BodyRuleEntity routeRule = new BodyRuleEntity();
        routeRule.setOrder(100);
        System.out.println(routeRule.getOrder());
        String json = "{\"ruleEntity\":[{\"id\":\"route-demo-1\",\"order\":-100,\"uri\":\"http://172.16.80.103:9087/\",\"useCommonConfig\":true},{\"id\":\"route-demo-2\",\"order\":-50,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/sptsm/notify\",\"/sptsm/api/asyncStatus\",\"/sptsm/thirdParty\",\"/sptsm/universalNotify\"],\"respStrategy\":1}},\"uri\":\"http://172.16.80.103:9087/\",\"useCommonConfig\":false},{\"id\":\"route-demo-3\",\"order\":-60,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/sptsm/wechatNotify/**/**\",\"/sptsm/notify/**/**\",\"/sptsm/orderPaid\"],\"respStrategy\":2}},\"uri\":\"http://172.16.80.103:9087/\",\"useCommonConfig\":false},{\"id\":\"route-demo-4\",\"order\":-40,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/mq/getDemo/**\",\"/mq/postDemo*/**\"],\"respStrategy\":1}},\"uri\":\"http://localhost:8083\",\"useCommonConfig\":false},{\"id\":\"route-demo-5\",\"order\":-50,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/getOrder/**\",\"/order/**\"],\"respStrategy\":1}},\"uri\":\"lb://sc-order\",\"useCommonConfig\":false},{\"id\":\"route-demo-6\",\"order\":-50,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/getOrder/**\",\"/createOrder/**\"],\"respStrategy\":1}},\"uri\":\"lb://sc-order\",\"useCommonConfig\":false},{\"id\":\"route-demo-7\",\"order\":-61,\"predicate\":{\"bodyRule\":{\"order\":-2147483648,\"strategy\":\"and\"},\"order\":0,\"pathRule\":{\"order\":0,\"path\":[\"/sptsm/unionNotify/**\"],\"respStrategy\":3}},\"uri\":\"http://172.16.80.103:9087/\",\"useCommonConfig\":false}]}";
        //RuleEntityWrapper<RouteRule> result = JSON.parseObject(json, new TypeReference<RuleEntityWrapper<RouteRule>>(){});
    }
}
