package com.zz.scgatewaynew.routedefine.filterrule;

import com.zz.scgatewaynew.routedefine.predicaterule.RuleCheck;
import org.springframework.core.Ordered;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:22
 * ************************************
 */
public interface IFilter extends RuleCheck, GetFilter, Ordered {
    @Override
    default boolean validate() {
        return true;
    }
}