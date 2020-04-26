package com.zz.scservice.feignapi;

import com.zz.scservice.entity.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ************************************
 * create by Intellij IDEA
 * FeignClient name参数是请求到具体后端服务的服务名
 * @author Francis.zz
 * @date 2020-04-23 14:20
 * ************************************
 */
@FeignClient("sc-service")
public interface OrderClient {
    @GetMapping("/getOrder")
    String getOrderInfo(@RequestBody OrderInfo params);
}
