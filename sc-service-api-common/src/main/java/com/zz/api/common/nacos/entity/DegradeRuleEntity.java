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
package com.zz.api.common.nacos.entity;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-04 15:45
 * ************************************
 */
@Setter
@Getter
public class DegradeRuleEntity {
    /**间隔单位*/
    /**0-秒*/
    public static final int INTERVAL_UNIT_SECOND = 0;
    /**1-分*/
    public static final int INTERVAL_UNIT_MINUTE = 1;
    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private String resource;
    private String limitApp;
    private Double count;
    private Integer timeWindow;
    private Integer minRequestAmount;
    /**
     * 统计窗口时长(秒)
     */
    private Integer statisticsTimeWindow;
    /**
     * 统计窗口单位
     */
    private Integer intervalUnit;
    /**
     * 慢响应时间(毫秒)
     */
    private Integer slowRt;
    /**
     * 0 rt 限流; 1为异常;
     */
    private Integer grade;
    private Date gmtCreate;
    private Date gmtModified;
    
    public static Integer calIntervalSec(Integer interval, Integer intervalUnit) {
        switch (intervalUnit) {
            case INTERVAL_UNIT_SECOND:
                return interval;
            case INTERVAL_UNIT_MINUTE:
                return interval * 60;
            default:
                break;
        }
        
        throw new IllegalArgumentException("Invalid intervalUnit: " + intervalUnit);
    }
    
    public static Object[] parseIntervalSec(Integer intervalSec) {
        if (intervalSec % 60 == 0) {
            return new Object[] {intervalSec / 60, INTERVAL_UNIT_MINUTE};
        }
        
        return new Object[] {intervalSec, INTERVAL_UNIT_SECOND};
    }
    
    public static DegradeRuleEntity fromDegradeRule(String app, String ip, Integer port, DegradeRule rule) {
        DegradeRuleEntity entity = new DegradeRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setResource(rule.getResource());
        entity.setLimitApp(rule.getLimitApp());
        entity.setCount(rule.getCount());
        entity.setTimeWindow(rule.getTimeWindow());
        entity.setGrade(rule.getGrade());
        Object[] intervalSecResult = parseIntervalSec(rule.getStatisticsTimeWindow());
        entity.setStatisticsTimeWindow((Integer) intervalSecResult[0]);
        entity.setIntervalUnit((Integer) intervalSecResult[1]);
        entity.setMinRequestAmount(rule.getMinRequestAmount());
        entity.setSlowRt(rule.getSlowRt());
        return entity;
    }
    
    public DegradeRule toRule() {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setLimitApp(limitApp);
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        rule.setGrade(grade);
        rule.setStatisticsTimeWindow(calIntervalSec(statisticsTimeWindow, intervalUnit));
        rule.setMinRequestAmount(minRequestAmount);
        rule.setSlowRt(slowRt);
        return rule;
    }
}
