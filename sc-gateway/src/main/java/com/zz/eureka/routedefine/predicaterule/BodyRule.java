package com.zz.eureka.routedefine.predicaterule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;

import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 * nacos配置中心的配置注入到java bean
 *
 * nacos解析数据类型的规则：
 * 先从dataId中获取，如果有“.”则取最后一个字符串作为type
 * 否则使用指定的type
 *
 * yaml解析代码参加{@link com.alibaba.nacos.spring.util.parse.DefaultYamlConfigParse}
 * @author Francis.zz
 * @date 2020-03-26 15:07
 * ************************************
 */
@Data
@Slf4j
public class BodyRule implements IRule {
    public static final List<String> ALLOW_ATTR = Lists.newArrayList("command", "caller", "issuerid");
    
    public static final String OR = "or";
    public static final String AND = "and";
    /**
     * 属性map,value为""或者null时，表示明确指定body体中没有key 这个属性
     * attrMap: {caller: OnePlusWallet1, command: ["query.order", "create.order"]}
     *
     * 例如下面配置：
     * <pre>
     * bodyRule:
     *   attrMap: {issueid: null, command: ["query.order", "create.order"]}
     *   strategy: and
     * </pre>
     * 表示当issueid不存在或者为空且command为"query.order", "create.order"时才会被正确路由
     *
     */
    private Map<String, List<String>> attrMap;
    
    private String strategy = AND;
    
    @Override
    public boolean validate() {
        if(!AND.equals(strategy) && !OR.equals(strategy)) {
            log.warn("[strategy] must be or、and, so BodyRule config is invalid. now strategy:{}", strategy);
            return false;
        }
        return true;
    }
    
    /**
     * 由于PredicateSpec.readBody不能调用多次，所以同一个route规则也不能重复配置BodyRule
     *
     * @param predicateSpec
     * @return
     */
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.readBody(String.class, body -> {
            JSONObject jsonObject = JSONObject.parseObject(body);
            boolean predicateFlag = false;
            
            if(attrMap == null || attrMap.isEmpty()) {
                return true;
            }
            
            for (Map.Entry<String, List<String>> entry : attrMap.entrySet()) {
                String name = entry.getKey();
                // 配置的值为空时表示请求body中key必须不存在或者为空
                List<String> regex = entry.getValue();
                String bodyValue;
                if(regex == null || regex.isEmpty()) {
                    if(StringUtils.isEmpty(jsonObject.getString(name))) {
                        if(AND.equals(strategy)) {
                            predicateFlag = true;
                        } else {
                            return true;
                        }
                    } else {
                        if(AND.equals(strategy)) {
                            return false;
                        }
                    }
                } else {
                    if ((bodyValue = jsonObject.getString(name)) != null && entry.getValue().contains(bodyValue)) {
                        if(AND.equals(strategy)) {
                            predicateFlag = true;
                        } else {
                            return true;
                        }
                    } else {
                        if(AND.equals(strategy)) {
                            return false;
                        }
                    }
                }
            }
            
            return predicateFlag;
        });
    }
}
