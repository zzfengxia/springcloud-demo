package com.zz.gateway.common.routedefine.predicaterule;

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
    
    /**
     * predicate顺序，值越小越优先
     */
    private int order;
    
    /**
     * 请求头匹配
     *
     * @param predicateSpec
     * @return
     */
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.header(headerName, headerValue);
    }
}
