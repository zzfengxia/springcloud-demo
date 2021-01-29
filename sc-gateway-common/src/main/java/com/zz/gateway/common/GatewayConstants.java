package com.zz.gateway.common;

/**
 * ************************************
 * create by Intellij IDEA
 * 网关服务专用常量
 *
 * @author Francis.zz
 * @date 2020-04-09 15:11
 * ************************************
 */
public class GatewayConstants {
    /**
     * 配置中心网关数据组
     */
    public static final String GROUP_GATEWAY = "config.gateway";
    /**
     * 动态路由配置的ID
     */
    public static final String DATA_ID_ROUTE = "gateway.route.rule.yaml";
    /**
     * 网关服务参数配置的ID
     */
    public static final String DATA_ID_SETTINGS = "gateway.setting.yaml";
    /**
     * sentinel网关流控配置ID
     */
    public static final String DATA_ID_FLOW = "gateway.sentinel.txt";
    
    /**
     * exchange参数-网关转发失败时给客户端的响应策略
     */
    public static final String FAIL_RESPONSE_STRATEGY = "failRespStrategy";
    
    /**
     * exchange参数-网关转发失败时给客户端的响应策略
     */
    public static final String CACHE_RESPONSE_BODY = "cacheResponseBody";
    
    /**
     * sp接口响应
     */
    public static final int SP_RESP_STRATEGY = 0;
    /**
     * 订单系统回调通知接口响应
     */
    public static final int ORDER_RESP_STRATEGY = 1;
    /**
     * 微信支付通知接口响应
     */
    public static final int WECHAT_RESP_STRATEGY = 2;
    
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    
    /**
     * sleuth日志追踪
     */
    public static final String TRACE_ID_NAME = "X-B3-TraceId";
    public static final String SPAN_ID_NAME = "X-B3-SpanId";
    
    /**
     * 记录未找到Route metric信息的resourceName
     * 在{@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}中会用到
     */
    public static final String RESOURCE_FOR_NOROUTE = "resource-no-route";
    
    public static final int PARAM_PARSE_STRATEGY_BODY = 5;
}
