package com.zz.eureka.config.predicaterule;

/**
 * ************************************
 * create by Intellij IDEA
 * 规则验证接口，验证配置的规则是否有效
 * 只有实现该接口的规则才会生效
 *
 * @author Francis.zz
 * @date 2020-03-26 17:30
 * ************************************
 */
@FunctionalInterface
public interface RuleCheck {
    boolean validate();
}
