package com.zz.scgatewaynew.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-21 16:59
 * ************************************
 */
@ConfigurationProperties(prefix = "config.dict")
@Component
@Setter
@Getter
//@RefreshScope
public class CardCodeDict {
    /**
     * nacos刷新存在BUG，当key删掉时，bean不会刷新.
     */
    private Map<String, String> cardCodeDict;
}
