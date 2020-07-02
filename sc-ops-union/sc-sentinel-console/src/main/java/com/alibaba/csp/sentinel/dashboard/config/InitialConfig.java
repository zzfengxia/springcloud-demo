package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-16 17:25
 * ************************************
 */
@Configuration
public class InitialConfig {
    @Value("${csp.sentinel.log.dir:}")
    private static String baseLogDir;
    
    @Value("${project.name:}")
    private static String projectName;
    
    @Value("${csp.sentinel.dashboard.server:}")
    private static String consoleServer;
    @Value("${csp.sentinel.api.port:}")
    private static String serverPort;
    
    
}
