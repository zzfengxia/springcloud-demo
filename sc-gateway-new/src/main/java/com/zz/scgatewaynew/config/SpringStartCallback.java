package com.zz.scgatewaynew.config;

import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 * springboot启动成功后的回调
 * {@link org.springframework.boot.SpringApplication#callRunners(ApplicationContext, ApplicationArguments)}
 *
 * @author Francis.zz
 * @date 2020-11-02 16:32
 * ************************************
 */
@Component
public class SpringStartCallback implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化InitFun和FlowRuleManager，解决由于延迟上报心跳（第一次路由后才会上报心跳）导致console总是会丢失第一次metric信息
        // 提前调用会导致无法初始化appType参数
        InitExecutor.doInit();
        // 先激活MetricTimerListener
        FlowRuleManager.getRules();
        System.out.println("启动成功...");
    }
}
