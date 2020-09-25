package com.zz.scgatewaynew.respdefine;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 * path predicate失败时的响应
 *
 * @author Francis.zz
 * @date 2020-04-18 16:32
 * ************************************
 */
public class NotFoundResponse implements UpstreamResponse {
    @Override
    public Response failResp(String code, String msg, ServerWebExchange exchange) {
        return Response.instance(httpStatus(), HttpStatus.NOT_FOUND.getReasonPhrase());
    }
    
    @Override
    public int httpStatus() {
        return HttpStatus.NOT_FOUND.value();
    }
}
