package com.zz.scorder.controller;

import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-23 14:38
 * ************************************
 */
@RestController
@AllArgsConstructor
@Slf4j
public class OrderController {
    // 使用构造方法注入
    private OrderClient orderClient;
    
    @GetMapping("getOrder")
    public String getMessage(@RequestParam String orderNo) {
        Date cur = new Date();
        log.info("requst time:" + DateFormatUtils.format(cur, "yyyy-MM-dd HH:mm:ss") + ",order:" + orderNo);
        OrderInfo params = new OrderInfo();
        params.setOrderNo(orderNo);
        params.setPayTime(cur);
        return orderClient.getOrderInfo(params);
    }
}
