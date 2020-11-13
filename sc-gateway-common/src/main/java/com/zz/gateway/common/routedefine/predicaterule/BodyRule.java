package com.zz.gateway.common.routedefine.predicaterule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zz.gateway.common.factory.CustomeReadBodyPredicateFactory;
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
 * 仅支持JSON格式数据的解析
 *
 * nacos解析数据类型的规则：
 * 先从dataId中获取，如果有“.”则取最后一个字符串作为type
 * 否则使用指定的type
 *
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
     * todo request body仅支持json格式字符串
     *
     */
    private Map<String, List<String>> attrMap;
    
    private String strategy = AND;
    /**
     * predicate顺序，值越小越优先
     */
    private int order;
    
    /**
     * 空对象，用来获取body体。因为还未找到在断言失败且未调用readBody断言的情况获取 POST请求request body的方法.确保优先级最高
     */
    public static BodyRule empty() {
        BodyRule empty = new BodyRule();
        empty.setOrder(HIGHEST_PRECEDENCE);
        return empty;
    }
    
    @Override
    public void validate() {
        if(!AND.equals(strategy) && !OR.equals(strategy)) {
            throw new IllegalArgumentException("[strategy] must be or、and, so BodyRule config is invalid. now strategy:" + strategy);
        }
    }
    
    /**
     * 由于PredicateSpec.readBody不能调用多次，所以同一个route规则也不能重复配置BodyRule
     * 如果配置了请求体参数校验，那么对于非json格式的请求会完全阻断
     *
     * @param predicateSpec
     * @return
     */
    @Override
    public BooleanSpec predicate(PredicateSpec predicateSpec) {
        return predicateSpec.asyncPredicate(
                new CustomeReadBodyPredicateFactory().applyAsync(c -> {
                    c.setStrategy(strategy);
                    c.setAttrMap(attrMap);
                    c.setPredicate(String.class, body -> {
                        if (attrMap == null || attrMap.isEmpty()) {
                            return true;
                        }
                        /*if (!JsonUtils.isJson(body)) {
                            log.info("request body is not a json data");
                            // todo 这里先让非json格式数据通过，等可以在外部获取body体后这里需要改为false.
                            return true;
                        }*/
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = JSONObject.parseObject(body);
                        } catch (Exception e) {
                            log.error("json parse error", e);
                            return false;
                        }
                        boolean predicateFlag = false;
                    
                        for (Map.Entry<String, List<String>> entry : attrMap.entrySet()) {
                            String name = entry.getKey();
                            // 配置的值为空时表示请求body中key必须不存在或者为空
                            List<String> regex = entry.getValue();
                            String bodyValue;
                            if (regex == null || regex.isEmpty()) {
                                if (StringUtils.isEmpty(jsonObject.getString(name))) {
                                    if (AND.equals(strategy)) {
                                        predicateFlag = true;
                                    } else {
                                        return true;
                                    }
                                } else {
                                    if (AND.equals(strategy)) {
                                        return false;
                                    }
                                }
                            } else {
                                if ((bodyValue = jsonObject.getString(name)) != null && entry.getValue().contains(bodyValue)) {
                                    if (AND.equals(strategy)) {
                                        predicateFlag = true;
                                    } else {
                                        return true;
                                    }
                                } else {
                                    if (AND.equals(strategy)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    
                        return predicateFlag;
                    });
                }));
    }
    
    @Override
    public int getOrder() {
        return this.order;
    }
    
    /*public static class CacheBodyRule extends BodyRule {
        @Override
        public BooleanSpec predicate(PredicateSpec predicateSpec) {
            return predicateSpec.asyncPredicate(new CustomeReadBodyPredicateFactory().applyAsync(c -> {}));
        }
    }*/
}
