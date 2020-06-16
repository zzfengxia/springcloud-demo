package com.zz.sccommon.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-06 11:13
 * ************************************
 */
public class ContextBeanUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
    
    public static <T> T getBean(Class<T> beanType) {
        return applicationContext.getBean(beanType);
    }
    
    public static boolean containsBean(String beanName) {
        return applicationContext.containsBean(beanName);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        ContextBeanUtil.applicationContext = applicationContext;
    }
}
