package com.zz.scgatewaynew.util;

import com.alibaba.fastjson.JSON;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.UuidUtils;
import com.zz.sccommon.util.sign.RSASignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-08 17:19
 * ************************************
 */
@Slf4j
public class GatewayUtils {
    /**
     * 该值与{@link ReadBodyPredicateFactory}类中的CACHE_REQUEST_BODY_OBJECT_KEY一致
     */
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    
    /**
     * 从ServerWebExchange缓存中提取traceId,如果没有只保存一个新值
     * 参考ServerWebExchange.LOG_ID_ATTRIBUTE
     *
     * @param exchange
     * @return
     */
    public static String getTraceIdFromCache(ServerWebExchange exchange) {
        String traceId = exchange.getAttribute(BizConstants.MDC_TRACE_ID);
        if(StringUtils.isEmpty(traceId)) {
            traceId = UuidUtils.generateUuid();
            exchange.getAttributes().put(BizConstants.MDC_TRACE_ID, traceId);
        }
        
        return traceId;
    }
    
    public static String formatRequest(ServerHttpRequest request) {
        String rawQuery = request.getURI().getRawQuery();
        String query = StringUtils.isNotBlank(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.getMethodValue() + " \"" + request.getPath() + query + "\"";
    }
    
    /**
     * 判断请求头如果有签名信息，则把响应数据加签后放入响应头
     *
     * @param exchange
     * @param body
     * @param privateKeyStr
     * @return flag 请求头签名标识
     */
    public static boolean wrapRespHeaderWithSign(ServerWebExchange exchange, Map<String, Object> body, String privateKeyStr) {
        boolean flag = false;
        String requestHeaderSignValue = exchange.getRequest().getHeaders().getFirst(BizConstants.SIGNATURE_VALUE);
        if (requestHeaderSignValue != null) {
            exchange.getResponse().getHeaders().add(BizConstants.SIGNATURE_TYPE, RSASignatureUtil.SIGN_ALGORITHMS_SHA256);
            exchange.getResponse().getHeaders().add(BizConstants.SIGNATURE_VALUE,
                    RSASignatureUtil.sign(JSON.toJSONString(body), privateKeyStr, "UTF-8", RSASignatureUtil.SIGN_ALGORITHMS_SHA256));
    
            flag = true;
        }
        return flag;
    }
    
    /**
     * 获取request body, 默认POST请求都已调用readBody predicate
     * todo 自定义方法只有在filter中生效，还没找到在外层获取body的方法。
     *
     * @param exchange
     * @return
     */
    public static Object fetchBody(ReadBodyPredicateFactory readBodyPredicateFactory, ServerWebExchange exchange) {
        if(!HttpMethod.POST.matches(exchange.getRequest().getMethodValue())) {
            return null;
        }
        
        return exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
    }
    
    /**
     * 校验请求是否为json格式
     *
     * @param contentType
     * @return
     */
    public static boolean isJson(MediaType contentType) {
        return MediaType.APPLICATION_JSON.equals(contentType) || MediaType.APPLICATION_JSON_UTF8.equals(contentType);
    }
}
