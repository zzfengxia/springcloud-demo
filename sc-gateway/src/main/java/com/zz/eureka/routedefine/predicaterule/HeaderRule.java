package com.zz.eureka.routedefine.predicaterule;

import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:21
 * ************************************
 */
@Data
public class HeaderRule implements IRule {
    private String headerName;
    private String headerValue;
    
    @Override
    public boolean validate() {
        return true;
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.header(headerName, headerValue);
    }
}
