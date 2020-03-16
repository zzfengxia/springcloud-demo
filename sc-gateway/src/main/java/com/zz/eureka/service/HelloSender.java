package com.zz.eureka.service;

import com.zz.eureka.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-11-22 15:17
 * ************************************
 */
@Component
public class HelloSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg) {
        if(msg == null) {
            msg = "Hello World!";
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, msg);
    }
}
