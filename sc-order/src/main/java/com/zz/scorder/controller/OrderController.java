package com.zz.scorder.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.common.RequestExtParams;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.exception.BizException;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.scorder.entity.ConfigEntity;
import com.zz.scorder.entity.MetricEntity;
import com.zz.scorder.entity.ServerIdConfig;
import com.zz.scorder.service.ServerConfigService;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
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
@Slf4j
public class OrderController {
    // 使用构造方法注入
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private ServerConfigService configService;
    private static final String MEASUREMENT = "sentinel_metric";
    @Autowired
    private InfluxDBClient influxDBClient;
    @Value("${spring.influx.database:}")
    private String database;
    
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
    
    @GetMapping("compare")
    public String testCompare() {
        List<String> metricFileGroup = Lists.newArrayList(
                "C:\\Users\\Administrator\\Desktop\\metric log\\sc-gateway-metrics.log.2020-11-10-1",
                "C:\\Users\\Administrator\\Desktop\\metric log\\sc-gateway-metrics.log.2020-11-10");
        Map<String, Long> totalPassPerSen = new HashMap<>();
        Map<String, Long> totalSuccessPerSen = new HashMap<>();
        
        metricFileGroup.forEach(fileName -> {
            File file = new File(fileName);
            if(!file.exists()) {
                return;
            }
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                String lineStr = null;
                while((lineStr = br.readLine()) != null && lineStr.length() != 0) {
                    
                    if(lineStr.contains("__total_inbound_traffic__"))
                        continue;
                    String[] datas = lineStr.split("\\|");
                    String curSec = datas[0];
                    
                    if(totalPassPerSen.get(curSec) != null) {
                        totalPassPerSen.put(curSec, totalPassPerSen.get(curSec) + Long.parseLong(datas[3]));
                    } else {
                        totalPassPerSen.put(curSec, Long.parseLong(datas[3]));
                    }
                    
                    if(totalSuccessPerSen.get(curSec) != null) {
                        totalSuccessPerSen.put(curSec, totalSuccessPerSen.get(curSec) + Long.parseLong(datas[5]));
                    } else {
                        totalSuccessPerSen.put(curSec, Long.parseLong(datas[5]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        
        if(totalPassPerSen.isEmpty() || totalSuccessPerSen.isEmpty()) {
            return "false";
        }
        System.out.println("开始查询DB比对...");
        totalSuccessPerSen.forEach((k, v) -> {
            // 查询influxdb中k对应的time的数据
            MetricEntity query = queryAndConcatByAppAndTime("sc-gateway", Long.parseLong(k));
            if(!query.getSuccessQps().equals(v)) {
                System.out.println(String.format("Success不匹配，时间：%s, db值：%s, 文件值：%s", k, query.getSuccessQps(), v));
                return;
            }
            if(!query.getPassQps().equals(totalPassPerSen.get(k))) {
                System.out.println(String.format("Pass不匹配，时间：%s, db值：%s, 文件值：%s", k, query.getPassQps(), totalPassPerSen.get(k)));
                return;
            }
        });
        return "success";
    }
    
    public synchronized MetricEntity queryAndConcatByAppAndTime(String app, long time) {
        if (StringUtil.isBlank(app)) {
            return null;
        }
        //String command = String.format("select * from %s where app = '%s' and resource = '%s' and time > %s and time < %s", MEASUREMENT, app, resource, startTime, endTime);
        //String command = String.format("from(bucket:\"%s\") |> range(start:%s, stop:%s) |> filter(fn:(r) => r._measurement == \"%s\" and r.app = \"%s\" and r.resource == \"%s\")", database, startTime, endTime, MEASUREMENT, app, resource);
        Flux flux = Flux.from(database)
                .range(-2L, ChronoUnit.DAYS)
                .filter(Restrictions.and(Restrictions.measurement().equal(MEASUREMENT),
                        Restrictions.tag("app").equal(app), Restrictions.time().equal(Instant.ofEpochMilli(time))))
                .pivot(new String[]{"_time"}, new String[]{"_field"}, "_value");
        
        List<SentinelMetric> queryResult = influxDBClient.getQueryApi().query(flux.toString(), SentinelMetric.class);
        MetricEntity entity = null;
        for(SentinelMetric sm : queryResult) {
            if (entity != null) {
                MetricEntity node = sm.toMetricEntity();
                entity.addPassQps(node.getPassQps());
                entity.addBlockQps(node.getBlockQps());
                entity.addRtAndSuccessQps(node.getRt(), node.getSuccessQps());
                entity.addExceptionQps(node.getExceptionQps());
                entity.addUpstreamFailQps(node.getUpstreamFailQps());
                entity.addCount(1);
            } else {
                entity = sm.toMetricEntity();
            }
        }
        return entity;
    }
    
    @Measurement(name = MEASUREMENT)
    public static class SentinelMetric {
        @Column(timestamp = true)
        private Instant time;
        @Column(tag = true)
        private String app;
        @Column(tag = true)
        private String resource;
        
        @Column(name = "pass_qps")
        private Long passQps;
        @Column(name = "success_qps")
        private Long successQps;
        @Column(name = "block_qps")
        private Long blockQps;
        @Column(name = "exception_qps")
        private Long exceptionQps;
        @Column(name = "upstream_fail_qps")
        private Long upstreamFailQps;
        @Column(name = "rt")
        private double rt;
        @Column(name = "classification")
        private Long classification;
        @Column(name = "count")
        private Long count;
        
        public MetricEntity toMetricEntity() {
            MetricEntity entity = new MetricEntity();
            entity.setApp(app);
            entity.setTimestamp(Date.from(time));
            entity.setResource(resource);
            entity.setPassQps(passQps);
            entity.setBlockQps(blockQps);
            entity.setSuccessQps(successQps);
            entity.setExceptionQps(exceptionQps);
            entity.setUpstreamFailQps(upstreamFailQps);
            entity.setRt(rt);
            entity.setClassification(NumberUtils.toInt(classification+""));
            entity.setCount(NumberUtils.toInt(count+"",0));
            
            return entity;
        }
    }
}
