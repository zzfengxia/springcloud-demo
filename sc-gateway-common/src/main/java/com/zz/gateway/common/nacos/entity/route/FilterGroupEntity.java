package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class FilterGroupEntity implements RuleCheck {
    private PathFilterEntity pathFilter;
    private HeaderFilterEntity headerFilter;
    
    private Integer order;
    
    
}
