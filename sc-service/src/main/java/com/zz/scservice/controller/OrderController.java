package com.zz.scservice.controller;

import com.alibaba.fastjson.JSON;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-23 14:30
 * ************************************
 */
@RestController
@Slf4j
public class OrderController implements OrderClient {
    @Value("${server.port}")
    private String port;
    
    @Override
    //@GetMapping("/getOrder")
    public String getOrderInfo(@RequestBody OrderInfo params1) {
        log.info("request params:" + JSON.toJSONStringWithDateFormat(params1, "yyyy-MM-dd HH:mm:ss"));
        return "hi, i'm from " + port + ", orderNo:" + params1.getOrderNo();
    }
}
