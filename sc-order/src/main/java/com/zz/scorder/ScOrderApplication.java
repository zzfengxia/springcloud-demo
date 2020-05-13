package com.zz.scorder;

import com.zz.sccommon.config.ClientSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务消费者，向Eureka服务注册中心注册服务
 * EnableFeignClients的EnableFeignClients指定需要扫描的feign client包
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zz.scservice"})
//@EnableCircuitBreaker
@ImportAutoConfiguration({ClientSecurityConfig.class})
public class ScOrderApplication {
    /**
     * OpenFeign 执行流程分析参考：https://www.cnblogs.com/chiangchou/p/api.html
     * @EnableFeignClients 注解开启FeignClient扫描，导入{{@link org.springframework.cloud.openfeign.FeignClientsRegistrar} 来扫描所有指定包下所有<code>FeignClient</code>注解类
     *
     * {@link feign.SynchronousMethodHandler#targetRequest} 是Feign处理请求的方法，target为Feign代理，执行之前会先执行{@link feign.RequestInterceptor}所有注册的请求拦截器，
     * 可以通过实现RequestInterceptor接口自定义一些操作，比如向Header添加参数，修改请求服务ID（多租户定制服务实现）等。
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ScOrderApplication.class, args);
    }
}
