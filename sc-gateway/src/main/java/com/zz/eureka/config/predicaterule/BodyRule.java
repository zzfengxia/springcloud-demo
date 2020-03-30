package com.zz.eureka.config.predicaterule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
     * 属性map
     * eg: cardCode: 1001, 1002
     */
    private Map<String, List<String>> attrMap;
    
    private String strategy = AND;
    
    @Override
    public boolean validate() {
        if(!AND.equals(strategy) && !OR.equals(strategy)) {
            return false;
        }
        return true;
    }
    
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.readBody(String.class, body -> {
            // 不能读多次
            log.info("request json:{}", JSON.toJSONString(body));
            JSONObject jsonObject = JSONObject.parseObject(body);
            boolean predicateFlag = false;
            
            if(attrMap == null || attrMap.isEmpty()) {
                return true;
            }
            
            for (Map.Entry<String, List<String>> entry : attrMap.entrySet()) {
                String name = entry.getKey();
                String bodyValue;
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
            
            return predicateFlag;
        });
    }
}
