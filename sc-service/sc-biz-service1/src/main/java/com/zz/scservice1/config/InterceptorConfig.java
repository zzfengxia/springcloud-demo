package com.zz.scservice1.config;

import com.zz.sccommon.interceptor.MDCLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ************************************
 * create by Intellij IDEA
 * 拦截器注册
 *
 * @author Francis.zz
 * @date 2019-12-20 11:55
 * ************************************
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MDCLogInterceptor())
                .addPathPatterns("/**");
    }
}
