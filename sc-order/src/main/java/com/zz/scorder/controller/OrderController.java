package com.zz.scorder.controller;

import com.alibaba.fastjson.JSON;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.common.RequestExtParams;
import com.zz.sccommon.constant.BizConstants;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public String getMessage(@RequestParam String orderNo, @RequestParam String issueId) {
        Date cur = new Date();
        log.info("requst time:" + DateFormatUtils.format(cur, "yyyy-MM-dd HH:mm:ss") + ",order:" + orderNo);
        OrderInfo params = new OrderInfo();
        params.setOrderNo(orderNo);
        params.setPayTime(cur);
        // 保存线程数据
        RequestExtParams extParams = new RequestExtParams();
        extParams.setIssueId(issueId);
        extParams.addHeader(BizConstants.HEADER_TRACE_ID, "123456789");
        FeignDataThreadLocal.set(extParams);
        return orderClient.getOrderInfo(params);
    }
    
    @PostMapping("createOrder")
    @ResponseBody
    public OrderInfo createOrder(@RequestBody OrderInfo json) {
        Date cur = new Date();
        log.info("requst time:" + DateFormatUtils.format(cur, "yyyy-MM-dd HH:mm:ss") + ",body:" + JSON.toJSONString(json));
        
        // 保存线程数据
        RequestExtParams extParams = new RequestExtParams();
        extParams.setIssueId(json.getIssueId());
        extParams.addHeader(BizConstants.HEADER_TRACE_ID, "123456789");
        FeignDataThreadLocal.set(extParams);
        return orderClient.createOrder(json);
    }
}
