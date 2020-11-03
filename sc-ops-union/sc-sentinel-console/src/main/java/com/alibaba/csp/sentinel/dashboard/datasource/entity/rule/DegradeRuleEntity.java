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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import java.util.Date;

/**
 * @author leyou
 */
public class DegradeRuleEntity implements RuleEntity {
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
    private Double slowRatioThreshold;
    private Integer statIntervalMs;
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
        entity.setSlowRatioThreshold(rule.getSlowRatioThreshold());
        entity.setStatIntervalMs(rule.getStatIntervalMs());
        return entity;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getLimitApp() {
        return limitApp;
    }

    public void setLimitApp(String limitApp) {
        this.limitApp = limitApp;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }
    
    public Integer getSlowRt() {
        return slowRt;
    }
    
    public void setSlowRt(Integer slowRt) {
        this.slowRt = slowRt;
    }
    
    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
    
    public Integer getStatisticsTimeWindow() {
        return statisticsTimeWindow;
    }
    
    public void setStatisticsTimeWindow(Integer statisticsTimeWindow) {
        this.statisticsTimeWindow = statisticsTimeWindow;
    }
    
    public Integer getIntervalUnit() {
        return intervalUnit;
    }
    
    public void setIntervalUnit(Integer intervalUnit) {
        this.intervalUnit = intervalUnit;
    }
    
    public Integer getMinRequestAmount() {
        return minRequestAmount;
    }
    
    public void setMinRequestAmount(Integer minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
    }
    
    public Double getSlowRatioThreshold() {
        return slowRatioThreshold;
    }
    
    public void setSlowRatioThreshold(Double slowRatioThreshold) {
        this.slowRatioThreshold = slowRatioThreshold;
    }
    
    public Integer getStatIntervalMs() {
        return statIntervalMs;
    }
    
    public void setStatIntervalMs(Integer statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
    }
    
    @Override
    public DegradeRule toRule() {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setLimitApp(limitApp);
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        rule.setGrade(grade);
        rule.setStatisticsTimeWindow(calIntervalSec(statisticsTimeWindow, intervalUnit));
        rule.setSlowRt(slowRt);
        if (minRequestAmount != null) {
            rule.setMinRequestAmount(minRequestAmount);
        }
        if (slowRatioThreshold != null) {
            rule.setSlowRatioThreshold(slowRatioThreshold);
        }
        if (statIntervalMs != null) {
            rule.setStatIntervalMs(statIntervalMs);
        }
        return rule;
    }
}
