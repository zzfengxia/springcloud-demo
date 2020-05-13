package com.zz.sccommon.common;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-11 10:00
 * ************************************
 */
public class FeignDataThreadLocal {
    private static ThreadLocal<RequestExtParams> dataLocal = new ThreadLocal<>();
    
    public static void set(RequestExtParams data) {
        dataLocal.set(data);
    }
    
    public static RequestExtParams get() {
        RequestExtParams data = dataLocal.get();
        dataLocal.remove();
        return data;
    }
}
