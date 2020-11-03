# 网关服务
## 使用框架
- Spring-cloud Hoxton.SR3
- Spring-boot 2.2.5.RELEASE
- Spring-cloud-gateway 网关
- nacos 服务发现、配置中心
- sentinel 服务降级、限流  
地址：https://github.com/alibaba/Sentinel/tree/master/sentinel-adapter/sentinel-spring-cloud-gateway-adapter  
文档：https://github.com/alibaba/Sentinel/wiki/网关流控

## 动态路由配置指南

## 日志信息
- sentinel日志
- nacos日志

sentinel日志：
> https://github.com/alibaba/Sentinel/wiki/启动配置项  
> https://github.com/alibaba/Sentinel/wiki/日志

nacos日志目录：/root/logs/nacos

# sentinel-1.8.0已知BUG

## 更新网关流控失败
com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager.applyToConvertedParamMap(230)
```
ParameterMetricStorage.getParamMetricForResource(resource).clearForRule(rule);
```
空指针异常。  

更新未请求过的路由的流控信息会报空指针，需要先请求一次，再更新。