package com.zz.gateway.common.routedefine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
public interface RuleCheck {
    default void validate() {
        Field[] curField = this.getClass().getDeclaredFields();
        for(Field f : curField) {
            RuleCheck rule = null;
            try {
                if(Modifier.isStatic(f.getModifiers())
                        || !RuleCheck.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                f.setAccessible(true);
                
                rule = f.get(this) != null ? (RuleCheck) f.get(this) : null;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if(rule != null) {
                rule.validate();
            }
        }
    }
}
