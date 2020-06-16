package com.zz.gateway.common.routedefine.predicaterule;

import com.zz.gateway.common.GatewayConstants;
import com.zz.gateway.common.factory.CustomPathRoutePredicateFactory;
import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

import java.util.Arrays;

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
    /**
     * 配置网关路由异常时的响应策略,跟path绑定
     */
    private int respStrategy = GatewayConstants.SP_RESP_STRATEGY;
    
    /**
     * predicate顺序，值越小越优先. path应该优先级比其他predicate高，这样才能使respStrategy参数生效
     */
    private int order;
    
    public PathRule(String text) {
        this.path = text.split(",");
    }
    
    public PathRule() {
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.asyncPredicate(
                new CustomPathRoutePredicateFactory().applyAsync(c -> {
                    c.setPatterns(Arrays.asList(path));
                    c.setRespStrategy(respStrategy);
                })
        );
    }
}
