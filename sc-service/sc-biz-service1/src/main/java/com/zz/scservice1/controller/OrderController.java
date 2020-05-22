package com.zz.scservice1.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.api.common.protocal.CityCodeConstant;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping(CityCodeConstant.GUANGXI)
public class OrderController implements OrderClient {
    @Value("${server.port}")
    private String port;
    @Value("${spring.application.name}")
    private String serverName;
    
    @Override
    public ApiResponse<String> getOrderInfo(@RequestBody OrderInfo params1) {
        log.info("request params:" + JSON.toJSONStringWithDateFormat(params1, "yyyy-MM-dd HH:mm:ss"));
        return ApiResponse.ofSuccessMsg("hi, i'm from " + serverName +  ":" + port + ", orderNo:" + params1.getOrderNo());
    }
    
    @Override
    public ApiResponse<OrderInfo> createOrder(@RequestBody OrderInfo params) {
        log.info("request Body:" + JSON.toJSON(params));
        if("100".equals(params.getUserId())) {
            throw new IllegalArgumentException();
        }
        if("tt".equals(params.getUserId())) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        params.setOrderNo("11" + System.currentTimeMillis());
        params.setUserId("Tom11");
        params.setPort(port);
        params.setCardCode(CityCodeConstant.GUANGXI);
        
        return ApiResponse.ofSuccess(params);
    }
}
