package com.zz.eureka.config;

import com.zz.eureka.config.filterrule.FilterGroup;
import com.zz.eureka.config.predicaterule.PredicateGroup;
import com.zz.eureka.config.predicaterule.RuleCheck;
import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import java.net.URI;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 11:24
 * ************************************
 */
@Data
public class RouteRule implements RuleCheck {
    private String id;
    private PredicateGroup predicate;
    private FilterGroup filter;
    /**
     * 访问地址
     * eg： http://ip:port/contextPath
     */
    private String uri;
    /**
     * 路由规则优先级，值越小优先级越高
     */
    private int order;
    
    public void getRoute(RouteLocatorBuilder.Builder builder) {
        if(!this.validate()) {
            return;
        }
        if(predicate != null) {
            builder.route(id, p -> {
                BooleanSpec uriSpec = predicate.predicate(p);
                if(filter != null) {
                    uriSpec.filters(f -> filter.filter(f));
                }
    
                return uriSpec.uri(URI.create(uri))
                        .order(order);
            });
        }
    }
    
    @Override
    public boolean validate() {
        if(id == null || uri == null) {
            return false;
        }
        return true;
    }
}
