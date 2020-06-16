package com.zz.gateway.common.routedefine.filterrule;

import lombok.Data;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 09:49
 * ************************************
 */
@Data
public class PathFilter implements IFilter {
    /**
     * 具体接口访问path，不要uri
     * eg： query
     */
    private String path;
    /**
     * filter顺序，值越小越优先.
     */
    private int order;
    
    @Override
    public UriSpec filter(GatewayFilterSpec filterSpec) {
        return filterSpec.setPath(path);
    }
}
