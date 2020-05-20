package com.zz.scservice.feignapi;

import com.zz.api.common.protocal.ApiResponse;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.fallback.OrderClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ************************************
 * create by Intellij IDEA
 * FeignClient name参数是请求到具体后端服务的服务名
 * @author Francis.zz
 * @date 2020-04-23 14:20
 * ************************************
 */
@FeignClient(value = "sc-service", /*fallback = OrderClientFallback.class, */fallbackFactory = OrderClientFactory.class)
public interface OrderClient {
    /**
     * FeignClient的configuration默认为 FeignClientsConfiguration
     * 当请求参数没有 @RequestBody、@RequestParam、@Param等注解时需要自定义encoder实现，不实现的话默认SpringEncoder
     * @see {@link feign.codec.Encoder}
     *
     * @param params
     * @return
     */
    @GetMapping("/getOrder")
    ApiResponse<String> getOrderInfo(@RequestBody OrderInfo params);
    
    @PostMapping("/createOrder")
    ApiResponse<OrderInfo> createOrder(OrderInfo order);
}
