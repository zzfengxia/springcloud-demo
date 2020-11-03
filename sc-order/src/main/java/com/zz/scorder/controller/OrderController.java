package com.zz.scorder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.common.RequestExtParams;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.exception.BizException;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.scorder.entity.ConfigEntity;
import com.zz.scorder.entity.ServerIdConfig;
import com.zz.scorder.service.ServerConfigService;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private ServerConfigService configService;
    
    @GetMapping("getOrder")
    public ApiResponse<String> getMessage(@RequestParam String orderNo, @RequestParam String issueId) {
        Date cur = new Date();
        log.info("requst time:" + DateFormatUtils.format(cur, "yyyy-MM-dd HH:mm:ss") + ",order:" + orderNo);
        OrderInfo params = new OrderInfo();
        params.setOrderNo(orderNo);
        params.setPayTime(cur);
        // 保存线程数据
        ServerIdConfig serverConfig = getServerId(issueId);
        if(serverConfig != null) {
            RequestExtParams extParams = new RequestExtParams();
            extParams.setIssueId(issueId);
            extParams.addHeader(BizConstants.HEADER_TRACE_ID, "123456789");
            extParams.setServerId(serverConfig.getServerId());
            extParams.setCardCode(serverConfig.getCardCode());
            FeignDataThreadLocal.set(extParams);
        }
        return orderClient.getOrderInfo(params);
    }
    
    @PostMapping("createOrder")
    @ResponseBody
    public ApiResponse<OrderInfo> createOrder(@RequestBody OrderInfo json, @RequestHeader Map<String, String> header) {
        Date cur = new Date();
        log.info("requst time:" + DateFormatUtils.format(cur, "yyyy-MM-dd HH:mm:ss") + ",body:" + JSON.toJSONString(json));
        log.info("request header:" + JSON.toJSONString(header));
        ConfigEntity entity = configService.getByIssueId("t_vfc_jilin");
        ConfigEntity entity2 = configService.selectByCardCode("000000");
        System.out.println("entity:" + entity.getServerUrl());
        System.out.println("entity2:" + entity2.getServerUrl());
        // 保存线程数据
        ServerIdConfig serverConfig = getServerId(json.getIssueId());
        if(serverConfig != null) {
            RequestExtParams extParams = new RequestExtParams();
            extParams.setIssueId(json.getIssueId());
            extParams.addHeader(BizConstants.HEADER_TRACE_ID, "123456789");
            extParams.setServerId(serverConfig.getServerId());
            extParams.setCardCode(serverConfig.getCardCode());
            FeignDataThreadLocal.set(extParams);
        }
        long start = System.currentTimeMillis();
        ApiResponse<OrderInfo> result = orderClient.createOrder(json);
        long end = System.currentTimeMillis();
        System.out.println("create order executed " + (end - start) + "ms");
        return result;
    }
    
    @RequestMapping("getApi")
    @ResponseBody
    public ApiResponse<String> getApi(String issueId, String msg, String t, @RequestHeader Map<String, String> headers) throws InterruptedException {
        log.info("[" + t + "] request headers:" + headers);
        log.info("[" + t + "] [getApi] request msg:" + msg);
        ConfigEntity result = null;//configService.getByIssueId(issueId);
        if(msg.contains("timeout")) {
            Thread.sleep(2500);
        }
        if(msg.contains("exception")) {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
        
        if(msg.contains("bizException")) {
            return ApiResponse.ofFail("-999", "fail");
        }
        
        if(RandomUtils.nextInt(1, 5000) == 5) {
            return ApiResponse.ofFail("-5", "中奖");
        }
        log.info("[" + t + "] [getApi] response");
        return ApiResponse.ofSuccess(JSON.toJSONString(result));
    }
    
    @PostMapping("postApi")
    @ResponseBody
    public ApiResponse<String> postApi(@RequestBody String json, @PathParam("t") String t,  @RequestHeader Map<String, String> headers) throws InterruptedException {
        log.info("[" + t + "] [postApi] request json:" + json);
        log.info("[" + t + "] request headers:" + headers);
        Map<String, String> req = JSON.parseObject(json, new TypeReference<Map<String, String>>(){});
        ConfigEntity result = null;
        if(req != null) {
            //result = configService.getByIssueId(req.get("issueId"));
        }
        
        if(RandomUtils.nextInt(1, 5000) == 555) {
            return ApiResponse.ofFail("-555", "中奖");
        }
        log.info("[" + t + "] [postApi] response");
        return ApiResponse.ofSuccess(JSON.toJSONString(result));
    }
    
    
    private static List<ServerIdConfig> serverIdConfig = Lists.newArrayList(
            ServerIdConfig.of("demo1", "", "sc-service", "sc-service2", "520000", null, null),
            ServerIdConfig.of("demo2", "", "sc-service", "sc-service1", "320200", null, null),
            ServerIdConfig.of("demo3", "", "sc-service", "sc-service1", "450000", null, null)
    );
    
    private ServerIdConfig getServerId(String issueId) {
        List<ServerIdConfig> matchList = serverIdConfig.stream().filter(c -> {
            return c.getIssueId().equals(issueId);
        }).collect(Collectors.toList());
        
        return matchList.size() > 0 ? matchList.get(0) : null;
    }
}
