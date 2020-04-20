package com.zz.scgateway.config;

import org.junit.Test;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;

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
    
        URI uri = URI.create("http://localhost:8083/mq/demo");
    }
}
