/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.repository.gateway;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntityWrapper;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.zz.gateway.common.nacos.entity.route.RouteRuleEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store {@link RouteRuleEntity} in memory.
 *
 * @author Francis.zz
 */
@Component
public class InMemRouteRuleStore {
    private Map<String, RouteRuleEntity> allRules = new ConcurrentHashMap<>(16);
    
    private Map<String, Map<String, RouteRuleEntity>> appRules = new ConcurrentHashMap<>(16);
    
    public RouteRuleEntity save(RouteRuleEntity entity) {
        if (entity.getId() == null) {
            return null;
        }
        allRules.put(entity.getId(), entity);
        appRules.computeIfAbsent(entity.getApp(), v -> new ConcurrentHashMap<>(32))
                .put(entity.getId(), entity);
        
        return entity;
    }
    
    public List<RouteRuleEntity> saveAll(List<RouteRuleEntity> rules) {
        allRules.clear();
        appRules.clear();
        
        if (rules == null) {
            return null;
        }
        List<RouteRuleEntity> savedRules = new ArrayList<>(rules.size());
        for (RouteRuleEntity rule : rules) {
            savedRules.add(save(rule));
        }
        return savedRules;
    }
    
    public List<RouteRuleEntity> saveAll(RuleEntityWrapper<RouteRuleEntity> rules) {
        allRules.clear();
        appRules.clear();
        
        if (rules == null || rules.getRuleEntity() == null) {
            return null;
        }
        List<RouteRuleEntity> savedRules = new ArrayList<>(rules.getRuleEntity().size());
        for (RouteRuleEntity rule : rules.getRuleEntity()) {
            savedRules.add(save(rule));
        }
        return savedRules;
    }
    
    public RouteRuleEntity delete(String routeId) {
        RouteRuleEntity entity = allRules.remove(routeId);
        if (entity != null) {
            if (appRules.get(entity.getApp()) != null) {
                appRules.get(entity.getApp()).remove(routeId);
            }
        }
        return entity;
    }
    
    public RouteRuleEntity findById(String id) {
        return allRules.get(id);
    }
    
    public List<RouteRuleEntity> findAllByApp(String appName) {
        AssertUtil.notEmpty(appName, "appName cannot be empty");
        Map<String, RouteRuleEntity> entities = appRules.get(appName);
        if (entities == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entities.values());
    }
    
    public RuleEntityWrapper<RouteRuleEntity> findRuleByApp(String appName) {
        AssertUtil.notEmpty(appName, "appName cannot be empty");
        Map<String, RouteRuleEntity> entities = appRules.get(appName);
        if (entities == null) {
            return null;
        }
        return RuleEntityWrapper.of(null, new Date(), new ArrayList<>(entities.values()));
    }
    
    public RuleEntityWrapper refreshRuleCache(DynamicRuleProvider<RuleEntityWrapper<RouteRuleEntity>> ruleProvider, String app) {
        try {
            RuleEntityWrapper<RouteRuleEntity> rules = ruleProvider.getRules(app);
            saveAll(rules);
            return rules;
        } catch (Exception e) {
            return null;
        }
    }
    
    public void clearAll() {
        allRules.clear();
        appRules.clear();
    }
}
