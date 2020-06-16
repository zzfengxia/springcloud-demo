package com.zz.scgatewaynew.transport;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.fastjson.JSON;
import com.zz.sccommon.util.ContextBeanUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 * 为sentinel-console提供的获取路由信息的接口
 *
 * @author Francis.zz
 * @date 2020-06-05 17:19
 * ************************************
 */
@CommandMapping(name = "gateway/getRouters", desc = "fetch all scg routers")
public class GetGatewayRouterCommandHandler implements CommandHandler<String> {
    private RouteLocator routeLocator;
    
    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        if(routeLocator == null) {
            routeLocator = ContextBeanUtil.getBean(RouteLocator.class);
        }
        if(routeLocator == null) {
            return CommandResponse.ofSuccess("");
        }
        List<Map<String, Object>> routeInfo = new ArrayList<>();
        routeLocator.getRoutes().map(this::serialize).subscribe(routeInfo::add);
        return CommandResponse.ofSuccess(JSON.toJSONString(routeInfo));
    }
    
    private Map<String, Object> serialize(Route route) {
        HashMap<String, Object> r = new HashMap<>();
        r.put("routeId", route.getId());
        r.put("uri", route.getUri().toString());
        r.put("order", route.getOrder());
        r.put("predicate", route.getPredicate().toString());
        if (!CollectionUtils.isEmpty(route.getMetadata())) {
            r.put("metadata", route.getMetadata());
        }
        
        ArrayList<String> filters = new ArrayList<>();
        
        for (int i = 0; i < route.getFilters().size(); i++) {
            GatewayFilter gatewayFilter = route.getFilters().get(i);
            filters.add(gatewayFilter.toString());
        }
        
        r.put("filters", filters);
        return r;
    }
}
