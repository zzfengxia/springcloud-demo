package com.zz.scgatewaynew.respdefine;

import com.zz.sccommon.constant.ApiConstants;
import com.zz.sccommon.util.xml.WXPayUtil;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 15:47
 * ************************************
 */
public class WechatResponse implements UpstreamResponse {
    @Override
    public Response failResp(String code, String msg, ServerWebExchange exchange) {
        Map<String, String> resp = new HashMap<>();
        resp.put(ApiConstants.WECHAT_RESP_CODE, "FAIL");
        resp.put(ApiConstants.WECHAT_RESP_MSG, "异常");
    
        try {
            return Response.instance(httpStatus(), WXPayUtil.mapToXml(resp));
        } catch (Exception e) {
            return Response.instance(httpStatus(), "");
        }
    }
    
    @Override
    public boolean isSuccessResponse(String respBody) {
        try {
            Map<String, String> respMap = WXPayUtil.xmlToMap(respBody);
            String respCode = respMap.get(ApiConstants.WECHAT_RESP_CODE);
            return "SUCCESS".equals(respCode);
        }catch (Exception e) {
            // no op
            return true;
        }
    }
}
