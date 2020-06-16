package com.zz.gateway.common.routedefine.filterrule;

import com.zz.gateway.common.routedefine.RuleCheck;
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
    default void validate() {
        // no op
    }
}
