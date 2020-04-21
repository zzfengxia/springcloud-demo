package com.zz.scgatewaynew.routedefine;

import lombok.Getter;
import lombok.Setter;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * 完整配置参考：
 * <pre>
 * {"flowRule": [
 *     {
 *       "resource": "customized_api1",
 *       "resourceMode": 1,
 *       "count": 1.0,
 *       "intervalSec": 5.0,
 *       "paramItem": {
 *           "parseStrategy": 2,
 *           "fieldName": "flowctrlflag",
 *           "pattern": "true",
 *           "matchStrategy": 0
 *       }
 *     }
 * ],
 * "apiDefinition": [
 *     {
 *         "apiName": "customized_api1",
 *         "predicateItems": [{
 *             "pattern": "/sptsm/dispatcher",
 *             "matchStrategy": 0
 *         }]
 *     }
 * ]}
 * </pre>
 * @author Francis.zz
 * @date 2020-04-03 11:17
 * ************************************
 */
@Setter
@Getter
public class SentinelDefinition {
    /**
     * 网关流控配置
     * <pre>
     * "flowRule":
     * [
     *     {
     *       "resource": "route-demo-1",
     *       "count": 2.0,
     *       "intervalSec": 1.0,
     *       "paramItem": {
     *           "parseStrategy": 2,
     *           "fieldName": "flowctrlflag",
     *           "pattern": "true",
     *           "matchStrategy": 0
     *       }
     *     }
     * ]
     * </pre>
     */
    private String flowRule;
    
    /**
     * api分组定义
     * <pre>
     * [
     *     {
     *         "apiName": "customized_api1",
     *         "predicateItems": [{
     *             "pattern": "/dispatcher",
     *             "matchStrategy": 0
     *         }]
     *     }
     * ]
     * </pre>
     */
    private String apiDefinition;
}
