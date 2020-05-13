package com.zz.sccommon.interceptor;

import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-09-25 15:44
 * @desc MDCLogInterceptor
 * ************************************
 */
@Slf4j
public class MDCLogInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(BizConstants.HEADER_TRACE_ID);
        if(StringUtils.isEmpty(traceId)) {
            traceId = UuidUtils.generateUuid();
        }
        MDC.put(BizConstants.MDC_TRACE_ID, traceId);
        if(StringUtils.isEmpty(traceId)) {
            log.info("not found [ " + BizConstants.HEADER_TRACE_ID + " ] from request headers");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.clear();
    }
}
