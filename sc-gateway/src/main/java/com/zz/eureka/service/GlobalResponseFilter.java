package com.zz.eureka.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-31 16:12
 * ************************************
 */
@Component
@Slf4j
public class GlobalResponseFilter implements GlobalFilter, Ordered {
    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return modifyResponseBodyGatewayFilterFactory.apply((c -> c.setRewriteFunction(String.class, String.class, (serverWebExchange, body) -> {
            System.out.println("origin response body:" + body);
            HttpStatus responseStatus = serverWebExchange.getResponse().getStatusCode();
            if(responseStatus != null && responseStatus.value() != 200) {
                // 后台服务响应不是正常的200状态， 这里只记录异常信息，给客户端响应正常状态码，使用json格式的信息标识错误信息
                log.info("服务端响应http status:{}, name:{}, reason:{}", responseStatus.value(), responseStatus.name(), responseStatus.getReasonPhrase());
                serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
    
                Map<String, Object> errorAttributes = new HashMap<>();
                errorAttributes.put("returnDesc", "服务器异常");
                // returnCode 可以转换为自定义的code
                errorAttributes.put("returnCode", "1001");
                errorAttributes.put("transactionid", "1234567890");
                errorAttributes.put("signType", null);
                errorAttributes.put("sign", null);
                
                body = JSON.toJSONString(errorAttributes);
            }
            System.out.println("response header:" + serverWebExchange.getResponse().getHeaders());
            return Mono.just(body);
        }))).filter(exchange, chain);
    }
    
    /**
     * 设置执行顺序，值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return -2;
    }
}
