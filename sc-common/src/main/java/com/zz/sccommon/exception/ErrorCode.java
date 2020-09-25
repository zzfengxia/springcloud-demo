package com.zz.sccommon.exception;

/**
 *  错误码 统一格式：CCXXX
 *  CC是模块代码；XXX具体错误代码
 *  错误码主要是对外返回
 */
public enum ErrorCode implements IErrorCode {
    /**
     * 参数说明：参数1是错误代码，参数2是响应给客户端的错误信息，参数3为系统日志打印信息以及写入操作表的错误信息
     * {@link BizException#BizException(IErrorCode, Object...)} 构造方法是为响应给客户端的信息赋值，日志打印的错误信息赋值需要
     * 使用{@link BizException#setArgs(Object...)}方法
     */

    /*==================================公共错误 000XXX start==================================================*/
    // 成功，禁止修改
    SUCCESS("0", "success"),
    // 系统内部错误
    SYSTEM_ERROR("00001", "system error", "system error, %s"),
    
    UPSTREAM_RESP_FAIL("98888", "上游服务业务处理失败"),
    
    TOO_MANY_REQUESTS("99998", "服务器繁忙，请稍后重试"),
    SERVER_DEGRADE("99997", "服务降级，请稍后重试"),
    /*==================================其他(比如身份认证、领取发票等) end========================================*/
    ;

    /**
     * pm_business_opt_log操作表记录信息
     */
    private String message;
    /**
     * 客户端响应信息
     */
    private String returnMsg;
    /**
     * 异常描述，支持String.format格式
     */
    private String errorCode;

    ErrorCode(String errorCode, String returnMsg) {
        this(errorCode, returnMsg, returnMsg);
    }

    ErrorCode(String errorCode, String returnMsg, String message) {
        this.message = message;
        this.returnMsg = returnMsg;
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getReturnMsg() {
        return returnMsg;
    }

    public ErrorCode setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
        return this;
    }

    public ErrorCode setMessage(String msg) {
        this.message = msg;
        return this;
    }
}
