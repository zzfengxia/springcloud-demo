package com.zz.scgatewaynew.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Value("${private.key:}")
    private String privateKeyStr;
}
