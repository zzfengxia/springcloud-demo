package com.zz.scgateway.routedefine.predicaterule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.core.OrderComparator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private BodyRule bodyRule = BodyRule.empty();
    private PathRule pathRule;
    private HeaderRule headerRule;
    private EffectiveDateRule dateRule;
    private QueryRule queryRule;
    
    private int order;
    
    /**
     * Predicate AND操作是将predicate分为left和right,只有最后加入的predicate会放到right. 断言规则是先断言left,如果left失败则不会再执行right的predicate。而且left断言也是遵从“&&”的语法规则
     * 因此可以为 PredicateGroup 的属性排序来实现predicate的执行先后问题
     *
     * @see {@link org.springframework.cloud.gateway.handler.AsyncPredicate.AndAsyncPredicate}
     * @param predicateSpec
     * @return
     */
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicate(predicateSpec, null);
    }
    
    public BooleanSpec predicate(PredicateSpec predicateSpec, PredicateGroup other) {
        try {
            List<IRule> ruleList = getValidRule(this);
            
            if(other != null) {
                ruleList.addAll(getValidRule(other));
            }
            // 排序
            OrderComparator.sort(ruleList);
            
            BooleanSpec booleanSpec = null;
            for (IRule rule : ruleList) {
                // and操作是将predicate分为left和right,只有最后加入的predicate会放到right. 断言规则是先断言left,如果left失败则不会再执行right的predicate
                if(booleanSpec != null) {
                    predicateSpec = booleanSpec.and();
                }
                booleanSpec = rule.predicate(predicateSpec);
            }
        
            return booleanSpec;
        } catch (Exception e) {
            log.error("predicate config of route assemble error", e);
            throw new IllegalArgumentException("predicate config of route assemble error");
        }
    }
    
    private static List<IRule> getValidRule(PredicateGroup predicateGroup) {
        Field[] curField = predicateGroup.getClass().getDeclaredFields();
        return Arrays.stream(curField).filter(f -> {
            try {
                IRule rule = null;
                return !Modifier.isStatic(f.getModifiers())
                        && IRule.class.isAssignableFrom(f.getType())
                        && (rule = (IRule) f.get(predicateGroup)) != null
                        && rule.validate();
            } catch (IllegalAccessException e) {
                return false;
            }
        }).map(f -> {
            try {
                return (IRule) f.get(predicateGroup);
            } catch (IllegalAccessException e) {
                return null;
            }
        }).collect(Collectors.toList());
    }
}
