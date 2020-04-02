package com.zz.eureka.config.predicaterule;

import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:01
 * ************************************
 */
@Data
public class PathRule implements IRule {
    private String[] path;
    
    public PathRule(String text) {
        this.path = text.split(",");
    }
    
    public PathRule() {
    }
    
    @Override
    public boolean validate() {
        return true;
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.path(path);
    }
}
