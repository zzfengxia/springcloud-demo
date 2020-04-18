package com.zz.eureka.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SettingProp {
    @Value("${sptsm.private.key}")
    private String privateKeyStr;
}
