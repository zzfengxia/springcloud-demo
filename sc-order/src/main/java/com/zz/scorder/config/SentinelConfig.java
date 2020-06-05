package com.zz.scorder.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.api.common.nacos.entity.DegradeRuleEntity;
import com.zz.api.common.nacos.entity.FlowRuleEntity;
import com.zz.api.common.nacos.entity.RuleEntityWrapper;
import com.zz.api.common.sentinelfeign.CustomSentinelFeign;
import com.zz.sccommon.exception.ErrorCode;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            Map<String, String> msg = new HashMap<>();
            if(e instanceof FlowException) {
                log.info("请求[" + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) + "]被限流");
                
                msg.put("code", "429");
                msg.put("message", "请求已被限流");
                
            } else if(e instanceof DegradeException) {
                log.info("请求[" + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) + "]已熔断");
    
                msg.put("code", ErrorCode.SERVER_DEGRADE.getErrorCode());
                msg.put("message", ErrorCode.SERVER_DEGRADE.getReturnMsg());
            } else {
                log.info("请求[" + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) + "]被拦截, " + e.getClass().getSimpleName());
    
                msg.put("code", "500");
                msg.put("message", "error");
            }
            response.setStatus(200);
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter out = response.getWriter();
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
    
    /**
     * 注册流控配置转换器。nacos中的数据
     * beanName必须以sentinel-开头，后面字符串则为application.yml中的converter-class配置
     * <code>
     *     converter-class: flowRuleDecoder
     *     data-type: custom
     * </code>
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler#registerBean}
     * 参照 {@link com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfig}
     * @return
     */
    @Bean("sentinel-flowRuleDecoder")
    public Converter<String, List<FlowRule>> flowRuleEntityDecoder() {
        return s -> {
            RuleEntityWrapper<FlowRuleEntity> parseRule = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<FlowRuleEntity>>(){});
            return parseRule.getRuleEntity().stream().map(FlowRuleEntity::toRule).collect(Collectors.toList());
        };
    }
    
    @Bean("sentinel-degradeRuleDecoder")
    public Converter<String, List<DegradeRule>> degradeRuleDecoder() {
        return s -> {
            RuleEntityWrapper<DegradeRuleEntity> parseRule = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<DegradeRuleEntity>>(){});
            return parseRule.getRuleEntity().stream().map(DegradeRuleEntity::toRule).collect(Collectors.toList());
        };
    }
}
