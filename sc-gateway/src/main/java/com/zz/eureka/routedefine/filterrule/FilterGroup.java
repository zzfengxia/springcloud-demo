package com.zz.eureka.routedefine.filterrule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
    
    @Override
    public UriSpec filter(GatewayFilterSpec filterSpec) {
        try {
            Field[] allField = this.getClass().getDeclaredFields();
            UriSpec uriSpec = null;
            for (Field field : allField) {
                if(Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (!IFilter.class.isAssignableFrom(field.getType())) {
                    log.warn("field:{} is not extend IFilter, so is invalid", field.getName());
                    continue;
                }
                IFilter filter = (IFilter) field.get(this);
                if(filter == null) {
                    continue;
                }
                if(!filter.validate()) {
                    log.warn("field:{} valid fail", field.getName());
                    continue;
                }
                uriSpec = filter.filter(filterSpec);
            }
        
            return uriSpec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
