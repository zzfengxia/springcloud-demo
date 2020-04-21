package com.zz.scgatewaynew.routedefine;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;

import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-02 17:41
 * ************************************
 */
public class CustomApiDefinition {
    private String apiName;
    private Set<ApiPathPredicateItem> pathPredicateItems;
    
    public String getApiName() {
        return apiName;
    }
    
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    
    public Set<ApiPathPredicateItem> getPredicateItems() {
        return pathPredicateItems;
    }
    
    public CustomApiDefinition setPredicateItems(Set<ApiPathPredicateItem> predicateItems) {
        this.pathPredicateItems = predicateItems;
        return this;
    }
}
