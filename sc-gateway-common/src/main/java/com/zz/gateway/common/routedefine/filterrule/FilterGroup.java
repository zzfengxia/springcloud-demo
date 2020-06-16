package com.zz.gateway.common.routedefine.filterrule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.core.OrderComparator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 11:17
 * ************************************
 */
@Setter
@Getter
@Slf4j
public class FilterGroup implements IFilter {
    private PathFilter pathFilter;
    private HeaderFilter headerFilter;
    
    private int order;
    
    /**
     * 使用GatewayFilterSpec里面参数为*FilterFactory创建的网关都是没有实现Ordered的，GatewayFilterSpec会默认用0包装。
     * 但是可以为FilterGroup创建出来的filter排序，实现动态配置的filter的执行先后功能
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler} 加载所有的filter
     *
     * @param filterSpec
     * @return
     */
    @Override
    public UriSpec filter(GatewayFilterSpec filterSpec) {
        return filter(filterSpec, null);
    }
    
    public UriSpec filter(GatewayFilterSpec filterSpec, FilterGroup other) {
        try {
            List<IFilter> filterList = getValidFilter(this);
    
            if(other != null) {
                filterList.addAll(getValidFilter(other));
            }
            // 排序
            OrderComparator.sort(filterList);
            
            UriSpec uriSpec = filterSpec;
            
            for (IFilter filter : filterList) {
                uriSpec = filter.filter(filterSpec);
            }
            
            return uriSpec;
        } catch (Exception e) {
            log.error("filter config of route assemble error", e);
            throw new IllegalArgumentException("filter config of route assemble error");
        }
    }
    
    private static List<IFilter> getValidFilter(FilterGroup filterGroup) {
        Field[] curField = filterGroup.getClass().getDeclaredFields();
        return Arrays.stream(curField).filter(f -> {
            try {
                return !Modifier.isStatic(f.getModifiers())
                        && IFilter.class.isAssignableFrom(f.getType())
                        && f.get(filterGroup) != null;
            } catch (IllegalAccessException e) {
                return false;
            }
        }).map(f -> {
            IFilter filter = null;
            try {
                filter = (IFilter) f.get(filterGroup);
            } catch (IllegalAccessException e) {
                return null;
            }
            filter.validate();
            return filter;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
