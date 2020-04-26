package com.zz.scgateway.config;

import com.google.common.collect.Lists;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-23 14:06
 * ************************************
 */
public class ReatorTest {
    @Test
    public void testCreate() {
        Mono<String> mono1 = Mono.just("hello");
        mono1.subscribe(System.out::println);
        mono1.flatMap(res -> {
            System.out.println(res);
            return Mono.just("world");
        }).subscribe(System.out::println);
    
        System.out.println(Lists.newArrayList("123".split("\\s*")));
    }
    
    public static void main(String[] args) throws InterruptedException {
        Flux.fromIterable(Lists.newArrayList(1, 2, 3, 4, 5))
                .map(arg -> arg * 2) // 处理后映射成新集合
                .subscribe(System.out::print) // subscribe会消费数据流
        ;
        System.out.println();
        Flux.fromIterable(Lists.newArrayList("hello", "world"))
                .flatMap(s -> Flux.fromArray(s.split("\\s*")))
                        //.delayElements(Duration.ofMillis(100)))
                .doOnNext(System.out::print) // doOnNext窃取式，不会消费数据流
                .doOnNext(s -> System.out.println())
                .filter(s -> !s.equals("l"))
                .reduce("-", (x, y) -> x + y) // 第一个参数为初始值，方法中的x参数就是前面自定义的值
                .subscribe(System.out::println);
    }
    
    @Test
    public void testFaltMap() {
        GetString impl1 = new GetStringImpl();
        GetString impl2 = () -> {
            System.out.println("impl2 exec...");
            return Flux.fromIterable(Lists.newArrayList("hello", "world"));
        };
        
        // 将GetString 接口的两个实现类初始化为流数据
        Flux<GetString> delegates = Flux.fromIterable(Lists.newArrayList(
                impl1, impl2
        ));
        
        // 调用两个实现类的test方法，将输出结果合并. 如果不调用subscribe，那么就不会执行流中GetString的实现类的方法
        delegates.flatMap(GetString::test)
                .subscribe(System.out::println);
    }
    
    @Test
    public void testEmpty() {
        Mono.empty().map(r -> {
            System.out.println("11");
            return "1";
        }).switchIfEmpty(Mono.just("2")).flatMap(r -> {
            System.out.println("res:" + r);
            return Mono.just("3");
        }).subscribe(System.out::println);
    }
}
