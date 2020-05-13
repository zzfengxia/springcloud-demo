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

import com.alibaba.csp.sentinel.init.InitExecutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
     */
    public static void main(String[] args) {
        triggerSentinelInit();
        SpringApplication.run(DashboardApplication.class, args);
    }

    private static void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }
}
