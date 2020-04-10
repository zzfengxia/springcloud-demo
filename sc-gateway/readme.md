## 技术选型
- Spring-cloud Hoxton.SR3
- Spring-boot 2.2.5.RELEASE
- Spring-cloud-gateway 网关
- nacos 服务发现、配置中心
- sentinel 服务降级、限流  
地址：https://github.com/alibaba/Sentinel/tree/master/sentinel-adapter/sentinel-spring-cloud-gateway-adapter  
文档：https://github.com/alibaba/Sentinel/wiki/网关流控

### 自定义动态路由配置指南

注意：
- 如果多个路由规则之间有交叉时，需要使用order指定路由匹配优先级，值越小越优先匹配