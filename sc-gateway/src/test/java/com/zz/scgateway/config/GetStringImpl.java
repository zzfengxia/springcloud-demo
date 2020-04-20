package com.zz.scgateway.config;

import com.google.common.collect.Lists;
import reactor.core.publisher.Flux;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-23 16:30
 * ************************************
 */
public class GetStringImpl implements GetString {
    @Override
    public Flux<String> test() {
        System.out.println("impl1 exec...");
        return Flux.fromIterable(Lists.newArrayList("1", "2", "3"));
    }
}
