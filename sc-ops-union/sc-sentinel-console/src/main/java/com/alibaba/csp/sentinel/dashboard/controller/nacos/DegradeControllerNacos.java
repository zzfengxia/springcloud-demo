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
package com.alibaba.csp.sentinel.dashboard.controller.nacos;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntityWrapper;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Francis.zz
 */
@Controller
@RequestMapping(value = "/v2/degrade", produces = MediaType.APPLICATION_JSON_VALUE)
public class DegradeControllerNacos {

    private final Logger logger = LoggerFactory.getLogger(DegradeControllerNacos.class);
    
    @Autowired
    @Qualifier("degradeRuleNacosProvider")
    private DynamicRuleProvider<RuleEntityWrapper<DegradeRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("degradeRuleNacosPublisher")
    private DynamicRulePublisher<RuleEntityWrapper<DegradeRuleEntity>> rulePublisher;
    @Autowired
    private InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository;
    
    @ResponseBody
    @RequestMapping("/rules.json")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<DegradeRuleEntity>> queryMachineRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            RuleEntityWrapper<DegradeRuleEntity> ruleEntity = ruleProvider.getRules(app);
            if (ruleEntity != null && !ruleEntity.getRuleEntity().isEmpty()) {
                for (DegradeRuleEntity entity : ruleEntity.getRuleEntity()) {
                    entity.setApp(app);
                }
            }
            List<DegradeRuleEntity> rules = repository.saveAll(ruleEntity);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @ResponseBody
    @RequestMapping("/new.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<DegradeRuleEntity> add(String app, String limitApp, String resource,
                                         Double count, Integer timeWindow, Integer grade, Integer statisticsTimeWindow,
                                         Integer minRequestAmount, Integer slowRt, Integer intervalUnit) {
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(limitApp)) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(resource)) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (count == null) {
            return Result.ofFail(-1, "count can't be null");
        }
        if (timeWindow == null) {
            return Result.ofFail(-1, "timeWindow can't be null");
        }
        if (grade == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (statisticsTimeWindow == null) {
            return Result.ofFail(-1, "statisticsTimeWindow can't be null");
        }
        // 间隔单位
        if (intervalUnit == null) {
            return Result.ofFail(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(DegradeRuleEntity.INTERVAL_UNIT_SECOND, DegradeRuleEntity.INTERVAL_UNIT_MINUTE).contains(intervalUnit)) {
            return Result.ofFail(-1, "Invalid intervalUnit: " + intervalUnit);
        }
        if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
            return Result.ofFail(-1, "Invalid grade: " + grade);
        }
        DegradeRuleEntity entity = new DegradeRuleEntity();
        entity.setApp(app.trim());
        entity.setLimitApp(limitApp.trim());
        entity.setResource(resource.trim());
        entity.setCount(count);
        entity.setTimeWindow(timeWindow);
        entity.setGrade(grade);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setStatisticsTimeWindow(statisticsTimeWindow);
        entity.setIntervalUnit(intervalUnit);
        entity.setMinRequestAmount(minRequestAmount);
        entity.setSlowRt(slowRt);
        try {
            // 先刷新缓存
            repository.refreshRuleCache(ruleProvider, entity.getApp());
            entity = repository.save(entity);
            publishRules(app);
        } catch (Throwable throwable) {
            logger.error("add degrade error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @ResponseBody
    @RequestMapping("/save.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<DegradeRuleEntity> updateIfNotNull(Long id, String app, String limitApp, String resource,
                                                     Double count, Integer timeWindow, Integer grade, Integer statisticsTimeWindow,
                                                     Integer minRequestAmount, Integer slowRt, Integer intervalUnit) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        if (grade != null) {
            if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
                return Result.ofFail(-1, "Invalid grade: " + grade);
            }
        }
        DegradeRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }

        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }

        if (StringUtil.isNotBlank(limitApp)) {
            entity.setLimitApp(limitApp.trim());
        }
        if (StringUtil.isNotBlank(resource)) {
            entity.setResource(resource.trim());
        }
        if (count != null) {
            entity.setCount(count);
        }
        if (timeWindow != null) {
            entity.setTimeWindow(timeWindow);
        }
        if (grade != null) {
            entity.setGrade(grade);
        }
        if (statisticsTimeWindow != null) {
            entity.setStatisticsTimeWindow(statisticsTimeWindow);
        }
        // 间隔单位
        if (intervalUnit != null) {
            if (!Arrays.asList(DegradeRuleEntity.INTERVAL_UNIT_SECOND, DegradeRuleEntity.INTERVAL_UNIT_MINUTE).contains(intervalUnit)) {
                return Result.ofFail(-1, "Invalid intervalUnit: " + intervalUnit);
            }
            entity.setIntervalUnit(intervalUnit);
        }
        if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
            return Result.ofFail(-1, "Invalid grade: " + grade);
        }
        if (minRequestAmount != null) {
            entity.setMinRequestAmount(minRequestAmount);
        }
        if (slowRt != null) {
            entity.setSlowRt(slowRt);
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("save degrade error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @ResponseBody
    @RequestMapping("/delete.json")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<Long> delete(Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        DegradeRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("delete degrade error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(String app) throws Exception {
        RuleEntityWrapper<DegradeRuleEntity> rules = repository.findRuleByApp(app);
        rulePublisher.publish(app, rules);
    }
}
