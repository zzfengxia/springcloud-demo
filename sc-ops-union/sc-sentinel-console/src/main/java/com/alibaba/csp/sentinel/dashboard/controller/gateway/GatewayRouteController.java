package com.alibaba.csp.sentinel.dashboard.controller.gateway;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.RouteInfoEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntityWrapper;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemRouteRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.zz.gateway.common.nacos.entity.route.RouteRuleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-09 17:38
 * ************************************
 */
@RestController
@RequestMapping(value = "/gateway/route")
public class GatewayRouteController {
    
    private final Logger logger = LoggerFactory.getLogger(GatewayRouteController.class);
    
    @Autowired
    @Qualifier("gatewayRouteRuleNacosProvider")
    private DynamicRuleProvider<RuleEntityWrapper<RouteRuleEntity>> gatewayRouteNacosProvider;
    @Autowired
    @Qualifier("gatewayRouteRuleNacosPublisher")
    private DynamicRulePublisher<RuleEntityWrapper<RouteRuleEntity>> gatewayRouteNacosPublisher;
    @Autowired
    private SentinelApiClient sentinelApiClient;
    
    @Autowired
    private InMemRouteRuleStore repository;
    
    /**
     * 网关服务实例所有有效路由
     *
     * @param app
     * @param ip
     * @param port
     * @return
     */
    @GetMapping("local/list.json")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<RouteInfoEntity>> queryRoutes(String app, String ip, Integer port) {
        
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        
        try {
            List<RouteInfoEntity> routes = sentinelApiClient.fetchRoutes(app, ip, port).get();
            return Result.ofSuccess(routes);
        } catch (Throwable throwable) {
            logger.error("queryRoutes error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }
    
    /**
     * nacos配置中心的路由配置信息
     *
     * @param app
     * @return
     */
    @GetMapping("list.json")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<RouteRuleEntity>> queryRoutes(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
    
        try {
            RuleEntityWrapper<RouteRuleEntity> rules = gatewayRouteNacosProvider.getRules(app);
            if (rules != null && !rules.getRuleEntity().isEmpty()) {
                for (RouteRuleEntity entity : rules.getRuleEntity()) {
                    entity.setApp(app);
                }
            }
            List<RouteRuleEntity> result = repository.saveAll(rules);
            return Result.ofSuccess(result);
        } catch (Throwable throwable) {
            logger.error("query gateway flow rules error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }
    
    @PostMapping("/new.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<RouteRuleEntity> addRoute(@RequestBody RouteRuleEntity reqVo) {
        
        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try{
            reqVo.validatePlus();
        } catch (Exception e) {
            logger.error("invalid route, " + e.getMessage());
            return Result.ofFail(-1, e.getMessage());
        }
        // 先刷新缓存
        repository.refreshRuleCache(gatewayRouteNacosProvider, reqVo.getApp());
        
        if(repository.findById(reqVo.getId()) != null) {
            return Result.ofFail(-1, "route id existed");
        }
        
        Date date = new Date();
        reqVo.setCreateTime(date);
        reqVo.setModifyTime(date);
        
        try {
            reqVo = repository.save(reqVo);
            publishApis(app);
        } catch (Throwable throwable) {
            logger.error("add gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        
        return Result.ofSuccess(reqVo);
    }
    
    @PostMapping("/save.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<RouteRuleEntity> saveRoute(@RequestBody RouteRuleEntity reqVo) {
        
        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try{
            reqVo.validatePlus();
        } catch (Exception e) {
            logger.error("invalid route, " + e.getMessage());
            return Result.ofFail(-1, e.getMessage());
        }
        
        if(repository.findById(reqVo.getId()) == null) {
            return Result.ofFail(-1, "route id not existed");
        }
        
        Date date = new Date();
        reqVo.setModifyTime(date);
        
        try {
            reqVo = repository.save(reqVo);
            publishApis(app);
        } catch (Throwable throwable) {
            logger.error("add gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        
        return Result.ofSuccess(reqVo);
    }
    
    @PostMapping("/delete.json")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<String> deleteRoute(String id) {
        if (id == null) {
            return Result.ofFail(-1, "route id can't be null");
        }
        
        RouteRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        
        try {
            repository.delete(id);
            publishApis(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("delete gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        
        return Result.ofSuccess(id);
    }
    
    private void publishApis(String app) throws Exception {
        RuleEntityWrapper<RouteRuleEntity> rules = repository.findRuleByApp(app);
        gatewayRouteNacosPublisher.publish(app, rules);
    }
}
