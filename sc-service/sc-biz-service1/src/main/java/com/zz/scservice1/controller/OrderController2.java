package com.zz.scservice1.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.api.common.protocal.CityCodeConstant;
import com.zz.scservice.entity.OrderInfo;
import lombok.extern.slf4j.Slf4j;
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
 * @date 2020-05-19 17:01
 * ************************************
 */
@RestController
@Slf4j
@RequestMapping(CityCodeConstant.WUXI)
public class OrderController2 {
    @PostMapping("/createOrder")
    public ApiResponse<OrderInfo> createOrder(@RequestHeader Map<String, String> header, @RequestBody OrderInfo params) {
        log.info("request Body:" + JSON.toJSON(params));
        log.info("request header:" + JSON.toJSONString(header));
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
        params.setCardCode(CityCodeConstant.WUXI);
        return ApiResponse.ofSuccess(params);
    }
}
