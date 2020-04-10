package com.zz.eureka.routedefine.filterrule;


import com.zz.eureka.routedefine.predicaterule.RuleCheck;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:22
 * ************************************
 */
public interface IFilter extends RuleCheck, GetFilter {
    @Override
    default boolean validate() {
        return true;
    }
}
