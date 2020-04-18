package com.zz.eureka.routedefine;

import com.zz.eureka.routedefine.filterrule.FilterGroup;
import com.zz.eureka.routedefine.predicaterule.PredicateGroup;
import com.zz.eureka.routedefine.predicaterule.RuleCheck;
import lombok.Data;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import java.net.URI;
import java.util.function.Function;

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
    
    /**
     * 使用公共配置标识。为true并且公共配置存在时，会使用“与”逻辑合并公共配置。默认为 true
     */
    private boolean useCommonConfig = true;
    
    public void getRoute(RouteLocatorBuilder.Builder builder) {
        this.getRoute(builder, null);
    }
    
    public void getRoute(RouteLocatorBuilder.Builder builder, PredicateGroup commonPredicate) {
        if(!this.validate()) {
            return;
        }
        
        if(useCommonConfig) {
            if(predicate == null && commonPredicate == null) {
                return;
            }
            builder.route(id, p -> {
                BooleanSpec uriSpec = null;
                if(predicate != null) {
                    uriSpec = predicate.predicate(p, commonPredicate);
                } else {
                    uriSpec = commonPredicate.predicate(p);
                }
                if(filter != null) {
                    uriSpec.filters(f -> filter.filter(f));
                }
        
                return uriSpec.uri(URI.create(uri))
                        .order(order);
            });
        } else {
            if(predicate == null) {
                return;
            }
            builder.route(id, p -> {
                BooleanSpec uriSpec;
                
                uriSpec = predicate.predicate(p);
                if(filter != null) {
                    uriSpec.filters(f -> filter.filter(f));
                }
        
                return uriSpec.uri(URI.create(uri))
                        .order(order);
            });
        }
    }
    
    /**
     * 组装路由规则
     *
     * @param builder
     * @param commonPredicate 共用的predicate
     */
    public void getRouteWithCommon(RouteLocatorBuilder.Builder builder, Function<PredicateSpec, BooleanSpec> commonPredicate) {
        if(!this.validate()) {
            return;
        }
        
        if(predicate != null) {
            builder.route(id, p -> {
                BooleanSpec uriSpec;
                if(commonPredicate != null) {
                    uriSpec = commonPredicate.apply(p);
                    p = uriSpec.and();
                }
                uriSpec = predicate.predicate(p);
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
