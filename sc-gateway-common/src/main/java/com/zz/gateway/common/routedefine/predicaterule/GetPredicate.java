package com.zz.gateway.common.routedefine.predicaterule;

import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:20
 * ************************************
 */
@FunctionalInterface
public interface GetPredicate {
    BooleanSpec predicate(PredicateSpec predicateSpec);
}
