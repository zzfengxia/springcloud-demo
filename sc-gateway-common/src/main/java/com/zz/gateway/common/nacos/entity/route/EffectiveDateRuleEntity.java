package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import com.zz.gateway.common.routedefine.predicaterule.EffectiveDateRule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;

/**
 * ************************************
 * create by Intellij IDEA
 * 生效时间规则
 *
 * @author Francis.zz
 * @date 2020-03-26 15:51
 * ************************************
 */
@Slf4j
public class EffectiveDateRuleEntity implements RuleCheck {
    /**
     * before和after同时存在的话，则会自动使用between规则
     */
    @Getter
    @Setter
    private String after;
    @Getter
    @Setter
    private String before;
    /**
     * predicate顺序，值越小越优先
     */
    @Setter
    private Integer order;
    
    private ZonedDateTime afterTime;
    private ZonedDateTime beforeTime;
    @Override
    public void validate() {
        try {
            if(StringUtils.isNotEmpty(before)) {
                beforeTime = ZonedDateTime.parse(before);
            }
            if(StringUtils.isNotEmpty(after)) {
                afterTime = ZonedDateTime.parse(after);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("DateRule must be ZonedDateTime, eg: 2020-03-31T13:56:28.082+08:00[Asia/Shanghai] ");
        }
        if(afterTime != null && beforeTime != null && afterTime.isBefore(beforeTime)) {
            throw new IllegalArgumentException("illegal params, [after] less then [before], so EffectiveDateRule config is invalid");
        }
    }
    
    public EffectiveDateRule toEffectiveDateRule() {
        EffectiveDateRule effectiveDateRule = new EffectiveDateRule();
        effectiveDateRule.setAfter(after);
        effectiveDateRule.setBefore(before);
        return effectiveDateRule;
    }
}
