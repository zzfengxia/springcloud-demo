package com.zz.eureka.config;

import com.zz.eureka.service.HelloSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-11-22 15:27
 * ************************************
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RabbitTest {
    @Autowired
    private HelloSender helloSender;

    @Test
    public void testSender() throws InterruptedException {
        helloSender.send(null);
    }
}
