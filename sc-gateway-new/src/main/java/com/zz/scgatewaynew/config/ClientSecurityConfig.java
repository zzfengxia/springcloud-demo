package com.zz.scgatewaynew.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * ************************************
 * create by Intellij IDEA
 * spring cloud gateway网关要使用@EnableWebFluxSecurity配置Security
 * @author Francis.zz
 * @date 2020-04-28 11:23
 * ************************************
 */
@EnableWebFluxSecurity
public class ClientSecurityConfig {
    /**
     * 配置文件中的 management.endpoints.web.base-path 属性值
     * 这里只对 web 端点做登录验证
     */
    private final String endpointsBasePath;
    
    public ClientSecurityConfig(WebEndpointProperties webEndpointProperties) {
        this.endpointsBasePath = webEndpointProperties.getBasePath();
    }
    
    @Bean
    protected SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        return http.authorizeExchange()
                // web端点需要登录
                .pathMatchers(endpointsBasePath + "/**").authenticated()
                // 其他请求进行忽略
                .anyExchange().permitAll()
                // 使用 httpBasic 进行登录验证,这是 spring-boot-admin server 的验证方式
                .and().httpBasic()
                // 如果不配置 formLogin,不会重定向到登录页,因为不需要登录页面
                // .and().formLogin()
                // 忽略对端点的 csrf 验证,或者直接关闭 csrf,http.csrf().disable();
                .and()
                .csrf()
                //.ignoringAntMatchers(endpointsBasePath + "/**");
                .disable().build();
    }
}
