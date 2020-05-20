package com.zz.scorder.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.fastjson.JSON;
import com.zz.api.common.sentinelfeign.CustomSentinelFeign;
import feign.Feign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerMapping;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-14 18:00
 * ************************************
 */
@Configuration
@Slf4j
public class SentinelConfig {
    /**
     * 自定义BlockExceptionHandler.定制限流响应
     *
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebMvcConfig} 注入BlockExceptionHandler
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebInterceptor}注入异常处理拦截器，拦截并处理BlockException异常
     *
     * 可以通过spring.cloud.setinel.blockPage 设置异常响应重定向的页面
     * 否则默认使用{@link com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler}类处理
     * DegradeException、FlowException都是继承于BlockException，所以当feign接口没有指定fallBack实现时，限流，降级情况下都会执行这里的代码
     *
     * @return
     */
    @Bean
    public BlockExceptionHandler customBlockExceptionHandler() {
        return ((request, response, e) -> {
            log.info("请求[" + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) + "]被限流");
            response.setStatus(200);
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter out = response.getWriter();
            Map<String, String> msg = new HashMap<>();
            msg.put("code", "429");
            msg.put("message", "请求已被限流");
            out.print(JSON.toJSONString(msg));
            out.flush();
            out.close();
        });
    }
    
    /**
     * 创建定制的Sentinel Feign Builder
     *
     * @return
     */
    @Bean
    @Scope("prototype")
    @Primary
    @ConditionalOnProperty(name = "feign.sentinel.enabled")
    public Feign.Builder customeFeignSentinelBuilder() {
        return CustomSentinelFeign.builder();
    }
}
