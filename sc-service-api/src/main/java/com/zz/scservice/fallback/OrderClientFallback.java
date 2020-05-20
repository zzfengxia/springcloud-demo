package com.zz.scservice.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.extern.slf4j.Slf4j;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-14 16:51
 * ************************************
 */
@Slf4j
public class OrderClientFallback implements OrderClient{
    private Throwable ex;
    
    public OrderClientFallback(Throwable ex) {
        this.ex = ex;
    }
    
    public OrderClientFallback() {
    }
    
    @Override
    public ApiResponse<String> getOrderInfo(OrderInfo params) {
        log.info("getOrderInfo 降级");
        return ApiResponse.ofFail(ErrorCode.SERVER_DEGRADE.getErrorCode(), "getOrderInfo 降级");
    }
    
    /**
     * 如果没有fallback实现，那么降级发生时会执行BlockExceptionHandler的实现
     *
     * sentinel执行降级的场景：
     * 1. 请求的服务响应http status不是200就会执行这里的代码(不管怎么配置降级)
     * 2.
     *
     * @param order
     * @return
     */
    @Override
    public ApiResponse<OrderInfo> createOrder(OrderInfo order) {
        ApiResponse<OrderInfo> resp = exceptionJudge();
        
        return resp != null ? resp : ApiResponse.ofFail(ErrorCode.SERVER_DEGRADE.getErrorCode(), "server降级");
    }
    
    private ApiResponse<OrderInfo> exceptionJudge() {
        if(BlockException.isBlockException(ex)) {
            log.info("serverName:[" + FeignDataThreadLocal.get().getServerName() + "], serverId:[" + FeignDataThreadLocal.get().getServerId() + "]已降级");
            return null;
        }
        log.info("client request error：" + ex.getMessage());
        return ApiResponse.ofFail(ErrorCode.SERVER_DEGRADE.getErrorCode(), ex);
    }
}
