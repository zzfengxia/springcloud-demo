package com.zz.eureka.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-11-22 15:14
 * ************************************
 */
@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "hello";

    /**
     * 创建一个持久化的消息队列
     *
     * @return
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }
}
