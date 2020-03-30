package com.zz.eureka.config.predicaterule;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:22
 * ************************************
 */
public interface IRule extends RuleCheck, GetPredicate {
    @Override
    default boolean validate() {
        return true;
    }
}
