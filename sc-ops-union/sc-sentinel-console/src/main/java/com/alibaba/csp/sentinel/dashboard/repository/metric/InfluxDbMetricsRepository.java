package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 * sentinel-metric信息存储在influxdb中
 * influxdb-client-java example：<a>https://github.com/influxdata/influxdb-client-java/tree/master/examples/src/main/java/example</a>
 *
 * @author Francis.zz
 * @date 2020-06-30 11:56
 * ************************************
 */
@Component("InfluxDbMetricsRepository")
public class InfluxDbMetricsRepository implements MetricsRepository<MetricEntity> {
    private static final String MEASUREMENT = "sentinel_metric";
    @Autowired
    private InfluxDBClient influxDBClient;
    @Value("${spring.influx.database:}")
    private String database;
    
    @Override
    public synchronized void save(MetricEntity metric) {
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            writeApi.writePoint(toPoint(metric));
        }
    }
    
    @Override
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        List<Point> pointList = new ArrayList<>();
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            metrics.forEach(p -> pointList.add(toPoint(p)));
            writeApi.writePoints(pointList);
        }
    }
    
    @Override
    public synchronized List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        //String command = String.format("select * from %s where app = '%s' and resource = '%s' and time > %s and time < %s", MEASUREMENT, app, resource, startTime, endTime);
        //String command = String.format("from(bucket:\"%s\") |> range(start:%s, stop:%s) |> filter(fn:(r) => r._measurement == \"%s\" and r.app = \"%s\" and r.resource == \"%s\")", database, startTime, endTime, MEASUREMENT, app, resource);
        Flux flux = Flux.from(database)
                .range(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime))
                .filter(Restrictions.and(Restrictions.measurement().equal(MEASUREMENT), Restrictions.tag("app").equal(app), Restrictions.tag("resource").equal(resource)))
                .pivot(new String[]{"_time"}, new String[]{"_field"}, "_value");
        
        List<SentinelMetric> queryResult = influxDBClient.getQueryApi().query(flux.toString(), SentinelMetric.class);
        queryResult.forEach(r -> results.add(r.toMetricEntity()));
        return results;
    }
    
    /**
     * 最近1分钟内的所有资源名称
     *
     * @param app application name
     * @return
     */
    @Override
    public synchronized List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        //最近一分钟的指标(实时数据)
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        
        //String command = String.format("select * from %s where app = '%s' and time > %s", MEASUREMENT, app, minTimeMs);
        //String command = String.format("from(bucket:\"%s\") |> range(start:-1m) |> filter(fn:(r) => r._measurement == \"%s\" and r.app == \"%s\")", database, MEASUREMENT, app);
        Flux flux = Flux.from(database)
                .range(Instant.now().minusMillis(1000 * 60))
                .filter(Restrictions.and(Restrictions.measurement().equal(MEASUREMENT), Restrictions.tag("app").equal(app)))
                .pivot(new String[]{"_time"}, new String[]{"_field"}, "_value");
        
        List<SentinelMetric> queryResult = influxDBClient.getQueryApi().query(flux.toString(), SentinelMetric.class);
        Map<String, MetricEntity> resourceCount = new HashMap<>(32);
        
        for (SentinelMetric metric : queryResult) {
            MetricEntity metricEntity = metric.toMetricEntity();
            String resource = metricEntity.getResource();
            if (resourceCount.containsKey(resource)) {
                MetricEntity oldEntity = resourceCount.get(resource);
                oldEntity.addPassQps(metricEntity.getPassQps());
                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                oldEntity.addBlockQps(metricEntity.getBlockQps());
                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(resource, metricEntity);
            }
        }
        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    MetricEntity e1 = o1.getValue();
                    MetricEntity e2 = o2.getValue();
                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                    if (t != 0) {
                        return t;
                    }
                    return e2.getPassQps().compareTo(e1.getPassQps());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    private Point toPoint(MetricEntity metric) {
        // series：InfluxDB数据结构的集合，一个特定的series由measurement，tag set和retention policy组成。
        return Point.measurement(MEASUREMENT)
                .time(metric.getTimestamp().toInstant(), WritePrecision.MS)
                .addTag("app", metric.getApp())
                .addTag("resource", metric.getResource())
                //.addField("timestamp", metric.getTimestamp().getTime())
                .addField("pass_qps", (long) metric.getPassQps())
                .addField("success_qps", (long) metric.getSuccessQps())
                .addField("block_qps", (long) metric.getBlockQps())
                .addField("exception_qps", (long) metric.getExceptionQps())
                .addField("upstream_fail_qps", (long) metric.getUpstreamFailQps())
                .addField("rt", metric.getRt())
                .addField("classification", metric.getClassification())
                .addField("count", metric.getCount());
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
