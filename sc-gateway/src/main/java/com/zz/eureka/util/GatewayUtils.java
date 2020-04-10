package com.zz.eureka.util;

import com.zz.eureka.common.BizConstans;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-08 17:19
 * ************************************
 */
public class GatewayUtils {
    /**
     * 从ServerWebExchange缓存中提取traceId,如果没有只保存一个新值
     * 参考ServerWebExchange.LOG_ID_ATTRIBUTE
     *
     * @param exchange
     * @return
     */
    public static String getTraceIdFromCache(ServerWebExchange exchange) {
        String traceId = exchange.getAttribute(BizConstans.MDC_TRACE_ID);
        if(StringUtils.isEmpty(traceId)) {
            traceId = UuidUtils.generateUuid();
            exchange.getAttributes().put(BizConstans.MDC_TRACE_ID, traceId);
        }
        
        return traceId;
    }
    
    public static String formatRequest(ServerHttpRequest request) {
        String rawQuery = request.getURI().getRawQuery();
        String query = StringUtils.isNotBlank(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.getMethodValue() + " \"" + request.getPath() + query + "\"";
    }
}
