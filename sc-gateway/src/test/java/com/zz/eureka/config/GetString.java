package com.zz.eureka.config;

import org.springframework.cloud.gateway.route.Route;
import reactor.core.publisher.Flux;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-23 16:29
 * ************************************
 */
public interface GetString {
    Flux<String> test();
}
