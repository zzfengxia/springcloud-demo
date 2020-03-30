package com.zz.eureka.config.predicaterule;

import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

import java.time.ZonedDateTime;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 15:51
 * ************************************
 */
@Data
public class InterfaceRule implements IRule {
    private String bodyAttrName = "command";
    private String interfaceName;
    
    private ZonedDateTime after;
    private ZonedDateTime before;
    private ZonedDateTime between;
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return null;
    }
}
