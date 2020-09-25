package com.alibaba.csp.sentinel.slots.block;

/**
 * Created by Francis.zz on 2017/7/10.
 */
public class UpstreamRespException extends RuntimeException {

    private Object[] args;
    private Object[] returnMsgArgs;

    public UpstreamRespException() {
        super();
    }

    public UpstreamRespException(String message) {
        super(message);
    }
}
