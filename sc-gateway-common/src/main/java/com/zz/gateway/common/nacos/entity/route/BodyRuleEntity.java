package com.zz.gateway.common.nacos.entity.route;

import com.google.common.collect.Lists;
import com.zz.gateway.common.routedefine.RuleCheck;
import com.zz.gateway.common.routedefine.predicaterule.BodyRule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 * nacos配置中心的配置注入到java bean
 * 仅支持JSON格式数据的解析
 *
 * nacos解析数据类型的规则：
 * 先从dataId中获取，如果有“.”则取最后一个字符串作为type
 * 否则使用指定的type
 *
 * @author Francis.zz
 * @date 2020-03-26 15:07
 * ************************************
 */
@Data
@Slf4j
public class BodyRuleEntity implements RuleCheck {
    public static final List<String> ALLOW_ATTR = Lists.newArrayList("command", "caller", "issuerid");
    
    public static final String OR = "or";
    public static final String AND = "and";
    /**
     * 属性map,value为""或者null时，表示明确指定body体中没有key 这个属性
     * attrMap: {caller: OnePlusWallet1, command: ["query.order", "create.order"]}
     *
     * 例如下面配置：
     * <pre>
     * bodyRule:
     *   attrMap: {issueid: null, command: ["query.order", "create.order"]}
     *   strategy: and
     * </pre>
     * 表示当issueid不存在或者为空且command为"query.order", "create.order"时才会被正确路由
     *
     * todo request body仅支持json格式字符串
     *
     */
    private Map<String, List<String>> attrMap;
    
    private String strategy = AND;
    /**
     * predicate顺序，值越小越优先
     */
    private Integer order;
    
    @Override
    public void validate() {
        if(!AND.equals(strategy) && !OR.equals(strategy)) {
            throw new IllegalArgumentException("[strategy] must be or、and, so BodyRule config is invalid. now strategy:" + strategy);
        }
    }
    
    public BodyRule toBodyRule() {
        BodyRule bodyRule = new BodyRule();
        bodyRule.setAttrMap(attrMap);
        bodyRule.setOrder(order);
        bodyRule.setStrategy(strategy);
        
        return bodyRule;
    }
}
