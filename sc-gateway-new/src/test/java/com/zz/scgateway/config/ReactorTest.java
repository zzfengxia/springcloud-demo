package com.zz.scgateway.config;

import com.alibaba.csp.sentinel.util.function.Function;
import com.google.common.collect.Lists;
import com.zz.gateway.common.factory.CustomeReadBodyPredicateFactory;
import org.junit.Test;
import org.reactivestreams.Subscription;
import org.springframework.cloud.gateway.support.GatewayToStringStyler;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-23 14:06
 * ************************************
 */
public class ReactorTest {
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
    
    @Test
    public void toStringStyler() {
        System.out.println(GatewayToStringStyler.filterToStringCreator(CustomeReadBodyPredicateFactory.class));
    }
    
    @Test
    public void testConcatMap() throws InterruptedException {
        Flux.fromIterable(Lists.newArrayList(1, 2, 3))
                .concatMap(x -> {
                    System.out.println("- " + x);
                    return Mono.just(x * 2);
                })
                .next() // .next()只返回第一个值
                .switchIfEmpty(Mono.empty())
                .flatMap(y -> {
                    System.out.println("-- " + y);
                    return Mono.just(y * 2);
                })
                .flatMap(z -> {
                    System.out.println("--- " + z);
                    return Mono.just(z * 2);
                })
                .subscribe();
        
        // next method
        Flux.just("hello", "world")
                .next()
                .doOnNext(System.out::println)
                .subscribe();
    }
    
    @Test
    public void testDefer() throws InterruptedException {
        Mono<Date> staticMono = Mono.just(new Date());
        // defer将supplier方法中的返回结果给下游订阅，每次订阅再执行一遍supplier获取最新的结果
        Mono<Date> dynamicMono = Mono.defer(() -> {
            return Mono.just(new Date());
        });
        
        staticMono.subscribe(System.out::println);
        dynamicMono.subscribe(System.out::println);
        
        Thread.sleep(5000);
        staticMono.subscribe(System.out::println);
        dynamicMono.subscribe(System.out::println);
    }
    
    @Test
    public void testThen() {
        // map每个元素都执行一次方法，返回值仍然存在原流对象中
        // flatMap每个元素都执行一次方法，返回值发射进新的流对象
        // then Flux完成后执行，返回新的Mono。
        Flux.just("Hello", "World")
                .map(s -> s.split(""))
                .flatMap(Flux::fromArray)
                //.then()
                .subscribe(System.out::println);
    }
    
    @Test
    public void testThen2() {
        Mono.defer(() -> {
            if(1 == 1) {
                return Mono.error(new IllegalArgumentException("1 == 1"));
            }
            return Mono.empty();
        }).then(Mono.defer(() -> {
            System.out.println("then exec...");
            return Mono.empty();
            })
        ).onErrorResume(e -> {
            System.out.println("onErrorResume exec...");
            return Mono.empty();
        }).subscribe();
    }
    
    @Test
    public void testOnError() {
        Mono.defer(() -> {
            if(1 == 1) {
                throw new IllegalArgumentException("1 == 1");
            }
            return Mono.empty();
        }).transform(publisher -> publisher.onErrorResume(e -> {
            System.out.println("onErrorResume1 exec...");
            if(e instanceof IllegalArgumentException) {
                return new MonoOperator<Object, Void>(publisher) {
                    @Override
                    public void subscribe(CoreSubscriber actual) {
                        publisher.subscribe(new CoreSubscriber<Object>() {
                            @Override
                            public void onSubscribe(Subscription s) {
                                actual.onSubscribe(s);
                            }
    
                            @Override
                            public void onNext(Object o) {
                                actual.onNext(o);
                            }
    
                            @Override
                            public void onError(Throwable t) {
                                System.out.println("do somethings on error");
                                actual.onError(t);
                            }
    
                            @Override
                            public void onComplete() {
                                actual.onComplete();
                            }
                        });
                    }
                };
            }
            return Mono.error(e);
        })).onErrorResume(e2 -> {
            System.out.println("onErrorResume2 exec...");
            return Mono.empty();
        }).subscribe();
    }
    
    @Test
    public void testTimeout() {
        Flux<Void> res = Flux.defer(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Flux.empty();
        });
        res.timeout(Duration.ofMillis(1000))
                .onErrorResume(v -> {
                    System.out.println("获取流数据结果超时");
                    return Mono.empty();
                }).subscribe();
    }
    
    @Test
    public void testDoChain() {
        /*
         *  执行链条1
         *  执行链条2
         *  执行链条2 then...
         *  执行链条2 then2...
         *  执行链条1 then...
         */
        List<Function<DemoCallChain, Mono<Void>>> supplierList = Lists.newArrayList(
                chain -> {
                    System.out.println("执行链条1");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return chain.doChain()
                            .then(Mono.defer(() -> {
                                System.out.println("执行链条1 then...");
                                return Mono.empty();
                            }));
                },
                chain -> {
                    System.out.println("执行链条2");
                    return chain.doChain()
                            .then(Mono.defer(() -> {
                                System.out.println("执行链条2 then...");
                                return Mono.empty();
                            }))
                            .then(Mono.defer(() -> {
                                System.out.println("执行链条2 then2...");
                                return Mono.empty();
                            }));
                }
        );
        new DemoCallChain(supplierList).doChain().subscribe();
    }
    
    private static class DemoCallChain {
        private int index;
        private List<Function<DemoCallChain, Mono<Void>>> methodSuppliers;
    
        /**
         * 初始化调用链，注入具体调用的提供商
         * @param methodSuppliers
         */
        public DemoCallChain(List<Function<DemoCallChain, Mono<Void>>> methodSuppliers) {
            this.index = 0;
            this.methodSuppliers = methodSuppliers;
        }
    
        public DemoCallChain(int index, DemoCallChain chain) {
            this.index = index;
            this.methodSuppliers = chain.getMethodSuppliers();
        }
    
        public List<Function<DemoCallChain, Mono<Void>>> getMethodSuppliers() {
            return methodSuppliers;
        }
        
        public Mono<Void> doChain() {
            return Mono.defer(() -> {
                if(index < methodSuppliers.size()) {
                    return methodSuppliers.get(index).apply(new DemoCallChain(index + 1, this));
                } else {
                    return Mono.empty();
                }
            });
        }
    }
}
