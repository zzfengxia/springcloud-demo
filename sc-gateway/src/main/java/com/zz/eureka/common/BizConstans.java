package com.zz.eureka.common;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-08 14:24
 * ************************************
 */
public final class BizConstans {
    /**
     * 日志追踪ID
     */
    public final static String MDC_TRACE_ID = "sessionId";
    /**
     * 请求执行开始时间
     */
    public final static String REQUEST_START_TIME = "startExecTime";
    /**
     * 流控标识 “true”时会被计数，请求头全小写
     */
    public final static String FLOW_CTRL_FLAG = "flowctrlflag";
    
    /**
     * 和华为交互，请求头中的签名参数，Signature-Type
     */
    public static final String SIGNATURE_TYPE = "signature-type";
    /**
     * 和华为交互，请求头中的签名参数，Signature-Value
     */
    public static final String SIGNATURE_VALUE = "signature-value";
}
