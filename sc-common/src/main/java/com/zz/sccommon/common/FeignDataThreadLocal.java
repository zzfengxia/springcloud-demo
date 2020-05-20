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
    private static InheritableThreadLocal<RequestExtParams> dataLocal = new InheritableThreadLocal<>();
    
    public static void set(RequestExtParams data) {
        dataLocal.set(data);
    }
    
    public static RequestExtParams get() {
        RequestExtParams data = dataLocal.get();
        return data;
    }
    
    /**
     * 调用set后一定要显示调用remove方法，防止线程池数据脏读
     */
    public static void remove() {
        dataLocal.remove();
    }
}
