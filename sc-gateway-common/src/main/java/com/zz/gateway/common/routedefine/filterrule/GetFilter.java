package com.zz.gateway.common.routedefine.filterrule;

import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-27 16:20
 * ************************************
 */
@FunctionalInterface
public interface GetFilter {
    UriSpec filter(GatewayFilterSpec filterSpec);
}
