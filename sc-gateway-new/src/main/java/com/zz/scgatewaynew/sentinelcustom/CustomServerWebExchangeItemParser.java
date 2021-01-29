package com.zz.scgatewaynew.sentinelcustom;

import com.alibaba.csp.sentinel.adapter.gateway.sc.ServerWebExchangeItemParser;
import com.alibaba.fastjson.JSONObject;
import com.zz.gateway.common.GatewayConstants;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2021-01-28 17:59
 * ************************************
 */
public class CustomServerWebExchangeItemParser extends ServerWebExchangeItemParser {
    /**
     * 从请求的BODY体获取指定key的值，目前仅支持json格式格式
     * todo 解析body体的接口，自定义扩展
     *
     * @param request
     * @param key
     * @return
     */
    public String getBodyValue(ServerWebExchange request, String key) {
        Object reqBody = request.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);
        if(reqBody == null) {
            return null;
        }
    
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject((String) reqBody);
        } catch (Exception e) {
            return null;
        }
    
        return jsonObject.getString(key);
    }
}
