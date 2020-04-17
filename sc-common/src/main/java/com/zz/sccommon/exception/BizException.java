package com.zz.sccommon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francis.zz on 2017/7/10.
 */
public class BizException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(BizException.class);

    private IErrorCode errorCode;
    private Object[] args;
    private Object[] returnMsgArgs;

    public BizException(IErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public BizException setArgs(Object... args) {
        this.args = args;
        return this;
    }

    public BizException setReturnMsgArgs(Object... returnMsgArgs) {
        this.returnMsgArgs = returnMsgArgs;
        return this;
    }

    public BizException(IErrorCode errorCode, Object... returnMsgArgs) {
        super();
        this.errorCode = errorCode;
        this.args = returnMsgArgs;
        this.returnMsgArgs = returnMsgArgs;
    }

    /**
     * 写入到记录表的错误信息，可能比{@link #getReturnMsg()}方法的信息更详细
     * @return
     */
    public String getMessage() {
        if(args != null) {
            return parseMessage(errorCode.getMessage(), args);
        }else {
            return errorCode.getMessage();
        }
    }

    /**
     * 更详细的错误信息
     *
     * @return
     */
    public String getContent() {
        if(args != null) {
            return (new StringBuilder()).append("errorCode:[").append(errorCode.getErrorCode()).append("] ").
                    append(parseMessage(errorCode.getMessage(), args)).toString();
        }else {
            return (new StringBuilder()).append("errorCode:[").append(errorCode.getErrorCode()).append("] ").
                    append(errorCode.getMessage()).toString();
        }
    }

    /**
     * 响应给客户端的错误信息
     *
     * @return
     */
    public String getReturnMsg() {
        if(returnMsgArgs != null) {
            return parseMessage(errorCode.getReturnMsg(), returnMsgArgs);
        }else {
            return errorCode.getReturnMsg();
        }
    }

    public String getErrorCode() {
        return errorCode.getErrorCode();
    }

    private String parseMessage(String formatMsg, Object[] args) {
        String message;
        try {
            message = String.format(formatMsg, args);
        }catch (Exception e) {
            logger.error("error code info parse failed, origin msg:{}", formatMsg);
            message = formatMsg.replaceAll("%s", "");
        }
        return message;
    }
}
