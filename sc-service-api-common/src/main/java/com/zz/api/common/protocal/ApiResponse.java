package com.zz.api.common.protocal;

import com.zz.sccommon.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by Francis.zz on 2017/4/27.
 */
@Accessors(chain = true)
@Setter
@Getter
public class ApiResponse<T> {
    private static String SUCCESS_CODE = ErrorCode.SUCCESS.getErrorCode();
    private boolean success;
    private String message;
    /**
     * 0：成功
     */
    private String code;
    private T data;
    
    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<T>()
                .setSuccess(true)
                .setMessage("success")
                .setCode(SUCCESS_CODE)
                .setData(data);
    }
    
    public static <T> ApiResponse<T> ofSuccessMsg(String msg) {
        return new ApiResponse<T>()
                .setSuccess(true)
                .setCode(SUCCESS_CODE)
                .setMessage(msg);
    }
    
    public static <T> ApiResponse<T> ofFail(String code, String msg) {
        return new ApiResponse<T>()
                .setSuccess(false)
                .setCode(code)
                .setMessage(msg);
    }
    
    public static <T> ApiResponse<T> ofFail(String code, Throwable throwable) {
        return new ApiResponse<T>()
                .setSuccess(false)
                .setCode(code)
                .setMessage(throwable.getClass().getName() + ", " + throwable.getMessage());
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
