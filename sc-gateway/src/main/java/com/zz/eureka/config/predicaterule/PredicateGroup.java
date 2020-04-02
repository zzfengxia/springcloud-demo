package com.zz.eureka.config.predicaterule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

import java.lang.reflect.Field;

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
public class PredicateGroup implements IRule {
    private BodyRule bodyRule;
    private HeaderRule headerRule;
    private EffectiveDateRule dateRule;
    private PathRule pathRule;
    private QueryRule queryRule;
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        try {
            Field[] allField = this.getClass().getDeclaredFields();
            BooleanSpec booleanSpec = null;
            for (Field field : allField) {
                if (!IRule.class.isAssignableFrom(field.getType())) {
                    log.warn("field:{} is not extend IRule, so is invalid", field.getName());
                    continue;
                }
                IRule rule = (IRule) field.get(this);
                if(rule == null) {
                    continue;
                }
                if(!rule.validate()) {
                    log.warn("field:{} valid fail", field.getName());
                    continue;
                }
                if(booleanSpec != null) {
                    predicateSpec = booleanSpec.and();
                }
                booleanSpec = rule.predicate(predicateSpec);
            }
            
            return booleanSpec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
