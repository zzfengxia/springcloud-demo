package com.zz.gateway.common.routedefine.predicaterule;

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
public interface IRule extends RuleCheck, GetPredicate, Ordered {
}
