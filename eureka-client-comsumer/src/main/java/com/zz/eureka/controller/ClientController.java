package com.zz.eureka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ClientController {
    /**
     * 实现rest客户端功能
     * LoadBalanced标识实现负载客户端，此时URI需要使用虚拟主机名（即服务提供者的名称，而不是主机名）
     */
    @Autowired
    @LoadBalanced
    private RestTemplate balanceTemplate;

    /**
     * 实现rest客户端功能
     * 无负载功能时，URL需要使用物理主机名(IP或域名，而不是注册到服务中心的服务名)
     */
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("client")
    public String fetchServer() {
        // 负载客户端
        String res = balanceTemplate.getForObject("http://server-provider/getUser/2", String.class);
        System.out.println("balance rest:" + res);
        // 无负载功能的客户端
        String res2 = restTemplate.getForObject("http://localhost:8081/getUser/2", String.class);
        System.out.println("common rest:" + res2);
        return res;
    }
}
