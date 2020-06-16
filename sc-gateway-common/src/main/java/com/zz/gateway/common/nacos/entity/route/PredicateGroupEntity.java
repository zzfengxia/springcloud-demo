package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:08
 * ************************************
 */
@Setter
@Getter
@Slf4j
public class PredicateGroupEntity implements RuleCheck {
    private BodyRuleEntity bodyRule;
    private PathRuleEntity pathRule;
    private HeaderRuleEntity headerRule;
    private EffectiveDateRuleEntity dateRule;
    private QueryRuleEntity queryRule;
    
    private Integer order;
}
