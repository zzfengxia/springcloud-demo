package com.zz.scgateway.respdefine;

import com.alibaba.fastjson.JSON;
import com.zz.sccommon.constant.ApiConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 15:46
 * ************************************
 */
public class OrderApiResponse implements IFailResponse {
    @Override
    public Response failResp(String code, String msg, ServerWebExchange exchange) {
        msg = StringUtils.isNotEmpty(msg) ? msg : "服务器开小差啦";
        
        Map<String, String> resp = new HashMap<>();
        resp.put(ApiConstants.ORDER_RESP_CODE, "2");
        resp.put(ApiConstants.ORDER_RESP_MSG, msg);
        
        return Response.instance(httpStatus(), JSON.toJSONString(resp));
    }
}
