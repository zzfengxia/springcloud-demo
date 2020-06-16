package com.zz.gateway.common.nacos.entity.route;

import com.alibaba.fastjson.annotation.JSONField;
import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 11:24
 * ************************************
 */
@Data
@Slf4j
public class RouteRuleEntity implements RuleCheck {
    private String app;
    private Date createTime;
    private Date modifyTime;
    
    private String id;
    private PredicateGroupEntity predicate;
    private FilterGroupEntity filter;
    /**
     * 访问地址
     * eg： http://ip:port/contextPath
     */
    private String uri;
    /**
     * 路由规则优先级，值越小优先级越高
     */
    private Integer order;
    
    public void validatePlus() {
        if(id == null || uri == null) {
            throw new IllegalArgumentException("[id, uri] must be not null");
        }
    
        if(predicate == null) {
            throw new IllegalArgumentException("args[predicate] must be not null");
        }
        
        this.validate();
    }
    
    @JSONField(serialize = false)
    public boolean isValid() {
        try {
            this.validatePlus();
        } catch (IllegalArgumentException e) {
            log.error("route rule[" + this.id + "] is invalid, " + e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("route rule[" + this.id + "] is invalid, " + e);
            return false;
        }
        
        return true;
    }
}
