package com.zz.gateway.common.routedefine.predicaterule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:32
 * ************************************
 */
@Data
@Slf4j
public class QueryRule implements IRule {
    public static final String OR = "or";
    public static final String AND = "and";
    
    /**
     * 属性map, 值可以为空字符串，则会检查query params是否存在key. value可以是正则表达式
     * queryMap: {version: (20200331|20200124)}
     */
    private Map<String, String> queryMap;
    
    private String strategy = AND;
    /**
     * predicate顺序，值越小越优先
     */
    private int order;
    @Override
    public void validate() {
        if(!AND.equals(strategy) && !OR.equals(strategy)) {
            throw new IllegalArgumentException("[strategy] must be or、and, so QueryRule config is invalid. now strategy:" + strategy);
        }
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        BooleanSpec result = null;
        for (Map.Entry<String, String> entry : queryMap.entrySet()) {
            String name = entry.getKey();
            String regex = entry.getValue();
    
            if(result != null) {
                if(AND.equals(strategy)) {
                    predicateSpec = result.and();
                } else {
                    predicateSpec = result.or();
                }
            }
            result = predicateSpec.query(name, regex);
        }
        
        return result;
    }
}
