package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Data;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:21
 * ************************************
 */
@Data
public class HeaderRuleEntity implements RuleCheck {
    private String headerName;
    private String headerValue;
    
    /**
     * predicate顺序，值越小越优先
     */
    private Integer order;
}
