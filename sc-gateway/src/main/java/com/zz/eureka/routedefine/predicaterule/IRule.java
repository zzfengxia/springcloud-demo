package com.zz.eureka.routedefine.predicaterule;

import org.springframework.core.Ordered;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:22
 * ************************************
 */
public interface IRule extends RuleCheck, GetPredicate, Ordered {
    @Override
    default boolean validate() {
        return true;
    }
}
