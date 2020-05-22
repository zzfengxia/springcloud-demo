package com.zz.scservice2.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.api.common.protocal.CityCodeConstant;
import com.zz.scservice.entity.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
@RequestMapping(CityCodeConstant.GUIZHOU)
public class OrderController /*implements OrderClient*/ {
    @Value("${server.port}")
    private String port;
    @Value("${spring.application.name}")
    private String serverName;
    @GetMapping("/getOrder")
    public ApiResponse<String> getOrderInfo(@RequestBody OrderInfo params1, @RequestHeader Map<String, String> headers) {
        log.info("request params:" + JSON.toJSONStringWithDateFormat(params1, "yyyy-MM-dd HH:mm:ss"));
        log.info("request headers:" + JSON.toJSON(headers));
        return ApiResponse.ofSuccessMsg("hi, i'm from " + serverName +  ":" + port + ", orderNo:" + params1.getOrderNo());
    }
    
    @PostMapping("/createOrder")
    public ApiResponse<OrderInfo> createOrder(@RequestBody OrderInfo params, @RequestHeader Map<String, String> headers) {
        log.info("request headers:" + JSON.toJSON(headers));
        log.info("request Body:" + JSON.toJSON(params));
        params.setOrderNo("11" + System.currentTimeMillis());
        params.setUserId("Tom11");
        params.setPort(port);
        params.setCardCode(CityCodeConstant.GUIZHOU);
        
        return ApiResponse.ofSuccess(params);
    }
}
