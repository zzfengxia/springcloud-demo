package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 14:28
 * ************************************
 */
@Setter
@Getter
public class HeaderFilterEntity implements RuleCheck {
    private Map<String, String> headers;
    /**
     * filter顺序，值越小越优先
     */
    private Integer order;
}
