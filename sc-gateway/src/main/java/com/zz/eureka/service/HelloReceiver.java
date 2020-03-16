package com.zz.eureka.service;

import com.zz.eureka.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-11-22 15:24
 * ************************************
 */
@Component
@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
public class HelloReceiver {

    @RabbitHandler
    public void process(String message) {
        System.out.println("Receive message:" + message);
    }
}
