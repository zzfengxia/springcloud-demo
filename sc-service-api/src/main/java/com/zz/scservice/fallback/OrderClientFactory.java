package com.zz.scservice.fallback;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-18 14:52
 * ************************************
 */
@Component
@Slf4j
public class OrderClientFactory implements FallbackFactory<OrderClientFallback> {
    @Override
    public OrderClientFallback create(Throwable cause) {
        return new OrderClientFallback(cause);
    }
}
