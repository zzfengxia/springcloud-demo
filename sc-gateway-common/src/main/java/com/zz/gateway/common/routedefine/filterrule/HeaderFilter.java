package com.zz.gateway.common.routedefine.filterrule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 14:28
 * ************************************
 */
@Setter
@Getter
public class HeaderFilter implements IFilter {
    private Map<String, String> headers;
    /**
     * filter顺序，值越小越优先
     */
    private int order;
    @Override
    public UriSpec filter(GatewayFilterSpec filterSpec) {
        headers.forEach(filterSpec::addRequestHeader);
        return filterSpec;
    }
}
