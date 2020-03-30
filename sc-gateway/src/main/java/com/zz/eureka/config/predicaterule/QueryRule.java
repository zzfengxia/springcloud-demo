package com.zz.eureka.config.predicaterule;

import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:32
 * ************************************
 */
@Data
public class QueryRule implements IRule {
    private String paramName;
    private String paramValue;
    
    private QueryRule or;
    private QueryRule and;
    
    @Override
    public boolean validate() {
        return true;
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return null;
    }
}
