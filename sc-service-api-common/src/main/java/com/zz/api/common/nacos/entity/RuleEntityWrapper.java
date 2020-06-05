package com.zz.api.common.nacos.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-26 10:35
 * ************************************
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RuleEntityWrapper<T> {
    /**
     * 维护当前最新ID
     */
    private Long curId;
    private Date updateTime;
    private List<T> ruleEntity;
}
