package com.zz.sccommon.exception;

/**
 * Created by Francis.zz on 2017/7/10.
 */
public interface IErrorCode {

    /**
     * 错误代码
     *
     * @return
     */
    String getErrorCode();

    /**
     * 记录在操作表的错误信息
     *
     * @return
     */
    String getMessage();

    /**
     * 响应给客户端的错误信息
     *
     * @return
     */
    String getReturnMsg();
}
