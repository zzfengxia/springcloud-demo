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
### 路由规则配置说明
dataId：gateway.route.rule.yaml 
```yaml
route:
  commonPredicate:
    bodyRule:
      attrMap: {command: [ "app.format.refund", "app.query", "autorefund.check", "card.type.query", "cardmove.in", "cardmove.out", "certification", "create.order", "delete.app", "deletecard.notify", "download.install.app", "entrust.cancel", "entrust.query", "entrust.sign", "format.ese", "get.apdu", "get.appletStatus", "get.rechargecard", "invoice.receive", "notify.card.balance", "notify.refundinfo", "personalized", "query.order", "query.success.order", "recharge", "refund", "stop.service.sync", "transcard.backup", "transcard.cancel.restore", "transcard.parkingcard.changeuser", "transcard.remove.check", "transcard.restore"]}
      order: -10
    pathRule:
      path: 
        - /sptsm/dispacher
        - /sptsm/center
      respStrategy: 0
      order: -15

  rules:
    - id: route-demo-1
      uri: http://172.16.80.103:9087/
      order: -100
      
    - id: route-demo-2
      predicate:
        pathRule:
          path: 
            - /sptsm/notify
            - /sptsm/api/asyncStatus
            - /sptsm/thirdParty
            - /sptsm/universalNotify
          respStrategy: 1
      useCommonConfig: false
      uri: http://172.16.80.103:9087/
      order: -50
    - id: route-demo-3
      predicate:
        pathRule:
          path: 
            - /sptsm/notify/**/**
          respStrategy: 2
      useCommonConfig: false
      uri: http://172.16.80.103:9087/
      order: -60
    - id: route-demo-4
      predicate:
        pathRule:
          path: 
            - /mq/getDemo/**
            - /mq/postDemo*/**
      filter:
        pathFilter:
          path: /mq/getDemo/sub1
        headerFilter:
          headers: {"testHeader": "demo"}
          
      useCommonConfig: false
      uri: http://localhost:8083/
      order: -50
```
路由配置说明：

* 支持自定义的predicate：Body、Header、Path、Query、接口生效日期等
* commonPredicate标识通用配置，具体路由可以使用useCommonConfig:false标识不使用通用配置
* Body配置支持匹配多个参数，使用strategy: or表示or或者and
* 如果多个路由规则之间有交叉时，需要使用order指定路由匹配优先级，值越小越优先匹配
* 每个predicate配置都可以使用order指定执行顺序，值越小优先级越高。并且通用配置的predicate会和具体路由的predicate合并并排序。
* 支持根据匹配path自定义网关异常时的响应策略，respStrategy与path绑定。SP：0；ORDER服务：1；微信支付：2。因此path predicate的优先级应高于其他predicate。
* predicate的path配置支持通配符

### 限流规则配置说明
dataId：gateway.sentinel.txt 
```yaml
{"flowRule": [
    {
      "resource": "customized_api1",
      "resourceMode": 1,
      "count": 200.0,
      "intervalSec": 10.0,
      "paramItem": {
          "parseStrategy": 2,
          "fieldName": "flowctrlflag",
          "pattern": "true",
          "matchStrategy": 0
      }
    },
    {
      "resource": "customized_api1",
      "resourceMode": 1,
      "count": 10.0,
      "intervalSec": 10.0,
      "paramItem": {
          "parseStrategy": 2,
          "fieldName": "command-id",
          "pattern": "create.order",
          "matchStrategy": 0
      }
    }
],
"apiDefinition": [
    {
        "apiName": "customized_api1",
        "predicateItems": [{
            "pattern": "/sptsm/dispacher",
            "matchStrategy": 0
        }]
    }
]}
```

限流配置说明：
- apiDefinition：根据请求path分组
- flowRule：流控配置，使用fieldName指定过滤请求头，对请求头中存在指定值的请求限流

## 日志信息
- sentinel日志
- nacos日志

sentinel日志：
> https://github.com/alibaba/Sentinel/wiki/启动配置项  
> https://github.com/alibaba/Sentinel/wiki/日志

nacos日志目录：/root/logs/nacos