package com.zz.api.common.config;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.zz.api.common.mybatis.MybatisMapperMonitorPlugin;
import com.zz.api.common.mybatis.MybatisSqlMonitorPlugin;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-21 15:32
 * ************************************
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.sentinel.mybatis.enabled", matchIfMissing = true)
@ConditionalOnClass(name = "org.apache.ibatis.plugin.Interceptor")
@AutoConfigureAfter({SentinelAutoConfiguration.class})
public class SentinelMybatisConfig {
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.sentinel.mybatis.enable-sql-resource", matchIfMissing = false)
    @ConditionalOnMissingBean
    public MybatisSqlMonitorPlugin sentinelMyBatisSqlInterceptor() {
        RecordLog.info("Registering MybatisSqlMonitorPlugin as Spring bean");
        return new MybatisSqlMonitorPlugin();
    }
    
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.sentinel.mybatis.enable-mapper-resource", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MybatisMapperMonitorPlugin sentinelMyBatisMapperInterceptor() {
        RecordLog.info("Registering MybatisMapperMonitorPlugin as Spring bean");
        return new MybatisMapperMonitorPlugin();
    }
}
