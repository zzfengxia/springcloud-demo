/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Sentinel dashboard application.
 *
 * @author Carpenter Lee
 */
@SpringBootApplication
public class DashboardApplication {
    
    /**
     * spring.cloud.sentinel.transport.port 配置的端口会在{@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}中用来开启socket监听服务.
     * 然后使用{@link com.alibaba.csp.sentinel.transport.command.http.HttpEventTask} 类做实际的处理操作。
     * {@link com.alibaba.csp.sentinel.command.annotation.CommandMapping}注解用来注册接口实际处理逻辑。
     * 比如sentinel console实时监控界面获取metric信息的请求<code>metric</code>就是sentinel客户端的{@link com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler}类处理的。
     * 实际metric数据来源就是从sentinel的metric日志文件中读取的。
     *
     * triggerSentinelInit初始化InitFunc接口的实现类
     */
    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

    @Value("${csp.sentinel.log.dir:}")
    private String baseLogDir;
    
    @Value("${project.name:}")
    private String projectName;
    
    @Value("${csp.sentinel.dashboard.server:}")
    private String consoleServer;
    @Value("${csp.sentinel.api.port:}")
    private String serverPort;
    // 初始化InitFunc接口的所有实现类
    private static void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }
    
    /**
     * sentinel-client日志打印配置：通过yml配置属性`csp.sentinel.log.dir`注入到系统ENV或者直接在启动命令行配置`-Dcsp.sentinel.log.dir`参数
     *
     */
    @PostConstruct
    private void init() {
        if (StringUtils.isEmpty(System.getProperty(LogBase.LOG_DIR))
                && StringUtils.hasText(baseLogDir)) {
            System.setProperty(LogBase.LOG_DIR, baseLogDir);
        }
    
        if (StringUtils.isEmpty(System.getProperty(SentinelConfig.APP_NAME_PROP_KEY))
                && StringUtils.hasText(projectName)) {
            System.setProperty(SentinelConfig.APP_NAME_PROP_KEY, projectName);
        }
    
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))
                && StringUtils.hasText(consoleServer)) {
            System.setProperty(TransportConfig.CONSOLE_SERVER, consoleServer);
        }
    
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))
                && StringUtils.hasText(serverPort)) {
            System.setProperty(TransportConfig.SERVER_PORT, serverPort);
        }
    
        triggerSentinelInit();
    }
}
