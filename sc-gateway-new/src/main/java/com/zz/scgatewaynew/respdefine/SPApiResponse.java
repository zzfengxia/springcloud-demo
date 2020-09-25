package com.zz.scgatewaynew.respdefine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.sccommon.constant.ApiConstants;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.sccommon.util.sign.RSASignatureUtil;
import com.zz.sccommon.util.sign.SignatureUtils;
import com.zz.scgatewaynew.util.GatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 15:45
 * ************************************
 */
public class SPApiResponse implements UpstreamResponse {
    private final String privateKeyStr;
    
    public SPApiResponse(String privateKeyStr) {
        this.privateKeyStr = privateKeyStr;
    }
    
    @Override
    public Response failResp(String code, String msg, ServerWebExchange exchange) {
        msg = StringUtils.isNotEmpty(msg) ? msg : "服务器开小差啦";
        code = StringUtils.isNotEmpty(code) ? code : ErrorCode.SYSTEM_ERROR.getErrorCode();
        
        Map<String, Object> resp = new HashMap<>();
        resp.put(ApiConstants.SP_RESP_CODE, code);
        resp.put(ApiConstants.SP_RESP_MSG, msg);
    
        boolean flag = GatewayUtils.wrapRespHeaderWithSign(exchange, resp, privateKeyStr);
        
        if(!flag) {
            // 签名
            String signStr = SignatureUtils.sign(resp, RSASignatureUtil.SIGN_ALGORITHMS_SHA256, privateKeyStr);
            resp.put("sign", signStr);
            resp.put("signType", RSASignatureUtil.SIGN_ALGORITHMS_SHA256);
        }
        
        return Response.instance(httpStatus(), JSON.toJSONString(resp));
    }
    
    @Override
    public boolean isSuccessResponse(String respBody) {
        try {
            Map<String, Object> respMap = JSON.parseObject(respBody, new TypeReference<Map<String, Object>>(){});
            Object respCode = respMap.get(ApiConstants.SP_RESP_CODE);
            return "0".equals(respCode);
        }catch (Exception e) {
            // no op
            return true;
        }
    }
}
