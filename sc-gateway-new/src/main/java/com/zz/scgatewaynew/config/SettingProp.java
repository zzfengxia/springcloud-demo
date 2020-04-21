package com.zz.scgatewaynew.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 16:42
 * ************************************
 */
@Component
@Getter
@Setter
public class SettingProp {
    @Value("${sptsm.private.key}")
    private String privateKeyStr;
    
    private Map<String, String> cardCodeDict;
    
    @ConfigurationProperties(prefix = "config.dict")
    public Map<String, String> getCardCodeDict() {
        return cardCodeDict;
    }
}
