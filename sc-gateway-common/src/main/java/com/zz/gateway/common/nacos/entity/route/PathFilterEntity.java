package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Data;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 09:49
 * ************************************
 */
@Data
public class PathFilterEntity implements RuleCheck {
    /**
     * 具体接口访问path，不要uri
     * eg： query
     */
    private String path;
    /**
     * filter顺序，值越小越优先.
     */
    private Integer order;
}
