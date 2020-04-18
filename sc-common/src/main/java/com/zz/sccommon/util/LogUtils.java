package com.zz.sccommon.util;

import com.zz.sccommon.constant.BizConstants;
import org.slf4j.MDC;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-08 15:37
 * ************************************
 */
public class LogUtils {
    /**
     * 将日志追踪ID保存进MDC
     *
     * @param sessionId
     */
    public static void saveSessionIdForLog(String sessionId) {
        MDC.clear();
        MDC.put(BizConstants.MDC_TRACE_ID, sessionId);
    }
    
    public static void clearSessionForLog() {
        MDC.clear();
    }
}
