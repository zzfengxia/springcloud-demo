package com.zz.eureka.config;

import org.junit.Test;
import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-16 11:18
 * ************************************
 */
public class RouteDefinitionTest {
    @Test
    public void testParseRouteText() {
        String text = "route1=http://127.0.0.1,Host=**.addrequestparameter.org,Path=/get";
        RouteDefinition routeDefinition = new RouteDefinition(text);
        System.out.println(routeDefinition.toString());
    }
}
